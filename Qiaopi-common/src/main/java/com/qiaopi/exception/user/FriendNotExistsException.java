package com.qiaopi.exception.user;

/**
 * 用户不存在异常类
 *
 */
public class FriendNotExistsException extends UserException
{
    private static final long serialVersionUID = 1L;

    public FriendNotExistsException()
    {
        super("user.friend.not.exists", null);
    }
}
