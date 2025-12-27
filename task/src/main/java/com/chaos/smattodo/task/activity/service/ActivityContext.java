package com.chaos.smattodo.task.activity.service;

import lombok.Builder;
import lombok.Data;

/**
 * 冗余快照上下文：不连表，保证前端展示与跳转所需信息齐全。
 */
@Data
@Builder
public class ActivityContext {

    private String username;

    private Long listGroupId;
    private String lgName;

    private Long listId;
    private String listName;

    private Long taskGroupId;
    private String taskGroupName;

    private Long taskId;
    private String taskName;

    /**
     * 简述，前端可直接用
     */
    private String summary;

    /**
     * JSON 字符串（例如 {"oldName":"A","newName":"B"}）
     */
    private String extraData;
}
