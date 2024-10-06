package com.qiaopi.exception.user;

/**
 * 用户密码不正确或不符合规范异常类
 *
 */
public class UserConfirmPasswordNotEqualsException extends UserException
{
    private static final long serialVersionUID = 1L;

    public UserConfirmPasswordNotEqualsException()
    {
        super("user.password.confirm.error", null);
    }
}
