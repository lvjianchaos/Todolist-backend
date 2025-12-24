package com.chaos.smattodo.task.dto.req;

import lombok.Data;

@Data
public class CreateListGroupReqDTO {
    private String name;

    private Double prevSortOrder;
}
