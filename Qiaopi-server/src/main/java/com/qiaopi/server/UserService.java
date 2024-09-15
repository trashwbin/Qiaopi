package com.qiaopi.server;

import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.vo.UserLoginVO;

public interface UserService {

    UserLoginVO login(UserLoginDTO userLoginDTO);

}
