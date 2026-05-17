package com.shortlink.common.utils;

import cn.hutool.crypto.digest.BCrypt;

public final class BCryptEncoder {

    private BCryptEncoder() {}

    public static String encode(CharSequence rawPassword) {
        return BCrypt.hashpw(rawPassword.toString());
    }

    public static boolean matches(CharSequence rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }
}
