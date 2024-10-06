package com.qiaopi.service;

import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.dto.UserResetPasswordDTO;
import com.qiaopi.dto.UserUpdateDTO;
import com.qiaopi.vo.UserLoginVO;
import com.qiaopi.vo.UserVO;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserLoginVO login(UserLoginDTO userLoginDTO);

    String register(UserRegisterDTO userRegisterDTO);

    void resetPasswordByEmail(UserResetPasswordDTO userResetPasswordDTO);

    UserVO getUserInfo(Long userId);

    Map<String, List> getUserRepository(Long userId);

    void updateUsername(UserUpdateDTO userUpdateDTO);

    void updatePassword(UserUpdateDTO userUpdateDTO);

    void updateUserInfo(UserUpdateDTO userUpdateDTO);

    Long getUserMoney(Long userId);
}
