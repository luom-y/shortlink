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
    SUCCESS(200, "成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权/未登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    TOKEN_EXPIRED(1003, "Token已过期"),
    TOKEN_INVALID(1004, "Token无效"),

    SHORTLINK_NOT_FOUND(2001, "短链接不存在"),
    SHORTLINK_EXPIRED(2002, "短链接已过期"),
    SHORTLINK_GENERATE_FAILED(2003, "短链接生成失败"),

    RATE_LIMIT_EXCEEDED(3001, "请求过于频繁（限流）");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
