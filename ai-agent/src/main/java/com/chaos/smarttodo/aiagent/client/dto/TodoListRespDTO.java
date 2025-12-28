package com.chaos.smarttodo.aiagent.client.dto;

import lombok.Data;

@Data
public class TodoListRespDTO {
    private Long id;
    private Long groupId;
    private String name;
    private Double sortOrder;
}

