package com.chaos.smattodo.task.dto.req;

import lombok.Data;

@Data
public class MoveTaskReqDTO {

    private Long listId;

    private Long taskGroupId;

    private Long parentId;

    private Long prevId;

    private Long nextId;

    private Double prevSortOrder;

    private Double nextSortOrder;
}

