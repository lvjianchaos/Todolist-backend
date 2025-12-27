package com.chaos.smattodo.task.activity.enums;

import lombok.Getter;

@Getter
public enum ActivityAction {

    CREATE(0),
    DELETE(1),
    COMPLETE(2),
    RENAME(3);

    private final int code;

    ActivityAction(int code) {
        this.code = code;
    }
}
