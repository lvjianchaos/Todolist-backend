package com.chaos.smarttodo.activity.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityLogRespDTO {

    private Long id;
    private Integer entityType;
    private Integer action;

    private Long listId;
    private String listName;

    private Long listGroupId;
    private String lgName;

    private Long tgId;
    private String tgName;

    private Long taskId;
    private String taskName;

    private String summary;
    private String extraData;

    /**
     * 前端可只显示年月日，但需要完整时间做排序
     */
    private LocalDateTime createdAt;
}

