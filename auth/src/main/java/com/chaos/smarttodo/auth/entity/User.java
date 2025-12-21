package com.chaos.smarttodo.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表实体
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /**
     * 密码通常在查询时排除，除非明确需要
     */
    @TableField(select = false)
    private String password;

    private String nickname;

    private String avatar;

    /**
     * 自动填充创建时间（需配置或手动设置）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}