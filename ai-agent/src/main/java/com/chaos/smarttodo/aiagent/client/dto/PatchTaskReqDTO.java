package com.chaos.smarttodo.aiagent.client.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatchTaskReqDTO {
    private String name;
    private String content;
    private LocalDate startedAt;
    private LocalDate dueAt;
    private Integer status;
    private Integer priority;
}

