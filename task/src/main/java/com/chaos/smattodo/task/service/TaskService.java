package com.chaos.smattodo.task.service;

import com.chaos.smattodo.task.dto.req.CreateTaskReqDTO;
import com.chaos.smattodo.task.dto.req.MoveTaskReqDTO;
import com.chaos.smattodo.task.dto.req.PatchTaskReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderTasksReqDTO;
import com.chaos.smattodo.task.dto.resp.TaskRespDTO;

import java.util.List;

public interface TaskService {

    List<TaskRespDTO> listTasks(Long userId, Long listId, Long taskGroupId, Long parentId);

    List<TaskRespDTO> listChildren(Long userId, Long taskId);

    TaskRespDTO createTask(Long userId, CreateTaskReqDTO dto);

    TaskRespDTO patchTask(Long userId, Long taskId, PatchTaskReqDTO dto);

    void deleteTask(Long userId, Long taskId, boolean cascade);

    TaskRespDTO moveTask(Long userId, Long taskId, MoveTaskReqDTO dto);

    List<TaskRespDTO> reorderTasks(Long userId, ReorderTasksReqDTO dto);
}
