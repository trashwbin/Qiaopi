package com.qiaopi.exception;

import com.qiaopi.exception.base.BaseException;

/**
 * 问题游戏异常
 */
public class QuestionException extends BaseException {
    private static final long serialVersionUID = 1L;

    public QuestionException(String code,Object[] args)
    {
        super("question", code, args, null);
    }

    public QuestionException(String code) {
        super("question",code);
    }}
