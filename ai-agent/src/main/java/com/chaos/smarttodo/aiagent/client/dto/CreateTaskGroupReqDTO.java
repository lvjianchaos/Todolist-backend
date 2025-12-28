package com.chaos.smarttodo.aiagent.client.dto;

import lombok.Data;

@Data
public class CreateTaskGroupReqDTO {
    private Long listId;
    private String name;
    private Long prevId;
    private Long nextId;
    private Double prevSortOrder;
}

