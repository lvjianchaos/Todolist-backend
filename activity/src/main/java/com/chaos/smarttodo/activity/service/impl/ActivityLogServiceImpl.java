package com.chaos.smarttodo.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chaos.smarttodo.activity.dto.resp.ActivityLogRespDTO;
import com.chaos.smarttodo.activity.entity.ActivityLog;
import com.chaos.smarttodo.activity.mapper.ActivityLogMapper;
import com.chaos.smarttodo.activity.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogMapper activityLogMapper;

    @Override
    public Page<ActivityLogRespDTO> pageLogs(Long userId, Long listId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<ActivityLog> p = new Page<>(safePage, safeSize);
        Page<ActivityLog> result = activityLogMapper.selectPage(p, new LambdaQueryWrapper<ActivityLog>()
                .eq(ActivityLog::getUserId, userId)
                .eq(listId != null, ActivityLog::getListId, listId)
                .orderByDesc(ActivityLog::getCreatedAt)
                .orderByDesc(ActivityLog::getId));

        List<ActivityLogRespDTO> records = result.getRecords().stream().map(this::toResp).toList();

        Page<ActivityLogRespDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(records);
        return dtoPage;
    }

    private ActivityLogRespDTO toResp(ActivityLog e) {
        ActivityLogRespDTO dto = new ActivityLogRespDTO();
        dto.setId(e.getId());
        dto.setEntityType(e.getEntityType());
        dto.setAction(e.getAction());
        dto.setListId(e.getListId());
        dto.setListName(e.getListName());
        dto.setListGroupId(e.getListGroupId());
        dto.setLgName(e.getLgName());
        dto.setTgId(e.getTgId());
        dto.setTgName(e.getTgName());
        dto.setTaskId(e.getTaskId());
        dto.setTaskName(e.getTaskName());
        dto.setSummary(e.getSummary());
        dto.setExtraData(e.getExtraData());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
