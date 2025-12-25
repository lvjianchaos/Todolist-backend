-- 创建数据库
CREATE DATABASE IF NOT EXISTS `smart_todo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `smart_todo`;

-- 1. 用户表
CREATE TABLE `user` (
                        `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                        `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                        `password` VARCHAR(100) NOT NULL COMMENT '加密存储的密码',
                        `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称',
                        `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
) ENGINE=InnoDB COMMENT='用户表';

-- 2. 清单分组表
CREATE TABLE `list_group` (
                              `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                              `user_id` BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
                              `name` VARCHAR(100) NOT NULL COMMENT '分组名称',
                              `sort_order` DOUBLE NOT NULL DEFAULT 0 COMMENT '排序值',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              INDEX `idx_user_sort` (`user_id`, `sort_order`)
) ENGINE=InnoDB COMMENT='清单分组表';

-- 3. 清单表
CREATE TABLE `list` (
                        `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                        `user_id` BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
                        `list_group_id` BIGINT UNSIGNED NOT NULL COMMENT '所属清单分组ID',
                        `name` VARCHAR(100) NOT NULL COMMENT '清单名称',
                        `sort_order` DOUBLE NOT NULL DEFAULT 0 COMMENT '排序值',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        INDEX `idx_group_sort` (`list_group_id`, `sort_order`),
                        INDEX `idx_user_list` (`user_id`)
) ENGINE=InnoDB COMMENT='清单表';

-- 4. 任务分组表
CREATE TABLE `task_group` (
                              `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                              `list_id` BIGINT UNSIGNED NOT NULL COMMENT '所属清单ID',
                              `name` VARCHAR(100) NOT NULL COMMENT '任务分组名称',
                              `sort_order` DOUBLE NOT NULL DEFAULT 0 COMMENT '排序值',
                              `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认分组: 0-否, 1-是',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              INDEX `idx_list_sort` (`list_id`, `sort_order`)
) ENGINE=InnoDB COMMENT='任务分组表';

-- 5. 任务表
CREATE TABLE `task` (
                        `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                        `user_id` BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID (隔离校验)',
                        `list_id` BIGINT UNSIGNED NOT NULL COMMENT '所属清单ID (核心查询维度)',
                        `task_group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属任务分组ID',
                        `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '直接父ID (顶级为NULL)',
                        `path` VARCHAR(512) NOT NULL DEFAULT '' COMMENT '路径枚举,如 "0/1/5/"',
                        `level` INT NOT NULL DEFAULT 1 COMMENT '递归深度,1为根任务',
                        `name` VARCHAR(255) NOT NULL COMMENT '任务标题',
                        `content` TEXT COMMENT '任务详情',
                        `sort_order` DOUBLE NOT NULL DEFAULT 0 COMMENT '同级排序值 (双精度取中算法)',
                        `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待办, 1-完成, 2-已过期',
                        `priority` TINYINT NOT NULL DEFAULT 0 COMMENT '优先级: 0-无, 1-低, 2-中, 3-高',
                        `started_at` DATE DEFAULT NULL COMMENT '开始时间',
                        `due_at` DATE DEFAULT NULL COMMENT '截止时间',
                        `completed_at` DATETIME DEFAULT NULL COMMENT '完成时间',
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 核心组合索引：优化清单内任务展示
                        INDEX `idx_query_composite` (`list_id`, `task_group_id`, `sort_order`),
    -- 索引：优化子任务懒加载查询
                        INDEX `idx_parent_id` (`parent_id`),
    -- 索引：优化路径查询（前缀索引）
                        INDEX `idx_path_prefix` (`path`(255)),
    -- 索引：优化过期任务扫描或个人任务聚合
                        INDEX `idx_user_status_due` (`user_id`, `status`, `due_at`)
) ENGINE=InnoDB COMMENT='任务表';

-- 6. 活动日志表
CREATE TABLE `activity_log` (
                                `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                                `user_id` BIGINT UNSIGNED NOT NULL COMMENT '执行用户ID',
                                `entity_id` BIGINT UNSIGNED NOT NULL COMMENT '操作实体ID',
                                `entity_type` TINYINT NOT NULL COMMENT '实体类型: 0-分组, 1-清单, 2-任务组, 3-任务',
                                `action` TINYINT NOT NULL COMMENT '操作类型: 0-创建, 1-删除, 2-完成, 3-修改, 4-移动',
                                `summary` VARCHAR(255) DEFAULT NULL COMMENT '操作简述快照',
                                `extra_data` JSON DEFAULT NULL COMMENT '详细快照或差异数据 (JSON格式)',
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志记录时间',
                                INDEX `idx_user_activity` (`user_id`, `created_at`),
                                INDEX `idx_entity_log` (`entity_id`, `entity_type`)
) ENGINE=InnoDB COMMENT='操作日志表';