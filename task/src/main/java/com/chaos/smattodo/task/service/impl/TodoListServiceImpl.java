package com.chaos.smattodo.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chaos.smattodo.task.common.enums.ListGroupErrorCodeEnum;
import com.chaos.smattodo.task.common.enums.TodoListErrorCodeEnum;
import com.chaos.smattodo.task.common.exception.ClientException;
import com.chaos.smattodo.task.dto.req.CreateListReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderListsReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;
import com.chaos.smattodo.task.dto.resp.TodoListRespDTO;
import com.chaos.smattodo.task.entity.ListGroup;
import com.chaos.smattodo.task.entity.Task;
import com.chaos.smattodo.task.entity.TaskGroup;
import com.chaos.smattodo.task.entity.TodoList;
import com.chaos.smattodo.task.mapper.ListGroupMapper;
import com.chaos.smattodo.task.mapper.TaskGroupMapper;
import com.chaos.smattodo.task.mapper.TaskMapper;
import com.chaos.smattodo.task.mapper.TodoListMapper;
import com.chaos.smattodo.task.service.ListGroupService;
import com.chaos.smattodo.task.service.TodoListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TodoListServiceImpl implements TodoListService {

    private static final double GAP = 1000D;

    private final TodoListMapper todoListMapper;
    private final ListGroupMapper listGroupMapper;

    private final TaskGroupMapper taskGroupMapper;
    private final TaskMapper taskMapper;

    /**
     * 复用 list-groups 的组装逻辑：拖拽排序后前端需要刷新分组+清单整体结构
     */
    private final ListGroupService listGroupService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoListRespDTO createList(Long userId, CreateListReqDTO dto) {
        ListGroup group = listGroupMapper.selectById(dto.getGroupId());
        if (group == null) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NOT_FOUND);
        }
        if (!Objects.equals(group.getUserId(), userId)) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NO_PERMISSION);
        }

        TodoList list = new TodoList();
        list.setUserId(userId);
        list.setListGroupId(dto.getGroupId());
        list.setName(dto.getName());
        list.setSortOrder(dto.getPrevSortOrder() + GAP);
        todoListMapper.insert(list);

        // 创建该清单下的两个任务分组
        // 1) 未分类（is_default = 1）
        TaskGroup unclassified = new TaskGroup();
        unclassified.setListId(list.getId());
        unclassified.setName("未分类");
        unclassified.setIsDefault(1);
        unclassified.setSortOrder(GAP);
        taskGroupMapper.insert(unclassified);

        // 2) 默认分组（is_default = 0）
        TaskGroup defaultGroup = new TaskGroup();
        defaultGroup.setListId(list.getId());
        defaultGroup.setName("默认分组");
        defaultGroup.setIsDefault(0);
        defaultGroup.setSortOrder(GAP * 2);
        taskGroupMapper.insert(defaultGroup);

        return toTodoListResp(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodoListRespDTO renameList(Long userId, Long listId, RenameReqDTO dto) {
        TodoList list = todoListMapper.selectById(listId);
        if (list == null) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_NOT_FOUND);
        }
        if (!Objects.equals(list.getUserId(), userId)) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_NO_PERMISSION);
        }

        list.setName(dto.getName());
        todoListMapper.updateById(list);
        return toTodoListResp(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteList(Long userId, Long listId) {
        TodoList list = todoListMapper.selectById(listId);
        if (list == null) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_NOT_FOUND);
        }
        if (!Objects.equals(list.getUserId(), userId)) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_NO_PERMISSION);
        }

        // 级联删除：清单 -> 任务分组 -> 任务
        taskMapper.delete(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getListId, listId));
        taskGroupMapper.delete(new LambdaQueryWrapper<TaskGroup>()
                .eq(TaskGroup::getListId, listId));

        todoListMapper.deleteById(listId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ListGroupRespDTO> reorderLists(Long userId, ReorderListsReqDTO dto) {
        TodoList list = todoListMapper.selectById(dto.getMovedId());
        if (list == null) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_NOT_FOUND);
        }
        if (!Objects.equals(list.getUserId(), userId)) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_NO_PERMISSION);
        }

        // 允许跨分组拖拽：以前端传入的 groupId 为准
        Long targetGroupId = dto.getGroupId();
        if (targetGroupId == null) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_SORT_ORDER_ERROR);
        }
        ListGroup targetGroup = listGroupMapper.selectById(targetGroupId);
        if (targetGroup == null) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NOT_FOUND);
        }
        if (!Objects.equals(targetGroup.getUserId(), userId)) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NO_PERMISSION);
        }

        Double prev = dto.getPrevSortOrder();
        Double next = dto.getNextSortOrder();
        double newSort = computeSortOrder(prev, next);

        if (Objects.equals(list.getSortOrder(), newSort) || (prev != null && next != null && (next - prev) < 1e-9)) {
            reindexGroupLists(userId, targetGroupId);
            newSort = computeSortOrder(prev, next);
        }

        list.setListGroupId(targetGroupId);
        list.setSortOrder(newSort);
        todoListMapper.updateById(list);

        return listGroupService.listListGroupByUserId(userId);
    }

    private void reindexGroupLists(Long userId, Long groupId) {
        List<TodoList> lists = todoListMapper.selectList(new LambdaQueryWrapper<TodoList>()
                .eq(TodoList::getUserId, userId)
                .eq(TodoList::getListGroupId, groupId)
                .orderByAsc(TodoList::getSortOrder)
                .orderByAsc(TodoList::getId));

        double current = GAP;
        for (TodoList l : lists) {
            l.setSortOrder(current);
            current += GAP;
            todoListMapper.updateById(l);
        }
    }

    private double computeSortOrder(Double prevSortOrder, Double nextSortOrder) {
        double prev = prevSortOrder == null ? 0d : prevSortOrder;
        double next = nextSortOrder == null ? 0d : nextSortOrder;

        if (prev == 0d && next == 0d) {
            return GAP;
        }
        if (prev == 0d) {
            return next / 2d;
        }
        if (next == 0d) {
            return prev + GAP;
        }
        if (prev >= next) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_SORT_ORDER_ERROR);
        }
        return (prev + next) / 2d;
    }

    private TodoListRespDTO toTodoListResp(TodoList list) {
        TodoListRespDTO dto = new TodoListRespDTO();
        dto.setId(list.getId());
        dto.setGroupId(list.getListGroupId());
        dto.setName(list.getName());
        dto.setSortOrder(list.getSortOrder());
        return dto;
    }
}

