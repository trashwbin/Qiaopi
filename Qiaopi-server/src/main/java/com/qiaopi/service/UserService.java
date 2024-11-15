package com.qiaopi.service;

import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.dto.UserResetPasswordDTO;
import com.qiaopi.dto.UserUpdateDTO;
import com.qiaopi.entity.*;
import com.qiaopi.vo.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface UserService {

    UserLoginVO login(UserLoginDTO userLoginDTO);

    String register(UserRegisterDTO userRegisterDTO);

    void resetPasswordByEmail(UserResetPasswordDTO userResetPasswordDTO);

    UserVO getUserInfo(Long userId);

    ConcurrentHashMap<String, List> getUserRepository(Long userId);

    void updateUsername(UserUpdateDTO userUpdateDTO);

    void updatePassword(UserUpdateDTO userUpdateDTO);

    void updateUserInfo(UserUpdateDTO userUpdateDTO);

    Long getUserMoney(Long userId);

    List<FriendVO> getMyFriends(Long userId);

    List<Address> getMyAddress(Long userId);

    List<Address> getFriendAddress(Long friendId);

    void sendResetPasswordCode(String email);

    Map<String, String> getCode();

    void sendCode(String email);

    List<FunctionCardVO> getMyFunctionCard(Long userId);

    List<Avatar> getAvatarList();

    List<Country> getCountries();

    void setUserDefaultAddress(Long addressId);

    void deleteUserAddress(Long addressId);

    void setFriendDefaultAddress(Long friendId, Long addressId);

    void deleteFriendAddress(Long friendId, Long addressId);

    void updateFriendRemark(Long friendId, String remark);

    void sign(Long userId);

    List<TaskTable> getTask(Long userId);


    ConcurrentHashMap getSignList(Long userId);

    UserStatistics getUserStatistics(Long userId);

    void finishTask(Long taskId);
}
