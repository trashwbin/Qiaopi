package com.qiaopi.exception.font;

import com.qiaopi.exception.base.BaseException;

/**
 * 功能卡异常类
 */
public class FontException extends BaseException {
    private static final long serialVersionUID = 1L;

    public FontException(String code, Object[] args)
    {
        super("card", code, args, null);
    }

    public FontException(String code) {
        super("font",code);
    }
}
