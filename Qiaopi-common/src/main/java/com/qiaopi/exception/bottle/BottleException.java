package com.qiaopi.exception.bottle;

import com.qiaopi.exception.base.BaseException;

/**
 * 漂流瓶异常
 */
public class BottleException extends BaseException {
    private static final long serialVersionUID = 1L;

    public BottleException(String code,Object[] args)
    {
        super("bottle", code, args, null);
    }

    public BottleException(String code) {
        super("bottle",code);
    }

}

