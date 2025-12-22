package com.chaos.smattodo.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务
 */
@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long listId;
    private Long taskGroupId;
    private Long parentId;
    private String path;
    private Integer level;
    private String name;
    private String content;
    private Double sortOrder;
    private Integer status; // 0-待办, 1-完成, 2-过期
    private Integer priority; // 0-无, 1-低, 2-中, 3-高
    private LocalDateTime dueAt;
    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}