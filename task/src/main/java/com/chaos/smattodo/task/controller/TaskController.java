package com.chaos.smattodo.task.controller;

import com.chaos.smattodo.task.common.result.Result;
import com.chaos.smattodo.task.common.result.Results;
import com.chaos.smattodo.task.dto.req.CreateTaskReqDTO;
import com.chaos.smattodo.task.dto.req.MoveTaskReqDTO;
import com.chaos.smattodo.task.dto.req.PatchTaskReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderTasksReqDTO;
import com.chaos.smattodo.task.dto.resp.TaskRespDTO;
import com.chaos.smattodo.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public Result<List<TaskRespDTO>> listTasks(@RequestHeader("X-User-Id") Long userId,
                                              @RequestParam("listId") Long listId,
                                              @RequestParam(value = "taskGroupId", required = false) Long taskGroupId,
                                              @RequestParam(value = "parentId") Long parentId) {
        return Results.success(taskService.listTasks(userId, listId, taskGroupId, parentId));
    }

    @GetMapping("/{taskId}/children")
    public Result<List<TaskRespDTO>> listChildren(@RequestHeader("X-User-Id") Long userId,
                                                 @PathVariable("taskId") Long taskId) {
        return Results.success(taskService.listChildren(userId, taskId));
    }

    @PostMapping
    public Result<TaskRespDTO> createTask(@RequestHeader("X-User-Id") Long userId,
                                         @RequestBody CreateTaskReqDTO dto) {
        return Results.success(taskService.createTask(userId, dto));
    }

    @PatchMapping("/{taskId}")
    public Result<TaskRespDTO> patchTask(@RequestHeader("X-User-Id") Long userId,
                                        @PathVariable("taskId") Long taskId,
                                        @RequestBody PatchTaskReqDTO dto) {
        return Results.success(taskService.patchTask(userId, taskId, dto));
    }

    @DeleteMapping("/{taskId}")
    public Result<Void> deleteTask(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable("taskId") Long taskId,
                                   @RequestParam(value = "cascade", required = false, defaultValue = "true") boolean cascade) {
        taskService.deleteTask(userId, taskId, cascade);
        return Results.success("删除成功!");
    }

    @PatchMapping("/{taskId}/move")
    public Result<TaskRespDTO> moveTask(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable("taskId") Long taskId,
                                       @RequestBody MoveTaskReqDTO dto) {
        return Results.success(taskService.moveTask(userId, taskId, dto));
    }

    @PatchMapping("/sort-order")
    public Result<List<TaskRespDTO>> reorderTasks(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestBody ReorderTasksReqDTO dto) {
        return Results.success(taskService.reorderTasks(userId, dto));
    }
}

