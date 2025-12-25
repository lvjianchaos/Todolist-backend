package com.chaos.smattodo.task.dto.req;

import lombok.Data;

@Data
public class CreateTaskReqDTO {

    private Long listId;

    private Long taskGroupId;

    private Long parentId;

    private String name;

    private Long prevId;

    private Long nextId;

    private Double prevSortOrder;
}

