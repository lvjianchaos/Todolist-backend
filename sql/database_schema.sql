-- 数据库设计方案 (MySQL 8.0+)
-- 核心设计思路：严格遵循 4 层级结构：清单分组 -> 清单 -> 任务分组 -> 任务 (含子任务)

CREATE DATABASE IF NOT EXISTS smart_todo DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_todo;

-- ==========================================
-- 第一层级：清单分组 (List Group)
-- ==========================================
-- 用户侧边栏的一级分类，如“自建清单分组”、“默认清单分组”
CREATE TABLE `list_group` (
                              `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              `user_id` BIGINT NOT NULL COMMENT '所属用户',
                              `name` VARCHAR(64) NOT NULL COMMENT '分组名称',

    -- 业务规则：每个用户注册时，系统自动创建一个 is_default=1 的分组（如“我的清单”）。
    -- 该分组不可被用户删除，不可改名。
                              `is_default` BOOLEAN DEFAULT FALSE COMMENT '是否为系统默认分组',

                              `sort_order` INT DEFAULT 0 COMMENT '排序权重，用于侧边栏拖拽排序',
                              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              INDEX idx_user (`user_id`)
) COMMENT '清单分组表';

-- ==========================================
-- 第二层级：清单 (Todo List)
-- ==========================================
-- 侧边栏的具体清单，如“购物清单”、“工作项目”
CREATE TABLE `todo_list` (
                             `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                             `user_id` BIGINT NOT NULL,
                             `group_id` BIGINT UNSIGNED NOT NULL COMMENT '归属的清单分组ID (FK: list_group.id)',
                             `name` VARCHAR(64) NOT NULL,

    -- 业务规则：系统初始化时创建的“默认清单（Inbox）”标记为 true。
    -- 该清单不可删除。新建任务时若未指定清单，默认落入此清单。
                             `is_default` BOOLEAN DEFAULT FALSE COMMENT '是否为默认清单(Inbox)',

                             `sort_order` INT DEFAULT 0 COMMENT '用于在分组内排序',
                             `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             FOREIGN KEY (`group_id`) REFERENCES `list_group`(`id`) ON DELETE CASCADE
) COMMENT '清单表';

-- ==========================================
-- 第三层级：任务分组 (Task Group)
-- ==========================================
-- 清单内部的区域划分，如“待处理”、“进行中”。
-- 业务规则：
-- 1. 任何清单（包括系统默认清单和用户自建清单）被创建时，必须自动创建一个 is_default=true 的任务分组。
-- 2. is_default=true 的任务分组不可被删除。
CREATE TABLE `task_group` (
                              `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              `list_id` BIGINT UNSIGNED NOT NULL COMMENT '所属清单ID (FK: todo_list.id)',
                              `name` VARCHAR(64) NOT NULL DEFAULT '默认分组',

    -- 新增字段：用于标记该分组是否为该清单的初始分组，前端根据此字段禁止删除按钮
                              `is_default` BOOLEAN DEFAULT FALSE COMMENT '是否为该清单的默认分组',

                              `sort_order` INT DEFAULT 0 COMMENT '看板列排序',
                              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              FOREIGN KEY (`list_id`) REFERENCES `todo_list`(`id`) ON DELETE CASCADE
) COMMENT '任务分组表';

-- ==========================================
-- 第四层级：任务 (Task)
-- ==========================================
-- 具体的任务项，支持无限子任务
CREATE TABLE `task` (
                        `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        `user_id` BIGINT NOT NULL,
                        `group_id` BIGINT UNSIGNED NOT NULL COMMENT '所属任务分组ID (FK: task_group.id)',
                        `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父任务ID，实现无限层级 (FK: task.id)',

    -- 冗余字段：方便在“全部任务”视图中快速查询，避免多级 Join
                        `list_id` BIGINT UNSIGNED NOT NULL COMMENT '所属清单ID (冗余)',

                        `title` VARCHAR(255) NOT NULL,
                        `description` TEXT,
                        `priority` TINYINT DEFAULT 0 COMMENT '0:无, 1:低, 2:中, 3:高',
                        `status` TINYINT DEFAULT 0 COMMENT '0:未完成, 1:已完成',

                        `start_time` DATETIME DEFAULT NULL,
                        `due_time` DATETIME DEFAULT NULL,
                        `completed_time` DATETIME DEFAULT NULL,

    -- 排序字段：建议采用“全量重排”策略，即由前端传入排序后的ID列表，后端更新此字段
                        `sort_order` INT DEFAULT 0 COMMENT '任务在当前分组下的排序',

                        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        INDEX idx_group (`group_id`),
                        INDEX idx_list_status (`list_id`, `status`),
                        INDEX idx_user_due (`user_id`, `due_time`),
                        INDEX idx_parent (`parent_id`)
) COMMENT '任务表';

-- ==========================================
-- 辅助表：动态日志 (Activity Log)
-- ==========================================
CREATE TABLE `activity_log` (
                                `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                `user_id` BIGINT NOT NULL,
                                `action_type` VARCHAR(32) NOT NULL COMMENT 'CREATE, UPDATE, DELETE, COMPLETE',
                                `target_type` VARCHAR(32) NOT NULL COMMENT 'TASK, TASK_GROUP, LIST, LIST_GROUP',
                                `target_id` BIGINT NOT NULL,
                                `target_name` VARCHAR(255) COMMENT '对象名称快照',
                                `context_info` JSON DEFAULT NULL COMMENT '存储清单名、任务分组名等上下文',
                                `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                INDEX idx_user_timeline (`user_id`, `created_at` DESC)
) COMMENT '用户动态日志表';

-- ==========================================
-- 初始化逻辑示例 (伪代码/注释)
-- ==========================================
/*
当新用户 (user_id = 88) 注册时，后端事务应执行：

1. 创建默认清单分组:
   INSERT INTO list_group (user_id, name, is_default) VALUES (88, '默认分组', 1);
   --> Returns group_id = 10

2. 创建默认清单 (Inbox):
   INSERT INTO todo_list (user_id, group_id, name, is_default) VALUES (88, 10, '任务', 1);
   --> Returns list_id = 200

3. 创建默认任务分组:
   INSERT INTO task_group (list_id, name, is_default) VALUES (200, '默认分组', 1);
   --> Returns task_group_id = 500

此时，用户新建任务若不指定位置，默认插入到 task_group_id = 500 中。
*/
