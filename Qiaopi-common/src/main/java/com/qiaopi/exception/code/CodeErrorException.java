package com.qiaopi.exception.code;

/**
 * 用户不存在异常类
 *
 */
public class CodeErrorException extends CodeException
{
    private static final long serialVersionUID = 1L;

    public CodeErrorException()
    {
        super("user.code.error", null);
    }
}
