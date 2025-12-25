package com.chaos.smattodo.task.dto.resp;

import lombok.Data;

@Data
public class TaskGroupRespDTO {

    private Long id;

    private Long listId;

    private String name;

    private Double sortOrder;

    private Integer isDefault;
}

