package com.chaos.smarttodo.aiagent.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListGroupRespDTO {
    private Long id;
    private String name;
    private Double sortOrder;
    private List<TodoListRespDTO> list;
}

