package com.chaos.smattodo.task.dto.req;

import lombok.Data;

@Data
public class CreateTaskGroupReqDTO {

    private Long listId;

    private String name;

    private Long prevId;

    private Long nextId;

    /**
     * 前端传入的前一个元素 sortOrder（用于快速计算新 sortOrder）
     */
    private Double prevSortOrder;
}

