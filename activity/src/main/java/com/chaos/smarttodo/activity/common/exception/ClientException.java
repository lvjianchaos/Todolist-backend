package com.chaos.smarttodo.activity.common.exception;

import com.chaos.smarttodo.activity.common.errorcode.BaseErrorCode;
import com.chaos.smarttodo.activity.common.errorcode.IErrorCode;

/**
 * 客户端异常，表示请求在到达服务器前出现的问题，如参数错误、认证失败等
 */
public class ClientException extends AbstractException {

    public ClientException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public ClientException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public ClientException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "ClentException{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }
}
