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
import com.qiaopi.entity.Address;
import com.qiaopi.entity.User;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.service.UserService;
import com.qiaopi.utils.AccountValidator;
import com.qiaopi.utils.StringUtils;
import com.qiaopi.vo.FriendVO;
import com.qiaopi.vo.FunctionCardVO;
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
     * @return
     */
    @GetMapping("/getCode")
    @Operation(summary = "获取验证码")
    public AjaxResult getCode() {
        Map<String, String> map = userService.getCode();
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
        return success(userService.register(userRegisterDTO));
    }

    @GetMapping("/sendCode")
    @Operation(summary = "发送验证码")
    public AjaxResult sendCode(@RequestParam("email") String email) {
        userService.sendCode(email);
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

        userService.sendResetPasswordCode(email);


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

    @PutMapping("/updateUsername")
    @Operation(summary = "修改用户名")
    public AjaxResult updateUsername(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("修改用户名：{}", userUpdateDTO.getUsername());
        userService.updateUsername(userUpdateDTO);
        return success(message("user.update.username.success"));
    }

    @PutMapping("/updatePassword")
    @Operation(summary = "修改密码")
    public AjaxResult updatePassword(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("用户修改密码:{}",UserContext.getUserId());
        userService.updatePassword(userUpdateDTO);
        return success(message("user.update.password.success"));
    }

    @GetMapping("/getAvatarList")
    @Operation(summary = "获取头像列表")
    public AjaxResult getAvatarList(){
        log.info("获取头像列表");
        return success(message("user.get.avatar.list.success"),userService.getAvatarList());
    }

    @PutMapping("/updateUserInfo")
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


    @GetMapping("/getMyFriends")
    @Operation(summary = "获取我的好友")
    public AjaxResult getMyFriends(){
        log.info("获取我的好友：{}",UserContext.getUserId());
        List<FriendVO> friends = userService.getMyFriends(UserContext.getUserId());
        return success(message("user.get.friends.success"),friends);
    }

    @GetMapping("/getFriendAddress")
    @Operation(summary = "获取当前好友地址")
    public AjaxResult getFriendAddress(@RequestParam("friendId") Long friendId){
        log.info("获取当前好友地址：{}",friendId);
        List<Address> addresses = userService.getFriendAddress(friendId);
        return success(message("user.get.friend.address.success"),addresses);
    }
    @GetMapping("/getMyAddress")
    @Operation(summary = "获取我的地址")
    public AjaxResult getMyAddress(){
        log.info("获取我的地址：{}",UserContext.getUserId());
        List<Address> addresses = userService.getMyAddress(UserContext.getUserId());
        return success(message("user.get.address.success"),addresses);
    }

    @GetMapping("/getMyFunctionCard")
    @Operation(summary = "获取我的功能卡")
    public AjaxResult getMyFunctionCard(){
        log.info("获取我的功能卡：{}",UserContext.getUserId());
        return success(message("user.get.function.card.success"),userService.getMyFunctionCard(UserContext.getUserId()));
    }

    @GetMapping("/getCountries")
    @Operation(summary = "获取国家列表")
    public AjaxResult getCountries(){
        log.info("获取国家列表");
        return success(message("user.get.countries.success"),userService.getCountries());
    }
}






