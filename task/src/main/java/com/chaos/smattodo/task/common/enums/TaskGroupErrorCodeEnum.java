package com.chaos.smattodo.task.common.enums;

import com.chaos.smattodo.task.common.errorcode.IErrorCode;

public enum TaskGroupErrorCodeEnum implements IErrorCode {

    TASK_GROUP_SORT_ORDER_ERROR("B000500", "任务分组排序值错误"),

    TASK_GROUP_NOT_FOUND("B000501", "任务分组不存在"),

    TASK_GROUP_NO_PERMISSION("B000502", "无权限操作该任务分组"),

    TASK_GROUP_DEFAULT_CANNOT_DELETE("B000503", "默认任务分组不可删除");

    private final String code;

    private final String message;

    TaskGroupErrorCodeEnum(String code, String message) {
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

