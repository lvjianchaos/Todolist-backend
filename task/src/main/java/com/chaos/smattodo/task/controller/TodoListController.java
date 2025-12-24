package com.chaos.smattodo.task.controller;

import com.chaos.smattodo.task.common.result.Result;
import com.chaos.smattodo.task.common.result.Results;
import com.chaos.smattodo.task.dto.req.CreateListReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderListsReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;
import com.chaos.smattodo.task.dto.resp.TodoListRespDTO;
import com.chaos.smattodo.task.service.TodoListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task/lists")
@RequiredArgsConstructor
public class TodoListController {

    private final TodoListService todoListService;

    @PostMapping
    public Result<TodoListRespDTO> createList(@RequestHeader("X-User-Id") Long userId,
                                             @RequestBody CreateListReqDTO dto) {
        return Results.success(todoListService.createList(userId, dto));
    }

    @PatchMapping("/{listId}")
    public Result<TodoListRespDTO> renameList(@RequestHeader("X-User-Id") Long userId,
                                             @PathVariable("listId") Long listId,
                                             @RequestBody RenameReqDTO dto) {
        return Results.success(todoListService.renameList(userId, listId, dto));
    }

    @DeleteMapping("/{listId}")
    public Result<Void> deleteList(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable("listId") Long listId) {
        todoListService.deleteList(userId, listId);
        return Results.success("删除成功!");
    }

    @PatchMapping("/sort-order")
    public Result<List<ListGroupRespDTO>> reorderLists(@RequestHeader("X-User-Id") Long userId,
                                                      @RequestBody ReorderListsReqDTO dto) {
        return Results.success(todoListService.reorderLists(userId, dto));
    }
}
