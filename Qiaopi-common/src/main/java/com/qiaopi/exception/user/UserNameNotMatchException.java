package com.qiaopi.exception.user;

/**
 * 用户密码不正确或不符合规范异常类
 *
 */
public class UserNameNotMatchException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserNameNotMatchException()
    {
        super("user.name.not.match", null);
    }
}
