package com.qiaopi.service;

import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.vo.UserLoginVO;

public interface UserService {

    UserLoginVO login(UserLoginDTO userLoginDTO);

    String register(UserRegisterDTO userRegisterDTO);

    void resetPasswordByEmail(UserRegisterDTO userRegisterDTO);
}