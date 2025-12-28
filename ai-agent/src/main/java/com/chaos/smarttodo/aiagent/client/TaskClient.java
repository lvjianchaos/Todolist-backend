package com.chaos.smarttodo.aiagent.client;

import com.chaos.smarttodo.aiagent.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "task")
public interface TaskClient {

    @GetMapping("/task/list-groups")
    TaskResult<List<ListGroupRespDTO>> listListGroups(@RequestHeader("X-User-Id") Long userId);

    @PostMapping("/task/list-groups")
    TaskResult<ListGroupRespDTO> createListGroup(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestBody CreateListGroupReqDTO dto);

    @PostMapping("/task/lists")
    TaskResult<TodoListRespDTO> createList(@RequestHeader("X-User-Id") Long userId,
                                          @RequestBody CreateListReqDTO dto);

    @GetMapping("/task/task-groups")
    TaskResult<List<TaskGroupRespDTO>> listTaskGroups(@RequestHeader("X-User-Id") Long userId,
                                                      @RequestParam("listId") Long listId);

    @PostMapping("/task/task-groups")
    TaskResult<TaskGroupRespDTO> createTaskGroup(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestBody CreateTaskGroupReqDTO dto);

    @PostMapping("/task/tasks")
    TaskResult<TaskRespDTO> createTask(@RequestHeader("X-User-Id") Long userId,
                                       @RequestBody CreateTaskReqDTO dto);

    @PatchMapping("/task/tasks/{taskId}")
    TaskResult<TaskRespDTO> patchTask(@RequestHeader("X-User-Id") Long userId,
                                      @PathVariable("taskId") Long taskId,
                                      @RequestBody PatchTaskReqDTO dto);

    @GetMapping("/task/tasks/root")
    TaskResult<List<TaskRespDTO>> listRootTasks(@RequestHeader("X-User-Id") Long userId,
                                                @RequestParam(value = "parentId", required = false, defaultValue = "0") Long parentId);
}
