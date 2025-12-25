package com.chaos.smattodo.task.dto.req;

import lombok.Data;

@Data
public class ReorderTaskGroupsReqDTO {

    private Long listId;

    private Long movedId;

    private Long prevId;

    private Long nextId;

    private Double prevSortOrder;

    private Double nextSortOrder;
}
