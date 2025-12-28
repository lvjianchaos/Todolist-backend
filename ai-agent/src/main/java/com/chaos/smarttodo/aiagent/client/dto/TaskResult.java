package com.chaos.smarttodo.aiagent.client.dto;

import lombok.Data;

@Data
public class TaskResult<T> {
    private String code;
    private String message;
    private T data;

    public boolean isSuccess() {
        return "0".equals(code);
    }
}
