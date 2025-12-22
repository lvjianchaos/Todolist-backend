package com.chaos.smattodo.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务分组
 */
@Data
@TableName("task_group")
public class TaskGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long listId;
    private String name;
    private Double sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
