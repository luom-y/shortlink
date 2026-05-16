package com.shortlink.common.result;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Unified API response wrapper.
 *
 * <p>Usage:
 * <pre>{@code
 *   Result.success(data);                    // 200 with data
 *   Result.success();                         // 200 without data
 *   Result.fail(ResultCode.BAD_REQUEST);      // 400
 *   Result.fail(ResultCode.USER_NOT_FOUND);   // 1001
 * }</pre>
 */
@Getter
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer code;
    private final String message;
    private final T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ---- success factories ----

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ---- fail factories ----

    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
