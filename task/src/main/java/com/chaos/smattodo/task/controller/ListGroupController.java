package com.chaos.smattodo.task.controller;

import com.chaos.smattodo.task.common.result.Result;
import com.chaos.smattodo.task.common.result.Results;
import com.chaos.smattodo.task.dto.req.CreateListGroupReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;
import com.chaos.smattodo.task.service.ListGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task/list-groups")
@RequiredArgsConstructor
public class ListGroupController {

    private final ListGroupService listGroupService;

    @GetMapping
    public Result<List<ListGroupRespDTO>> listListGroups(@RequestHeader("X-User-Id") Long userId) {
        return Results.success(listGroupService.listListGroupByUserId(userId));
    }

    @PostMapping
    public Result<ListGroupRespDTO> createListGroup(@RequestHeader("X-User-Id") Long userId,
                                                   @RequestBody CreateListGroupReqDTO dto) {
        return Results.success(listGroupService.createListGroup(userId, dto));
    }

    @PatchMapping("/{groupId}")
    public Result<ListGroupRespDTO> renameListGroup(@RequestHeader("X-User-Id") Long userId,
                                                   @PathVariable("groupId") Long groupId,
                                                   @RequestBody RenameReqDTO dto) {
        return Results.success(listGroupService.renameListGroup(userId, groupId, dto));
    }

    @DeleteMapping("/{groupId}")
    public Result<Void> deleteListGroup(@RequestHeader("X-User-Id") Long userId,
                                        @PathVariable("groupId") Long groupId) {
        listGroupService.deleteListGroup(userId, groupId);
        return Results.success("删除成功!");
    }

    @PatchMapping("/sort-order")
    public Result<List<ListGroupRespDTO>> reorderListGroups(@RequestHeader("X-User-Id") Long userId,
                                                           @RequestBody ReorderReqDTO dto) {
        return Results.success(listGroupService.reorderListGroups(userId, dto));
    }
}
