package com.qiaopi.controller.user;


import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.entity.User;
import com.qiaopi.result.AjaxResult;
import com.qiaopi.server.UserService;
import com.qiaopi.server.impl.UserServiceImpl;
import com.qiaopi.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.qiaopi.result.AjaxResult.success;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
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
    @ApiOperation(value = "员工登录")
    public AjaxResult login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("员工登录：{}", userLoginDTO);

        UserLoginVO userLoginVO = userService.login(userLoginDTO);

        return success(userLoginVO);
    }
}
