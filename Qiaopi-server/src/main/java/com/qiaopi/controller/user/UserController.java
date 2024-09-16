package com.qiaopi.controller.user;


import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.server.UserService;
import com.qiaopi.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import static com.qiaopi.result.AjaxResult.success;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

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
}
