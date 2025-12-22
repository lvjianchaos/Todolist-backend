package com.chaos.smattodo.task.controller;

import com.chaos.smattodo.task.common.result.Result;
import com.chaos.smattodo.task.common.result.Results;
import com.chaos.smattodo.task.dto.req.ListGroupRemoveReqDTO;
import com.chaos.smattodo.task.dto.req.ListGroupSaveReqDTO;
import com.chaos.smattodo.task.dto.req.ListGroupUpdateNameReqDTO;
import com.chaos.smattodo.task.dto.req.ListGroupUpdateSortOrderReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;
import com.chaos.smattodo.task.service.ListGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class ListGroupController {

    private final ListGroupService listGroupService;

    @GetMapping("/list-group")
    public Result<List<ListGroupRespDTO>> listListGroups(@RequestHeader("X-User-Id") Long userId) {
        return Results.success(listGroupService.listListGroupByUserId(userId));
    }

    @PostMapping("/list-group")
    public Result<Void> saveListGroup(@RequestHeader("X-User-Id") Long userId, @RequestBody ListGroupSaveReqDTO dto) {
        listGroupService.saveListGroup(userId, dto);
        return Results.success("保存成功!");
    }

    @DeleteMapping("/list-group")
    public Result<Void> removeListGroup(@RequestBody ListGroupRemoveReqDTO dto) {
        listGroupService.removeListGroup(dto.getId());
        return Results.success("删除成功!");
    }

    @PostMapping("/list-group/update-name")
    public Result<Void> updateListGroupName(@RequestBody ListGroupUpdateNameReqDTO dto) {
        listGroupService.updateListGroupName(dto);
        return Results.success("重命名成功!");
    }

    @PostMapping("/list-group/update-sort")
    public Result<Void> updateListGroupSortOrder(@RequestBody ListGroupUpdateSortOrderReqDTO dto) {
        listGroupService.updateListGroupSortOrder(dto);
        return Results.success("重新排序成功!");
    }
}
