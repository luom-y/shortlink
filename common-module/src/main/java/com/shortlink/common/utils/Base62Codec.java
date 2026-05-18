package com.shortlink.common.utils;

/**
 * Base62编解码器，用于短码生成。
 * 字符集: 0-9, A-Z, a-z（共62个字符）。
 */
public final class Base62Codec {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    private Base62Codec() {}

    /**
     * 将数字编码为Base62字符串。
     */
    public static String encode(long number) {
        if (number < 0) {
            throw new IllegalArgumentException("number must be non-negative");
        }
        if (number == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % BASE);
            sb.append(ALPHABET.charAt(remainder));
            number /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * 将Base62字符串解码为数字。
     */
    public static long decode(String base62) {
        if (base62 == null || base62.isEmpty()) {
            throw new IllegalArgumentException("base62 string must not be empty");
        }
        long result = 0;
        for (int i = 0; i < base62.length(); i++) {
            char c = base62.charAt(i);
            int idx = ALPHABET.indexOf(c);
            if (idx < 0) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            result = result * BASE + idx;
        }
        return result;
    }
}
