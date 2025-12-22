package com.chaos.smattodo.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 清单
 */
@Data
@TableName("list")
public class TodoList {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long listGroupId;
    private String name;
    private Double sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

