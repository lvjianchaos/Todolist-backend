package com.chaos.smarttodo.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chaos.smarttodo.activity.common.result.Result;
import com.chaos.smarttodo.activity.dto.resp.ActivityLogRespDTO;
import com.chaos.smarttodo.activity.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activity/logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    /**
     * 获取动态：
     * - 不传 listId：获取全部动态
     * - 传 listId：获取某个清单下动态（用于清单详情页）
     */
    @GetMapping
    public Result<Page<ActivityLogRespDTO>> page(@RequestHeader("X-User-Id") Long userId,
                                                @RequestParam(value = "listId", required = false) Long listId,
                                                @RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "size", defaultValue = "20") int size) {
        return com.chaos.smarttodo.activity.common.result.Results.success(
                activityLogService.pageLogs(userId, listId, page, size)
        );
    }
}
