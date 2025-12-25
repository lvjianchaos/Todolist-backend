package com.chaos.smattodo.task.controller;

import com.chaos.smattodo.task.common.result.Result;
import com.chaos.smattodo.task.common.result.Results;
import com.chaos.smattodo.task.dto.req.CreateTaskGroupReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderTaskGroupsReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.resp.TaskGroupRespDTO;
import com.chaos.smattodo.task.service.TaskGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task/task-groups")
@RequiredArgsConstructor
public class TaskGroupController {

    private final TaskGroupService taskGroupService;

    @GetMapping
    public Result<List<TaskGroupRespDTO>> listTaskGroups(@RequestHeader("X-User-Id") Long userId,
                                                        @RequestParam("listId") Long listId) {
        return Results.success(taskGroupService.listTaskGroups(userId, listId));
    }

    @PostMapping
    public Result<TaskGroupRespDTO> createTaskGroup(@RequestHeader("X-User-Id") Long userId,
                                                   @RequestBody CreateTaskGroupReqDTO dto) {
        return Results.success(taskGroupService.createTaskGroup(userId, dto));
    }

    @PatchMapping("/{groupId}")
    public Result<TaskGroupRespDTO> renameTaskGroup(@RequestHeader("X-User-Id") Long userId,
                                                   @PathVariable("groupId") Long groupId,
                                                   @RequestBody RenameReqDTO dto) {
        return Results.success(taskGroupService.renameTaskGroup(userId, groupId, dto));
    }

    @DeleteMapping("/{groupId}")
    public Result<Void> deleteTaskGroup(@RequestHeader("X-User-Id") Long userId,
                                        @PathVariable("groupId") Long groupId) {
        taskGroupService.deleteTaskGroup(userId, groupId);
        return Results.success("删除成功!");
    }

    @PatchMapping("/sort-order")
    public Result<List<TaskGroupRespDTO>> reorderTaskGroups(@RequestHeader("X-User-Id") Long userId,
                                                           @RequestBody ReorderTaskGroupsReqDTO dto) {
        return Results.success(taskGroupService.reorderTaskGroups(userId, dto));
    }
}

