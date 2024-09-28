package com.qiaopi.exception.user;

/**
 * 用户不存在异常类
 *
 */
public class UserLoginNotExistsException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserLoginNotExistsException()
    {
        super("user.login.not.exists", null);
    }
}
