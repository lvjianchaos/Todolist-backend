package com.chaos.smarttodo.aiagent.client.dto;

import lombok.Data;

@Data
public class CreateListReqDTO {
    private Long groupId;
    private String name;
    private Double prevSortOrder;
}

