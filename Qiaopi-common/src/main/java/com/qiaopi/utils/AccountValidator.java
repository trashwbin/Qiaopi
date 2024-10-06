package com.qiaopi.utils;

import java.util.regex.Pattern;

/**
 * 账号验证工具类
 */
public class AccountValidator {

    // 用户名正则, x位（字母，数字，下划线，减号）
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    public static boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidAccount(String account) {
        return isValidUsername(account) || isValidEmail(account);
    }
}