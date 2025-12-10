package com.chaos.smarttodo.common.result;

import com.chaos.smarttodo.common.constant.ResultCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private int code;
    private boolean success;
    private String msg;
    private T data;
    private long timestamp;

    private R(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.success = code == 200;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> R<T> success(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), data, ResultCode.SUCCESS.getMsg());
    }

    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, null, msg);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), null, resultCode.getMsg());
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.FAILURE.getCode(), null, msg);
    }
}