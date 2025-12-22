package com.chaos.smattodo.task.dto.req;

import lombok.Data;

/**
 *
 */
@Data
public class ListGroupUpdateSortOrderReqDTO {
    private Long id;
    // 拖拽后，该元素前一个元素的 sortOrder，如果没有前一个元素，则为 0
    private Double prevSortOrder;
    // 拖拽后，该元素后一个元素的 sortOrder，如果没有后一个元素，则为 0
    private Double nextSortOrder;
}
