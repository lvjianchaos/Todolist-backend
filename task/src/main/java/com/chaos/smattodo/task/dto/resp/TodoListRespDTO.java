package com.chaos.smattodo.task.dto.resp;

import lombok.Data;

@Data
public class TodoListRespDTO {
    private Long id;
    private Long groupId;
    private String name;
    private Double sortOrder;
}

