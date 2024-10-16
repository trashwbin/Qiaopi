package com.qiaopi.exception.paper;

import com.qiaopi.exception.base.BaseException;

/**
 * 功能卡异常类
 */
public class PaperException extends BaseException {
    private static final long serialVersionUID = 1L;

    public PaperException(String code, Object[] args)
    {
        super("card", code, args, null);
    }

    public PaperException(String code) {
        super("paper",code);
    }
}
