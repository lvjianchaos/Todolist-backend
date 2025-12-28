package com.chaos.smarttodo.aiagent.client.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRespDTO {
    private Long id;
    private Long userId;
    private Long listId;
    private Long taskGroupId;
    private Long parentId;

    private String name;
    private String content;

    private Integer status;
    private Integer priority;

    private LocalDate startedAt;
    private LocalDate dueAt;
}

