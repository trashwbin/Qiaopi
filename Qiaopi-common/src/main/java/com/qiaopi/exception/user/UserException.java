package com.qiaopi.exception.user;

import com.qiaopi.exception.base.BaseException;

/**
 * 用户信息异常类

 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }

    public UserException(String code) {
        super("user",code);
    }
}
