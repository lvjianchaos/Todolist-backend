package com.chaos.smattodo.task.service;

import com.chaos.smattodo.task.dto.req.CreateTaskGroupReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderTaskGroupsReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.resp.TaskGroupRespDTO;

import java.util.List;

public interface TaskGroupService {

    List<TaskGroupRespDTO> listTaskGroups(Long userId, Long listId);

    TaskGroupRespDTO createTaskGroup(Long userId, CreateTaskGroupReqDTO dto);

    TaskGroupRespDTO renameTaskGroup(Long userId, Long groupId, RenameReqDTO dto);

    void deleteTaskGroup(Long userId, Long groupId);

    List<TaskGroupRespDTO> reorderTaskGroups(Long userId, ReorderTaskGroupsReqDTO dto);
}
