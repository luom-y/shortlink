package com.shortlink.shortlink.constants;

public final class RedisConstants {

    private RedisConstants() {}

    /** Short link cache prefix: shortlink:cache:{shortCode} -> originalUrl */
    public static final String SHORTLINK_CACHE = "shortlink:cache:";

    /** Short link click count prefix: shortlink:clicks:{shortCode} */
    public static final String SHORTLINK_CLICKS = "shortlink:clicks:";

    /** Null value placeholder for cache penetration prevention (TTL shorter than normal) */
    public static final String NULL_PLACEHOLDER = "__NULL__";
}
