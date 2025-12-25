package com.chaos.smattodo.task.common.enums;

import com.chaos.smattodo.task.common.errorcode.IErrorCode;

public enum TaskErrorCodeEnum implements IErrorCode {

    TASK_SORT_ORDER_ERROR("B000600", "任务排序值错误"),

    TASK_NOT_FOUND("B000601", "任务不存在"),

    TASK_NO_PERMISSION("B000602", "无权限操作该任务"),

    TASK_MOVE_TARGET_ERROR("B000603", "任务移动目标不合法");

    private final String code;

    private final String message;

    TaskErrorCodeEnum(String code, String message) {
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

