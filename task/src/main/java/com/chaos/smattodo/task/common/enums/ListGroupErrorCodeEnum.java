package com.chaos.smattodo.task.common.enums;

import com.chaos.smattodo.task.common.errorcode.IErrorCode;

public enum ListGroupErrorCodeEnum implements IErrorCode {

    LIST_GROUP_SORT_ORDER_ERROR("B000300", "清单分组排序值错误"),;

    private final String code;

    private final String message;

    ListGroupErrorCodeEnum(String code, String message) {
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
