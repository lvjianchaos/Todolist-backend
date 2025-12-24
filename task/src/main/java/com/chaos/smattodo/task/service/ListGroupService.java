package com.chaos.smattodo.task.service;

import com.chaos.smattodo.task.dto.req.CreateListGroupReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;

import java.util.List;

public interface ListGroupService {

    List<ListGroupRespDTO> listListGroupByUserId(Long userId);

    ListGroupRespDTO createListGroup(Long userId, CreateListGroupReqDTO dto);

    ListGroupRespDTO renameListGroup(Long userId, Long groupId, RenameReqDTO dto);

    void deleteListGroup(Long userId, Long groupId);

    List<ListGroupRespDTO> reorderListGroups(Long userId, ReorderReqDTO dto);
}

