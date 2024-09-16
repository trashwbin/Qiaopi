package com.qiaopi.exception.code;

/**
 * 用户不存在异常类
 *
 */
public class CodeTimeoutException extends CodeException
{
    private static final long serialVersionUID = 1L;

    public CodeTimeoutException()
    {
        super("user.code.expire", null);
    }
}
