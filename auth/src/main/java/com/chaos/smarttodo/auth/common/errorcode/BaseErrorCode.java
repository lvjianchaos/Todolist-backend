package com.chaos.smarttodo.auth.common.errorcode;

public enum BaseErrorCode implements IErrorCode {
    /**
     * 通用错误码
     */
    SUCCESS("200", "成功"),
    UNKNOWN_ERROR("500", "未知错误"),
    INVALID_REQUEST("400", "无效请求"),
    UNAUTHORIZED("401", "未授权"),
    FORBIDDEN("403", "禁止访问"),
    NOT_FOUND("404", "资源未找到"),
    INTERNAL_SERVER_ERROR("500", "服务器内部错误"),

    // 一级宏观错误码 客户端错误
    CLIENT_ERROR("A000001", "客户端错误"),
    // 一级宏观错误码 服务器错误
    SERVICE_ERROR("B000001", "服务器错误");

    private final String code;
    private final String message;

    BaseErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
