package com.qiaopi.exception.code;

import com.qiaopi.exception.base.BaseException;

/**
 * 用户信息异常类

 */
public class CodeException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public CodeException(String code, Object[] args)
    {
        super("code", code, args, null);
    }
}
