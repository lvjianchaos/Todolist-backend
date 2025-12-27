package com.chaos.smarttodo.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_log")
public class ActivityLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;

    private Integer entityType;
    private Integer action;

    private Long listGroupId;
    private String lgName;

    private Long listId;
    private String listName;

    private Long tgId;
    private String tgName;

    private Long taskId;
    private String taskName;

    private String summary;
    private String extraData;

    private LocalDateTime createdAt;
}

