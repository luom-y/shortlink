package com.shortlink.common.result;

import lombok.Getter;

/**
 * Standardized API result codes.
 *
 * <p>HTTP-aligned codes for infrastructure; 1xxx+ for business domain errors.
 */
@Getter
public enum ResultCode {

    // ---- Success ----
    SUCCESS(200, "Success"),

    // ---- Client errors ----
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),

    // ---- Server errors ----
    INTERNAL_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),

    // ---- Business: User (1xxx) ----
    USER_NOT_FOUND(1001, "User not found"),
    PASSWORD_ERROR(1002, "Incorrect password"),
    TOKEN_EXPIRED(1003, "Token expired"),
    TOKEN_INVALID(1004, "Invalid token"),

    // ---- Business: ShortLink (2xxx) ----
    SHORTLINK_NOT_FOUND(2001, "Short link not found"),
    SHORTLINK_EXPIRED(2002, "Short link has expired"),
    SHORTLINK_GENERATE_FAILED(2003, "Short link generation failed"),

    // ---- Business: Rate Limit (3xxx) ----
    RATE_LIMIT_EXCEEDED(3001, "Rate limit exceeded");

    private final Integer code;
    private final String message;
}
