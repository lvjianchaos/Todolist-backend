package com.chaos.smattodo.task.activity.service;

import com.chaos.smattodo.task.activity.enums.ActivityAction;
import com.chaos.smattodo.task.activity.enums.ActivityEntityType;

/**
 * task 模块内的活动记录器：同步写入 activity_log（同库），用于强一致动态流。
 */
public interface ActivityRecorder {

    void record(Long userId,
                ActivityEntityType entityType,
                ActivityAction action,
                ActivityContext context);
}

