package com.qiaopi.controller.user;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.entity.User;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.server.UserService;
import com.qiaopi.utils.AccountValidator;
import com.qiaopi.utils.StringUtils;
import com.qiaopi.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.qiaopi.constant.MessageConstant.*;
import static com.qiaopi.result.AjaxResult.error;
import static com.qiaopi.result.AjaxResult.success;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;


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

        return success(userLoginVO);
    }

    /**
     * 获取验证码
     *
     * @return
     */
    @GetMapping("/getCode")
    @Operation(summary = "获取验证码")
    public AjaxResult getrCode() {
        log.info("获取验证码");

        //设置验证码的宽和高，获取验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100);

        //设置验证码的唯一标识uuid
        String verify = IdUtil.simpleUUID();

        //图形验证码写出，可以写出到文件，也可以写出到流
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        lineCaptcha.write(os);
        //获取验证码
        String code = lineCaptcha.getCode();

        //将验证码存入redis
        redisTemplate.opsForValue().set(verify, code, Duration.ofMinutes(1));

        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);

        map.put("uuid", verify);
        //map.put("code", code);
        map.put("img", Base64.encode(os.toByteArray()));

        return success(map);
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

        return StringUtils.isEmpty(msg) ? success(Register_SUCCESS) : error(msg);
    }



    @Value("${spring.mail.username}")
    private String sender;

    @Value("${spring.mail.nickname}")
    private String nickname;

    @GetMapping("/sendCode")
    @Operation(summary = "发送验证码")
    public AjaxResult sendCode(@RequestParam("email") String email)
    {
        // 验证邮箱是否已经注册
        if (userMapper.exists( new LambdaQueryWrapper<User>().eq(User::getEmail, email))) {
            return error(EMAIL_EXISTS);
        }
        //判断邮箱是否合法
        else if (!AccountValidator.isValidEmail(email)) {
            return error(EMAIL_FORMAT_ERROR);
        }

        String verify = "email_code_" + email;
        //判断5分钟内是否发送过验证码
        if (redisTemplate.hasKey(verify)) {
            return error(CODE_SEND_FREQUENTLY);
        }

        // 创建一个邮件
        SimpleMailMessage message = new SimpleMailMessage();

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

        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>(5);
        map.put("uuid", verify);
        //map.put("code", code);

        return success("发送成功", map);
    }
}



