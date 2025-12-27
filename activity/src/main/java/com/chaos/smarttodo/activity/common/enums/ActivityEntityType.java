package com.chaos.smarttodo.activity.common.enums;

import lombok.Getter;

@Getter
public enum ActivityEntityType {

    LIST_GROUP(0),
    LIST(1),
    TASK_GROUP(2),
    TASK(3);

    private final int code;

    ActivityEntityType(int code) {
        this.code = code;
    }
}

