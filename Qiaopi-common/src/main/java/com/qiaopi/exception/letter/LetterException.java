package com.qiaopi.exception.letter;

import com.qiaopi.exception.base.BaseException;

/**
 * 信件异常类
 */
public class LetterException extends BaseException {
    private static final long serialVersionUID = 1L;

    public LetterException(String code, Object[] args)
    {
        super("letter", code, args, null);
    }

    public LetterException(String code) {
        super("letter",code);
    }
}
