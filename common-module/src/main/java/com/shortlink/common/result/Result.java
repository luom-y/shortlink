package com.shortlink.common.result;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应体。
 * @param <T> 数据类型
 */
@Getter
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Integer SUCCESS_CODE = 200;

    private final Integer code;
    private final String message;
    private final T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, null, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, null, data);
    }

    public static <T> Result<T> fail(ResultCode code) {
        return new Result<>(code.getCode(), code.getMessage(), null);
    }

    public static <T> Result<T> fail(ResultCode code, String message) {
        return new Result<>(code.getCode(), message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }
}
