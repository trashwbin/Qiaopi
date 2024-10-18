package com.qiaopi.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.constant.JwtClaimsConstant;
import com.qiaopi.constant.UserConstants;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.dto.UserResetPasswordDTO;
import com.qiaopi.dto.UserUpdateDTO;
import com.qiaopi.entity.*;
import com.qiaopi.exception.base.BaseException;
import com.qiaopi.exception.code.CodeErrorException;
import com.qiaopi.exception.code.CodeTimeoutException;
import com.qiaopi.exception.user.*;
import com.qiaopi.mapper.*;
import com.qiaopi.properties.JwtProperties;
import com.qiaopi.service.LetterService;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.AccountValidator;
import com.qiaopi.utils.JwtUtil;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.utils.StringUtils;
import com.qiaopi.utils.ip.IpUtils;
import com.qiaopi.vo.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cn.hutool.core.bean.BeanUtil.copyProperties;
import static com.qiaopi.result.AjaxResult.error;
import static com.qiaopi.result.AjaxResult.success;
import static com.qiaopi.utils.MessageUtils.message;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;
    private final FriendMapper friendMapper;
    private final AvatarMapper avatarMapper;
    private final FontColorMapper fontColorMapper;
    private final FontMapper fontMapper;
    private final PaperMapper paperMapper;
    private final CardMapper cardMapper;
    private final JwtProperties jwtProperties;
    private final RedisTemplate redisTemplate;
    private final LetterMapper letterMapper;
    private final LetterService letterService;

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

        return UserLoginVO.builder()
                .id(user.getId())
                .token(token)
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public String register(UserRegisterDTO userRegisterDTO) {

        String msg = "", email = userRegisterDTO.getUsername().toLowerCase(), password = userRegisterDTO.getPassword();
        User user = new User();
        user.setEmail(email);

        //一系列检验
        if (StringUtils.isEmpty(email)) {
            msg = message("user.email.empty");
        } else if (!AccountValidator.isValidEmail(email)) {
            msg = message("email.format.error");
        } else if (StringUtils.isEmpty(password)) {
            msg = message("user.password.empty");
        } else if (userRegisterDTO.getCode() == null) {
            msg = message("user.code.empty");
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = message("user.password.length");
        } else if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getConfirmPassword())) {
            msg = message("user.password.confirm.error");
        } else if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) != null) {
            msg = email + message("email.exists");
        }
        String emailKey = message("user.register.prefix") + email;

        String code = (String) redisTemplate.opsForValue().get(emailKey);

        if (StringUtils.isEmpty(code)) {
            msg = message("user.code.expire");
        } else if (!code.equals(userRegisterDTO.getCode())) {
            msg = message("user.code.error");
        }
        //检验是否通过，如果不通过，抛出异常
        if (!StringUtils.isEmpty(msg)) {
            throw new UserException(msg);
        }
        //删除验证码
        redisTemplate.delete(emailKey);
        //设置昵称
        user.setNickname(email.substring(0, email.indexOf("@")));
        //设置用户名
        user.setUsername(message("user.username.prefix") + System.currentTimeMillis() + getStringRandom(3));
        //设置密码
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));

        //注册后添加默认参数
        //设置默认头像
        user.setAvatar(avatarMapper.selectById(1).getUrl());
        //设置默认性别
        user.setSex("男");
        //设置默认字体颜色
        user.setFontColors(Collections.singletonList(BeanUtil.copyProperties(fontColorMapper.selectById(1), FontColorVO.class)));
        //设置默认字体
        user.setFonts(Collections.singletonList(BeanUtil.copyProperties(fontMapper.selectById(1), FontVO.class)));
        //设置默认纸张
        user.setPapers(Collections.singletonList(BeanUtil.copyProperties(paperMapper.selectById(1), PaperVO.class)));
        //设置默认功能卡
        FunctionCard functionCard = cardMapper.selectById(1L);
        FunctionCardVO functionCardVO = copyProperties(functionCard, FunctionCardVO.class);
        functionCardVO.setNumber(1);
        user.setFunctionCards(Collections.singletonList(functionCardVO));
        //设置默认印章
        user.setSignets(Collections.emptyList());
        //设置默认地址
        user.setAddresses(Collections.emptyList());
        //发送邮件
        Letter letter = letterMapper.selectById(0);
        letter.setRecipientEmail(email);
        letterService.sendLetterToEmail(Collections.singletonList(letter));
        //设置默认余额
        user.setMoney(100L);
        userMapper.insert(user);
        msg = message("user.register.success");

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
        if (userResetPasswordDTO.getPassword().length() < UserConstants.PASSWORD_MIN_LENGTH
                || userResetPasswordDTO.getPassword().length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new UserException(message("user.password.length"));
        } else if (!userResetPasswordDTO.getPassword().equals(userResetPasswordDTO.getConfirmPassword())) {
            throw new UserConfirmPasswordNotEqualsException();
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

        userMapper.update(user, new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()));

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
        repository.put("fonts", CollUtil.isEmpty(fonts) ? Collections.emptyList() : fonts);

        List<PaperVO> papers = user.getPapers();
        repository.put("papers", CollUtil.isEmpty(papers) ? Collections.emptyList() : papers);

        List<FontColorVO> fontColors = user.getFontColors();
        repository.put("fontColors", CollUtil.isEmpty(fontColors) ? Collections.emptyList() : fontColors);

        List<SignetVO> signets = user.getSignets();
        repository.put("signets", CollUtil.isEmpty(signets) ? Collections.emptyList() : signets);

        return repository;
    }

    @Override
    public void updateUsername(UserUpdateDTO userUpdateDTO) {
        //管理员用户名禁止更改
        if (UserContext.getUserId()==1L){
            throw new UserException(MessageUtils.message("user.admin.error"));
        }
        //检验用户名是否合法
        if (StringUtils.isEmpty(userUpdateDTO.getUsername()) || !AccountValidator.isValidUsername(userUpdateDTO.getUsername())) {
            throw new UserException(message("user.username.length"));
        }
        //检验用户名是否存在
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, userUpdateDTO.getUsername())) != null) {
            throw new UserException(message("user.username.exists"));
        }
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        user.setUsername(userUpdateDTO.getUsername());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(UserUpdateDTO userUpdateDTO) {
        //检验密码是否合法
        if (StringUtils.isEmpty(userUpdateDTO.getOldPassword()) || StringUtils.isEmpty(userUpdateDTO.getNewPassword()) || StringUtils.isEmpty(userUpdateDTO.getConfirmPassword())) {
            throw new UserPasswordNotMatchException();
        }
        //检验新密码是否合法
        if (userUpdateDTO.getNewPassword().length() < UserConstants.PASSWORD_MIN_LENGTH
                || userUpdateDTO.getNewPassword().length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new UserException("user.password.length", null);
        } else if (!userUpdateDTO.getNewPassword().equals(userUpdateDTO.getConfirmPassword())) {
            throw new UserConfirmPasswordNotEqualsException();
        }

        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        //检验旧密码是否正确
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(userUpdateDTO.getOldPassword().getBytes()))) {
            throw new UserException("user.old.password.error", null);
        }

        user.setPassword(DigestUtils.md5DigestAsHex(userUpdateDTO.getNewPassword().getBytes()));
        userMapper.updateById(user);
    }

    @Override
    public void updateUserInfo(UserUpdateDTO userUpdateDTO) {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new UserNotExistsException();
        }
        if (StringUtils.isNotEmpty(userUpdateDTO.getNickname())) {
            user.setNickname(userUpdateDTO.getNickname());
        }
        if (userUpdateDTO.getAvatarId()!=null) {
            Avatar avatar = avatarMapper.selectById(userUpdateDTO.getAvatarId());
            user.setAvatar(avatar.getUrl());
        }
        if (StringUtils.isNotEmpty(userUpdateDTO.getSex())) {
            user.setSex(userUpdateDTO.getSex());
        }
        userMapper.updateById(user);
    }

    @Override
    public Long getUserMoney(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }
        return user.getMoney();
    }

    @Override
    public List<FriendVO> getMyFriends(Long userId) {
        //查询好友列表
        List<Friend> friendList = friendMapper.selectList(new LambdaQueryWrapper<Friend>().eq(Friend::getOwningId, userId));

        if (CollUtil.isEmpty(friendList)) {
            return Collections.emptyList();
        }

        return BeanUtil.copyToList(friendList, FriendVO.class);
    }

    @Override
    public List<Address> getMyAddress(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }
        return user.getAddresses();
    }

    @Override
    public List<Address> getFriendAddress(Long friendId) {
        //查询好友地址,根据好友id和所属用户id查询
        Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>().eq(Friend::getId, friendId).eq(Friend::getOwningId, UserContext.getUserId()));
        if (friend == null) {
            throw new FriendNotExistsException();
        }
        return friend.getAddresses();
    }

    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String sender;
    @Value("${spring.mail.nickname}")
    private String nickname;
    @Override
    public void sendResetPasswordCode(String email) {
        email = email.toLowerCase();

        //判断邮箱是否合法
        if (!AccountValidator.isValidEmail(email)) {
            throw new BaseException(message("email.format.error"));
        }
        String verify = message("user.reset.password.prefix") + email;
        //判断5分钟内是否发送过验证码
        if (redisTemplate.hasKey(verify)) {
            throw new UserException(message("user.get.code.limit"));
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        // 验证邮箱是否未注册
        if (user == null) {
            throw new BaseException(message("email.not.exists"));
        }

        // 创建一个邮件
        //SimpleMailMessage message = new SimpleMailMessage();
        // 创建一个 MimeMessage 代替 SimpleMailMessage
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 设置发件人
            helper.setFrom(nickname + '<' + sender + '>');

            // 设置收件人
            helper.setTo(email);

            // 设置邮件主题
            helper.setSubject(nickname + "-重置密码");

            // 生成六位随机数
            String code = RandomUtil.randomNumbers(6);
            log.info("重置密码验证码：{}", code);

            // 将验证码存入 redis，有效期为5分钟
            redisTemplate.opsForValue().set(verify, code, Duration.ofMinutes(5));

            // 定义邮件内容，使用 HTML
            String content = "<div style='font-family: Arial, sans-serif;'>" +
                    "<h1>" + nickname + "账户密码重置 </h1>" +
                    "<h2>你好，" + user.getNickname() + "<h2>" +
                    "<h2>【验证码】您的重置密码验证码为：" + code + "</h2>" +
                    "<p style='font-size: 14px;'>请在五分钟内使用此验证码重置您的密码，逾期作废。</p>" +
                    "<p style='font-size: 14px;'>如果您没有请求重置密码，请忽略此邮件。</p>" +
                    "<hr>" +
                    "<p style='font-size: 12px; color: gray;'>此邮件为系统自动发送，请勿回复。</p>" +
                    "</div>";

            // 设置邮件内容为 HTML
            helper.setText(content, true);

            // 发送邮件
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new UserException(message("user.sent.code.failed"));
        } catch (MailException e) {
            throw new UserException(message("user.sent.code.failed.by.email"));
        }
    }

    @Override
    public Map<String, String> getCode() {
        //设置验证码的宽和高，获取验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 30);

        //设置验证码的唯一标识uuid
        String verify = IdUtil.simpleUUID();

        //图形验证码写出，可以写出到文件，也可以写出到流
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        captcha.write(os);
        //获取验证码
        String code = captcha.getCode();
        log.info("获取验证码:{}", code);

        //将验证码存入redis
        redisTemplate.opsForValue().set(verify, code, Duration.ofMinutes(5));

        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);

        map.put("uuid", verify);
        //map.put("code", code);
        map.put("img", Base64.encode(os.toByteArray()));
        return map;
    }

    @Override
    public void sendCode(String email) {
        // 邮箱转小写
        email = email.toLowerCase();
        // 验证邮箱是否已经注册
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, email))) {
            throw new UserException(message("email.exists"));
        }
        //判断邮箱是否合法
        else if (!AccountValidator.isValidEmail(email)) {
            throw new UserException(message("email.format.error"));
        }

        String verify = message("user.register.prefix") + email;
        //判断5分钟内是否发送过验证码
        if (redisTemplate.hasKey(verify)) {
            throw new UserException(message("user.sent.code.limit"));
        }

        // 创建一个 MimeMessage 代替 SimpleMailMessage
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 设置发件人
            helper.setFrom(nickname + '<' + sender + '>');

            // 设置收件人
            helper.setTo(email);

            // 设置邮件主题
            helper.setSubject("欢迎访问 " + nickname);

            // 生成六位随机数
            String code = RandomUtil.randomNumbers(6);
            log.info("邮箱验证码：{}", code);

            // 将验证码存入 redis，有效期为5分钟
            redisTemplate.opsForValue().set(verify, code, Duration.ofMinutes(5));

            // 定义邮件内容，使用 HTML
            String content = "<div style='font-family: Arial, sans-serif;'>" +
                    "<h1>欢迎访问 " + nickname + "</h1>" +
                    "<h2>【验证码】您的验证码为：" + code + "</h2>" +
                    "<p style='font-size: 14px;'>验证码五分钟内有效，逾期作废。</p>" +
                    "<hr>" +
                    "<p style='font-size: 12px; color: gray;'>此邮件为系统自动发送，请勿回复。</p>" +
                    "</div>";

            // 设置邮件内容为 HTML
            helper.setText(content, true);

            // 发送邮件
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new UserException(message("user.sent.code.failed"));
        } catch (MailException e) {
            throw new UserException(message("user.sent.code.failed.by.email"));
        }

    }

    @Override
    public List<FunctionCardVO> getMyFunctionCard(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException();
        }
        return user.getFunctionCards();
    }

    @Override
    public List<Avatar> getAvatarList() {
        return avatarMapper.selectList(null);
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
