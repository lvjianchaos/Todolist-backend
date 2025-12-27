package com.chaos.smarttodo.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chaos.smarttodo.activity.dto.resp.ActivityLogRespDTO;

public interface ActivityLogService {

    Page<ActivityLogRespDTO> pageLogs(Long userId, Long listId, int page, int size);
}
