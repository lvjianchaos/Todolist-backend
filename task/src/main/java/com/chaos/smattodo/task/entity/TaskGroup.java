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

    /**
     * 是否默认分组: 0-否, 1-是
     */
    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
