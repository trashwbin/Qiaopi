package com.qiaopi.server.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiaopi.constant.JwtClaimsConstant;
import com.qiaopi.dto.UserLoginDTO;
import com.qiaopi.entity.User;
import com.qiaopi.exception.user.UserNameNotMatchException;
import com.qiaopi.exception.user.UserNotExistsException;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.properties.JwtProperties;
import com.qiaopi.server.UserService;
import com.qiaopi.utils.AccountValidator;
import com.qiaopi.utils.JwtUtil;
import com.qiaopi.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private  UserMapper userMapper;
    @Autowired
    private JwtProperties jwtProperties;


    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {

        //TODO 检验验证码


        // 根据用户名和密码查询用户信息
        if (!AccountValidator.isValidAccount(userLoginDTO.getUsername())) {
            //用户名不匹配
            throw new UserNameNotMatchException();
        }

        //为了方便查询，将用户名和密码封装到User对象中
        User userLogin = User.builder().password(userLoginDTO.getPassword()).build();

        //TODO 对前端传过来的明文密码进行md5或其他加密处理
//        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (AccountValidator.isValidEmail(userLoginDTO.getUsername())) {
            userLogin.setEmail(userLoginDTO.getUsername());
        } else {
            userLogin.setUserName(userLoginDTO.getUsername());
        }

        //根据用户名和密码查询用户信息的条件构造器
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (userLogin.getUserName() != null) {
            wrapper
                    .eq(User::getUserName, userLogin.getUserName())
                    .eq(User::getPassword, userLogin.getPassword());
        } else {
            wrapper
                    .eq(User::getEmail, userLogin.getEmail())
                    .eq(User::getPassword, userLogin.getPassword());
        }
        //根据条件查询用户信息,mybatis-plus会自动查询，封装到User对象中
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            //账号不存在
            throw new UserNotExistsException();
        }

        //TODO 登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .token(token)
                .sex(user.getSex())
                .avatar(user.getAvatar())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .build();

        return userLoginVO;
    }
}
