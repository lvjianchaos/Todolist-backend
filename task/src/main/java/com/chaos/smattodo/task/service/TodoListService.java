package com.chaos.smattodo.task.service;

import com.chaos.smattodo.task.dto.req.CreateListReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderListsReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;
import com.chaos.smattodo.task.dto.resp.TodoListRespDTO;

import java.util.List;

public interface TodoListService {

    TodoListRespDTO createList(Long userId, CreateListReqDTO dto);

    TodoListRespDTO renameList(Long userId, Long listId, RenameReqDTO dto);

    void deleteList(Long userId, Long listId);

    List<ListGroupRespDTO> reorderLists(Long userId, ReorderListsReqDTO dto);
}
