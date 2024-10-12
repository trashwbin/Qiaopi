package com.qiaopi.exception.card;

import com.qiaopi.exception.base.BaseException;

/**
 * 功能卡异常类
 */
public class CardException extends BaseException {
    private static final long serialVersionUID = 1L;

    public CardException(String code, Object[] args)
    {
        super("card", code, args, null);
    }

    public CardException(String code) {
        super("card",code);
    }
}
