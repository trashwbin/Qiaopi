package com.qiaopi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.constant.JwtClaimsConstant;
import com.qiaopi.constant.UserConstants;
import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.entity.User;
import com.qiaopi.exception.code.CodeErrorException;
import com.qiaopi.exception.code.CodeTimeoutException;
import com.qiaopi.exception.user.UserNameNotMatchException;
import com.qiaopi.exception.user.UserNotExistsException;
import com.qiaopi.exception.user.UserPasswordNotMatchException;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.properties.JwtProperties;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.AccountValidator;
import com.qiaopi.utils.JwtUtil;
import com.qiaopi.utils.StringUtils;
import com.qiaopi.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static cn.hutool.core.bean.BeanUtil.copyProperties;
import static com.qiaopi.constant.MessageConstant.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private  UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {

        //检验验证码是否正确
        //从redis中获取验证码
        String code = (String) redisTemplate.opsForValue().get(userLoginDTO.getUuid());
        if (code == null) {
            //验证码已过期
            throw new CodeTimeoutException();
        } else if (!code.equals(userLoginDTO.getCode())) {
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
            throw new UserNotExistsException();
        }

        //更新最后登录时间，ip地址
        user.setLoginDate(LocalDateTime.now());
        user.setLoginIp(userLoginDTO.getLoginIp());
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
                .username(user.getUsername())
                .token(token)
                .sex(user.getSex())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .email(user.getEmail())
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
            msg = EMAIL_EMPTY;
        } else if (!AccountValidator.isValidEmail(email)) {
            msg = EMAIL_FORMAT_ERROR;
        } else if (StringUtils.isEmpty(password))
        {
            msg = PASSWORD_EMPTY;
        }
        else if (userRegisterDTO.getCode() == null)
        {
            msg = CODE_EMPTY;
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = PASSWORD_FORMAT_ERROR;
        }
        else if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) != null)
        {
            msg = email + EMAIL_EXISTS;
        }else {

            String emailKey = "email_code_" + email;

            String code = (String) redisTemplate.opsForValue().get(emailKey);

            if (StringUtils.isEmpty(code)) {
                msg = CODE_EXPIRED;
            } else if (!code.equals(userRegisterDTO.getCode())) {
                msg = CODE_ERROR;
            }else {
                redisTemplate.delete(emailKey);
                //设置昵称
                user.setNickname(email.substring(0, email.indexOf("@")));
                //设置用户名
                user.setUsername(USERNAME_PREFFIX + System.currentTimeMillis() + getStringRandom(3));
                //设置密码
                user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
                userMapper.insert(user);
                msg = Register_SUCCESS;
            }
        }
        return msg;
    }

    @Override
    public void resetPasswordByEmail(UserRegisterDTO userRegisterDTO) {
        //检验验证码是否正确
        //从redis中获取验证码
        // 根据用户名查询用户信息
        if (!AccountValidator.isValidEmail(userRegisterDTO.getUsername())) {
            //用户名不合法
            throw new UserNotExistsException();
        }
        //将邮箱转换为小写
        userRegisterDTO.setUsername(userRegisterDTO.getUsername().toLowerCase());

        String verify = "email_reset_code_" + userRegisterDTO.getUsername();
        String code = (String) redisTemplate.opsForValue().get(verify);
        if (code == null) {
            //验证码已过期
            throw new CodeTimeoutException();
        } else if (!code.equals(userRegisterDTO.getCode())) {
            //验证码不匹配
            throw new CodeErrorException();
        }
        if (StringUtils.isEmpty(userRegisterDTO.getPassword())) {
            //密码为空
            throw new UserPasswordNotMatchException();
        }
        //删除验证码
        redisTemplate.delete(verify);
        //为了方便查询，将用户名和密码封装到User对象中
        //对前端传过来的明文密码进行MD5加密处理
        User user = User.builder().password(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes())).email(userRegisterDTO.getUsername()).build();

        userMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getEmail,user.getEmail()));

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
