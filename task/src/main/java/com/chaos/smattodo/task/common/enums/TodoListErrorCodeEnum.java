package com.chaos.smattodo.task.common.enums;

import com.chaos.smattodo.task.common.errorcode.IErrorCode;

public enum TodoListErrorCodeEnum implements IErrorCode {

    TODO_LIST_SORT_ORDER_ERROR("B000400", "清单排序值错误"),

    TODO_LIST_NOT_FOUND("B000401", "清单不存在"),

    TODO_LIST_NO_PERMISSION("B000402", "无权限操作该清单");

    private final String code;

    private final String message;

    TodoListErrorCodeEnum(String code, String message) {
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

