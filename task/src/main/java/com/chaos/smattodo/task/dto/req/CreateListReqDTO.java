package com.chaos.smattodo.task.dto.req;

import lombok.Data;

@Data
public class CreateListReqDTO {
    private Long groupId;
    private String name;

    private Double prevSortOrder;
}
