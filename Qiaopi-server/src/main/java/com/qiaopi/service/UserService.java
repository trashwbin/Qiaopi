package com.qiaopi.service;

import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.dto.UserRegisterDTO;
import com.qiaopi.dto.UserResetPasswordDTO;
import com.qiaopi.dto.UserUpdateDTO;
import com.qiaopi.entity.Address;
import com.qiaopi.entity.Avatar;
import com.qiaopi.entity.Country;
import com.qiaopi.vo.FriendVO;
import com.qiaopi.vo.FunctionCardVO;
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
}
