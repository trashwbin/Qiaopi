package com.qiaopi.exception.friend;

import com.qiaopi.exception.base.BaseException;

public class FriendException extends BaseException {
    private static final long serialVersionUID = 1L;

    public FriendException(String code, Object[] args)
    {
        super("friend", code, args, null);
    }

    public FriendException(String code) {
        super("friend",code);
    }}
