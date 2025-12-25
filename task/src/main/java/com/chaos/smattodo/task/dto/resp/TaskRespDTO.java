package com.chaos.smattodo.task.dto.resp;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskRespDTO {

    private Long id;
    private Long userId;
    private Long listId;
    private Long taskGroupId;
    private Long parentId;

    private String name;
    private String content;
    private Double sortOrder;

    /**
     * 0-待办, 1-完成, 2-过期
     */
    private Integer status;

    /**
     * 0-无, 1-低, 2-中, 3-高
     */
    private Integer priority;

    private LocalDate startedAt;
    private LocalDate dueAt;
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 是否存在子任务（懒加载场景）
     */
    private Boolean hasChildren;
}

