package com.qiaopi.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.constant.JwtClaimsConstant;
import com.qiaopi.constant.UserConstants;
import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.dto.UserResetPasswordDTO;
import com.qiaopi.entity.Font;
import com.qiaopi.entity.Paper;
import com.qiaopi.entity.User;
import com.qiaopi.exception.code.CodeErrorException;
import com.qiaopi.exception.code.CodeTimeoutException;
import com.qiaopi.exception.user.*;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.properties.JwtProperties;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.AccountValidator;
import com.qiaopi.utils.JwtUtil;
import com.qiaopi.utils.StringUtils;
import com.qiaopi.utils.ip.IpUtils;
import com.qiaopi.vo.FontVO;
import com.qiaopi.vo.PaperVO;
import com.qiaopi.vo.UserLoginVO;
import com.qiaopi.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static cn.hutool.core.bean.BeanUtil.copyProperties;
import static com.qiaopi.utils.MessageUtils.message;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;
    private final RedisTemplate redisTemplate;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {

        //检验验证码是否正确
        //从redis中获取验证码
        String code = (String) redisTemplate.opsForValue().get(userLoginDTO.getUuid());
        if (code == null) {
            //验证码已过期
            throw new CodeTimeoutException();
        //} else if (!code.equals(userLoginDTO.getCode())) {
        } else if (!code.equalsIgnoreCase(userLoginDTO.getCode())) {
            //验证码不匹配
            throw new CodeErrorException();
        }
        // 根据用户名查询用户信息
        if (!AccountValidator.isValidAccount(userLoginDTO.getUsername())) {
            //用户名不合法
            throw new UserNameNotMatchException();
        }
        if (StringUtils.isEmpty(userLoginDTO.getPassword())) {
            //密码为空
            throw new UserPasswordNotMatchException();
        }
        redisTemplate.delete(userLoginDTO.getUuid());
        //为了方便查询，将用户名和密码封装到User对象中
        //对前端传过来的明文密码进行MD5加密处理
        User userLogin = User.builder().password(DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes())).build();

        if (AccountValidator.isValidEmail(userLoginDTO.getUsername())) {
            userLogin.setEmail(userLoginDTO.getUsername().toLowerCase());
        } else {
            userLogin.setUsername(userLoginDTO.getUsername());
        }

        //根据用户名和密码查询用户信息的条件构造器
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (userLogin.getUsername() != null) {
            wrapper
                    .eq(User::getUsername, userLogin.getUsername())
                    .eq(User::getPassword, userLogin.getPassword());
        } else {
            wrapper
                    .eq(User::getEmail, userLogin.getEmail())
                    .eq(User::getPassword, userLogin.getPassword());
        }
        //根据条件查询用户信息,mybatis-plus会自动查询，封装到User对象中
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            //账号不存在
            throw new UserLoginNotExistsException();
        }

        //更新最后登录时间，ip地址
        user.setLoginDate(LocalDateTime.now());
        user.setLoginIp(IpUtils.getIpAddr());
        userMapper.updateById(user);

        //生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .money(user.getMoney())
                .build();

        return userLoginVO;
    }

    @Override
    public String register(UserRegisterDTO userRegisterDTO) {

        String msg = "", email = userRegisterDTO.getUsername().toLowerCase(), password = userRegisterDTO.getPassword();
        User user = new User();
        user.setEmail(email);


        if (StringUtils.isEmpty(email))
        {
            msg = message("user.email.empty");
        } else if (!AccountValidator.isValidEmail(email)) {
            msg =message("email.format.error");
        } else if (StringUtils.isEmpty(password))
        {
            msg = message("user.password.empty");
        }
        else if (userRegisterDTO.getCode() == null)
        {
            msg = message("user.code.empty");
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = message("user.password.length");
        }
        else if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) != null)
        {
            msg = email + message("email.exists");
        }else {

            String emailKey = message("user.register.prefix") + email;

            String code = (String) redisTemplate.opsForValue().get(emailKey);

            if (StringUtils.isEmpty(code)) {
                msg = message("user.code.expire");
            } else if (!code.equals(userRegisterDTO.getCode())) {
                msg = message("user.code.error");
            }else {
                redisTemplate.delete(emailKey);
                //设置昵称
                user.setNickname(email.substring(0, email.indexOf("@")));
                //设置用户名
                user.setUsername(message("user.username.prefix") + System.currentTimeMillis() + getStringRandom(3));
                //设置密码
                user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
                userMapper.insert(user);
                msg = message("user.register.success");
            }
        }
        return msg;
    }

    @Override
    public void resetPasswordByEmail(UserResetPasswordDTO userResetPasswordDTO) {
        //检验验证码是否正确
        //从redis中获取验证码
        // 根据用户名查询用户信息
        if (!AccountValidator.isValidEmail(userResetPasswordDTO.getUsername())) {
            //用户名不合法
            throw new UserNotExistsException();
        }
        //将邮箱转换为小写
        userResetPasswordDTO.setUsername(userResetPasswordDTO.getUsername().toLowerCase());

        String verify = message("user.reset.password.prefix") + userResetPasswordDTO.getUsername();
        String code = (String) redisTemplate.opsForValue().get(verify);
        if (code == null) {
            //验证码已过期
            throw new CodeTimeoutException();
        } else if (!code.equals(userResetPasswordDTO.getCode())) {
            //验证码不匹配
            throw new CodeErrorException();
        }
        if (StringUtils.isEmpty(userResetPasswordDTO.getPassword())) {
            //密码为空
            throw new UserPasswordNotMatchException();
        }
        //删除验证码
        redisTemplate.delete(verify);
        //为了方便查询，将用户名和密码封装到User对象中
        //对前端传过来的明文密码进行MD5加密处理
        User user = User.builder().password(DigestUtils.md5DigestAsHex(userResetPasswordDTO.getPassword().getBytes())).email(userResetPasswordDTO.getUsername()).build();

        userMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getEmail,user.getEmail()));

    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }

        return copyProperties(user, UserVO.class);
    }

    @Override
    public Map<String, List> getUserRepository(Long userId) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new UserNotExistsException();
        }
        Map<String, List> repository = new HashMap<>();
        List<FontVO> fonts = user.getFonts();
        repository.put("fonts", CollUtil.isEmpty(fonts)? Collections.emptyList(): fonts);

        List<PaperVO> papers = user.getPapers();
        repository.put("papers", CollUtil.isEmpty(papers)? Collections.emptyList(): papers);
        return repository;
    }

    //生成随机用户名，数字和字母组成,
    public String getStringRandom(int length) {

        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (random.nextInt(26) + temp);
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }
}
