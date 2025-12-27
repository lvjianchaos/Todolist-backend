package com.chaos.smarttodo.activity.common.exception;

import com.chaos.smarttodo.activity.common.errorcode.IErrorCode;
import lombok.Getter;

/**
 * 抽象异常类，所有自定义异常均继承此类
 * @see ClientException
 * @see ServiceException
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message,throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = errorCode.message();
    }
}
