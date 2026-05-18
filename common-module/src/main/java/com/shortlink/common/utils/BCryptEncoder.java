package com.shortlink.common.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * BCrypt密码加密工具（基于Hutool）。
 */
public final class BCryptEncoder {

    private BCryptEncoder() {}

    /**
     * 加密明文密码。
     */
    public static String encode(CharSequence rawPassword) {
        return BCrypt.hashpw(rawPassword.toString());
    }

    /**
     * 校验明文密码与密文是否匹配。
     */
    public static boolean matches(CharSequence rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }
}
