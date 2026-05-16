package com.shortlink.common.result;

public final class Results {



        private static final String DEFAULT_ERROR_CODE = "500";
        private static final String DEFAULT_ERROR_MESSAGE = "系统异常";

        private static Result<Void> buildFailure(String code, String message) {
            return new Result<Void>()
                    .setCode(code)
                    .setMessage(message);
        }
        /**
         * 成功（无数据）
         */
        public static Result<Void> success() {
            return new Result<Void>()
                    .setCode(Result.SUCCESS_CODE);
        }

        /**
         * 成功（有数据）
         */
        public static <T> Result<T> success(T data) {
            return new Result<T>()
                    .setCode(Result.SUCCESS_CODE)
                    .setData(data);
        }

        /**
         * 默认失败
         */
        public static Result<Void> failure() {
            return buildFailure(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE);
        }

        /**
         * 自定义失败
         */
        public static Result<Void> failure(String errorCode, String errorMessage) {
            return buildFailure(
                    errorCode != null ? errorCode : DEFAULT_ERROR_CODE,
                    errorMessage != null ? errorMessage : DEFAULT_ERROR_MESSAGE
            );
        }
    }

