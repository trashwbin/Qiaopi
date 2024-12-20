package com.qiaopi.constant;

/**
 * 用户常量信息
 *
 */
public class UserConstants
{
    /**
     * 平台内系统用户的唯一标志
     */
    public static final String SYS_USER = "SYS_USER";

    /** 正常状态 */
    public static final String NORMAL = "0";

    /** 异常状态 */
    public static final String EXCEPTION = "1";

    /** 用户封禁状态 */
    public static final String USER_DISABLE = "1";


    /** 校验是否唯一的返回标识 */
    public final static boolean UNIQUE = true;
    public final static boolean NOT_UNIQUE = false;

    /**
     * 用户名长度限制
     */
    public static final int USERNAME_MIN_LENGTH = 6;
    public static final int USERNAME_MAX_LENGTH = 20;

    /**
     * 密码长度限制
     */
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 20;
}
