package com.qiaopi.controller.user;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.dto.UserResetPasswordDTO;
import com.qiaopi.dto.UserUpdateDTO;
import com.qiaopi.entity.User;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.AccountValidator;
import com.qiaopi.utils.StringUtils;
import com.qiaopi.vo.UserLoginVO;
import com.qiaopi.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.qiaopi.result.AjaxResult.error;
import static com.qiaopi.result.AjaxResult.success;
import static com.qiaopi.utils.MessageUtils.message;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户相关接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private final UserService userService;
    private final JavaMailSender javaMailSender;
    private final RedisTemplate redisTemplate;
    private final UserMapper userMapper;
    @Value("${spring.mail.username}")
    private String sender;
    @Value("${spring.mail.nickname}")
    private String nickname;

    /**
     * 登录
     *
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public AjaxResult login(@RequestBody UserLoginDTO userLoginDTO) {


        log.info("用户登录：{}", userLoginDTO);

        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return success(message("user.login.success"), userLoginVO);
    }

    /**
     * 获取验证码
     *
     * @return
     */
    @GetMapping("/getCode")
    @Operation(summary = "获取验证码")
    public AjaxResult getCode() {

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

        return success(message("user.get.code.success"), map);
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public AjaxResult register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册：{}", userRegisterDTO);

        String msg = userService.register(userRegisterDTO);

        log.info("用户注册结果：{}", msg);
        return StringUtils.equals(msg, message("user.register.success")) ? success(msg) : error(msg);
    }

    @GetMapping("/sendCode")
    @Operation(summary = "发送验证码")
    public AjaxResult sendCode(@RequestParam("email") String email) {
        // 邮箱转小写
        email = email.toLowerCase();
        // 验证邮箱是否已经注册
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, email))) {
            return error(message("email.exists"));
        }
        //判断邮箱是否合法
        else if (!AccountValidator.isValidEmail(email)) {
            return error(message("email.format.error"));
        }

        String verify = message("user.register.prefix") + email;
        //判断5分钟内是否发送过验证码
        if (redisTemplate.hasKey(verify)) {
            return error(message("user.sent.code.limit"));
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
            return error(message("user.sent.code.failed"));
        } catch (MailException e) {
            return error(message("user.sent.code.failed") + "，请检查邮箱是否正确");
        }

        /*
        // 设置发件人
        message.setFrom(nickname+'<'+sender+'>');

        // 设置收件人
        message.setTo(email);

        // 设置邮件主题
        message.setSubject("欢迎访问"+nickname);

        //生成六位随机数
        String code = RandomUtil.randomNumbers(6);

        //将验证码存入redis，有效期为5分钟
        redisTemplate.opsForValue().set(verify, code, Duration.ofMinutes(5));

        String content = "【验证码】您的验证码为：" + code + " 。 验证码五分钟内有效，逾期作废。\n\n\n" +
                "------------------------------\n\n\n" ;

        message.setText(content);

        // 发送邮件
        javaMailSender.send(message);
        */

//        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);
//        map.put("uuid", verify);
        //map.put("code", code);

        return success(message("user.sent.code.success"));
    }

    @PostMapping("/resetPasswordByEmail")
    @Operation(summary = "通过邮箱重置密码")
    public AjaxResult resetPasswordByEmail(@RequestBody UserResetPasswordDTO userResetPasswordDTO) {

        userService.resetPasswordByEmail(userResetPasswordDTO);

        // 验证邮箱是否已经注册
        return success(message("user.reset.password.success"));
    }

    @GetMapping("/sendResetPasswordCode")
    @Operation(summary = "发送重置密码验证码")
    public AjaxResult sendResetPasswordCode(@RequestParam("email") String email) {

        email = email.toLowerCase();

        //判断邮箱是否合法
        if (!AccountValidator.isValidEmail(email)) {
            return error(message("email.format.error"));
        }
        String verify = message("user.reset.password.prefix") + email;
        //判断5分钟内是否发送过验证码
        if (redisTemplate.hasKey(verify)) {
            return error(message("user.get.code.limit"));
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        // 验证邮箱是否未注册
        if (user == null) {
            return error(message("email.not.exists"));
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
            return error(message("user.sent.code.failed"));
        } catch (MailException e) {
            return error(message("user.sent.code.failed") + "，请检查邮箱是否正确");
        }

        return success(message("user.sent.code.success"));
    }

    @GetMapping("/getUserInfo")
    @Operation(summary = "获取用户信息")
    public AjaxResult getUserInfo(HttpServletRequest request) {
        UserVO userVO = userService.getUserInfo(UserContext.getUserId());
        log.info("获取用户信息：{}", userVO);
        return success(message("user.get.info.success"), userVO);
    }

    @GetMapping("/getUserRepository")
    @Operation(summary = "获取用户仓库")
    public AjaxResult getUserRepository(){
        Map<String,List> userRepository = userService.getUserRepository(UserContext.getUserId());
        log.info("用户仓库：{}",userRepository);
        return success(message("user.get.repository.success"),userRepository);
    }

    @PostMapping("/updateUsername")
    @Operation(summary = "修改用户名")
    public AjaxResult updateUsername(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("修改用户名：{}", userUpdateDTO.getUsername());
        userService.updateUsername(userUpdateDTO);
        return success(message("user.update.username.success"));
    }

    @PostMapping("/updatePassword")
    @Operation(summary = "修改密码")
    public AjaxResult updatePassword(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("用户修改密码:{}",UserContext.getUserId());
        userService.updatePassword(userUpdateDTO);
        return success(message("user.update.password.success"));
    }

    @PostMapping("/updateUserInfo")
    @Operation(summary = "修改用户信息")
    public AjaxResult updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("修改用户信息：{}", userUpdateDTO);
        userService.updateUserInfo(userUpdateDTO);
        return success(message("user.update.info.success"));
    }

    @GetMapping("/getUserMoney")
    @Operation(summary = "获取用户猪仔钱")
    public AjaxResult getUserMoney(){
        Long money = userService.getUserMoney(UserContext.getUserId());
        log.info("获取'{}'猪仔钱：{}",UserContext.getUserId(),money);
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);
        map.put("money", money.toString());
        return success(message("user.get.money.success"),map);
    }
}






