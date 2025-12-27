package com.chaos.smattodo.task.activity.service.impl;

import com.chaos.smattodo.task.activity.entity.ActivityLog;
import com.chaos.smattodo.task.activity.enums.ActivityAction;
import com.chaos.smattodo.task.activity.enums.ActivityEntityType;
import com.chaos.smattodo.task.activity.mapper.ActivityLogMapper;
import com.chaos.smattodo.task.activity.service.ActivityContext;
import com.chaos.smattodo.task.activity.service.ActivityRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityRecorderImpl implements ActivityRecorder {

    /**
     * 由 task 模块的 @MapperScan("com.chaos.smattodo.task.mapper") 扫描不到本包，
     * 所以需要把 activity mapper 也放到同一扫描路径下（见后续对 TaskApplication 的修改）。
     */
    private final ActivityLogMapper activityLogMapper;

    @Override
    public void record(Long userId, ActivityEntityType entityType, ActivityAction action, ActivityContext context) {
        if (userId == null || entityType == null || action == null) {
            return;
        }

        ActivityLog log = new ActivityLog();
        log.setUserId(userId);
        log.setUsername(context == null ? null : context.getUsername());
        log.setEntityType(entityType.getCode());
        log.setAction(action.getCode());

        if (context != null) {
            log.setListGroupId(context.getListGroupId());
            log.setLgName(context.getLgName());

            log.setListId(context.getListId());
            log.setListName(context.getListName());

            log.setTgId(context.getTaskGroupId());
            log.setTgName(context.getTaskGroupName());

            log.setTaskId(context.getTaskId());
            log.setTaskName(context.getTaskName());

            log.setSummary(context.getSummary());
            log.setExtraData(context.getExtraData());
        }

        activityLogMapper.insert(log);
    }
}
