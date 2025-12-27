package com.chaos.smattodo.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chaos.smattodo.task.activity.enums.ActivityAction;
import com.chaos.smattodo.task.activity.enums.ActivityEntityType;
import com.chaos.smattodo.task.activity.service.ActivityContext;
import com.chaos.smattodo.task.activity.service.ActivityRecorder;
import com.chaos.smattodo.task.common.enums.ListGroupErrorCodeEnum;
import com.chaos.smattodo.task.common.exception.ClientException;
import com.chaos.smattodo.task.dto.req.CreateListGroupReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;
import com.chaos.smattodo.task.dto.resp.TodoListRespDTO;
import com.chaos.smattodo.task.entity.ListGroup;
import com.chaos.smattodo.task.entity.TodoList;
import com.chaos.smattodo.task.entity.Task;
import com.chaos.smattodo.task.entity.TaskGroup;
import com.chaos.smattodo.task.mapper.ListGroupMapper;
import com.chaos.smattodo.task.mapper.TodoListMapper;
import com.chaos.smattodo.task.mapper.TaskGroupMapper;
import com.chaos.smattodo.task.mapper.TaskMapper;
import com.chaos.smattodo.task.service.ListGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListGroupServiceImpl implements ListGroupService {

    private static final double GAP = 1000D;

    private final ListGroupMapper listGroupMapper;
    private final TodoListMapper todoListMapper;

    private final TaskGroupMapper taskGroupMapper;
    private final TaskMapper taskMapper;

    private final ActivityRecorder activityRecorder;

    @Override
    public List<ListGroupRespDTO> listListGroupByUserId(Long userId) {
        List<ListGroup> groups = listGroupMapper.selectList(new LambdaQueryWrapper<ListGroup>()
                .eq(ListGroup::getUserId, userId)
                .orderByAsc(ListGroup::getSortOrder)
                .orderByAsc(ListGroup::getId));

        if (groups.isEmpty()) {
            return Collections.emptyList();
        }

        List<TodoList> lists = todoListMapper.selectList(new LambdaQueryWrapper<TodoList>()
                .eq(TodoList::getUserId, userId)
                .orderByAsc(TodoList::getSortOrder)
                .orderByAsc(TodoList::getId));

        Map<Long, List<TodoListRespDTO>> groupIdToLists = lists.stream()
                .collect(Collectors.groupingBy(TodoList::getListGroupId, Collectors.mapping(this::toTodoListResp, Collectors.toList())));

        List<ListGroupRespDTO> resp = new ArrayList<>(groups.size());
        for (ListGroup group : groups) {
            ListGroupRespDTO dto = toListGroupResp(group);
            dto.setList(groupIdToLists.getOrDefault(group.getId(), Collections.emptyList()));
            resp.add(dto);
        }
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ListGroupRespDTO createListGroup(Long userId, CreateListGroupReqDTO dto) {
        ListGroup group = new ListGroup();
        group.setUserId(userId);
        group.setName(dto.getName());

        double sortOrder = dto.getPrevSortOrder() + GAP;
        group.setSortOrder(sortOrder);
        listGroupMapper.insert(group);

        activityRecorder.record(userId,
                ActivityEntityType.LIST_GROUP,
                ActivityAction.CREATE,
                ActivityContext.builder()
                        .listGroupId(group.getId())
                        .lgName(group.getName())
                        .summary("创建 清单分组 " + group.getName())
                        .build());

        ListGroupRespDTO resp = toListGroupResp(group);
        resp.setList(Collections.emptyList());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ListGroupRespDTO renameListGroup(Long userId, Long groupId, RenameReqDTO dto) {
        ListGroup group = listGroupMapper.selectById(groupId);
        if (group == null) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NOT_FOUND);
        }
        if (!Objects.equals(group.getUserId(), userId)) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NO_PERMISSION);
        }

        String oldName = group.getName();
        group.setName(dto.getName());
        listGroupMapper.updateById(group);

        activityRecorder.record(userId,
                ActivityEntityType.LIST_GROUP,
                ActivityAction.RENAME,
                ActivityContext.builder()
                        .listGroupId(group.getId())
                        .lgName(group.getName())
                        .summary("重命名 清单分组 " + oldName + " 为 " + group.getName())
                        .extraData("{\"oldName\":\"" + escapeJson(oldName) + "\",\"newName\":\"" + escapeJson(group.getName()) + "\"}")
                        .build());

        ListGroupRespDTO resp = toListGroupResp(group);
        resp.setList(listListsByGroup(userId, groupId));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteListGroup(Long userId, Long groupId) {
        ListGroup group = listGroupMapper.selectById(groupId);
        if (group == null) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NOT_FOUND);
        }
        if (!Objects.equals(group.getUserId(), userId)) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NO_PERMISSION);
        }

        // 先记日志（删除后可能查不到名称）
        activityRecorder.record(userId,
                ActivityEntityType.LIST_GROUP,
                ActivityAction.DELETE,
                ActivityContext.builder()
                        .listGroupId(group.getId())
                        .lgName(group.getName())
                        .summary("删除 清单分组 " + group.getName())
                        .build());

        // 层层级联删除（批量版）：ListGroup -> TodoList -> TaskGroup -> Task
        List<Long> listIds = todoListMapper.selectList(new LambdaQueryWrapper<TodoList>()
                        .select(TodoList::getId)
                        .eq(TodoList::getUserId, userId)
                        .eq(TodoList::getListGroupId, groupId))
                .stream()
                .map(TodoList::getId)
                .filter(Objects::nonNull)
                .toList();

        if (!listIds.isEmpty()) {
            taskMapper.delete(new LambdaQueryWrapper<Task>()
                    .eq(Task::getUserId, userId)
                    .in(Task::getListId, listIds));

            taskGroupMapper.delete(new LambdaQueryWrapper<TaskGroup>()
                    .in(TaskGroup::getListId, listIds));

            todoListMapper.deleteBatchIds(listIds);
        }

        listGroupMapper.deleteById(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ListGroupRespDTO> reorderListGroups(Long userId, ReorderReqDTO dto) {
        ListGroup group = listGroupMapper.selectById(dto.getMovedId());

        if (group == null) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NOT_FOUND);
        }
        if (!Objects.equals(group.getUserId(), userId)) {
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_NO_PERMISSION);
        }

        Double prev = dto.getPrevSortOrder();
        Double next = dto.getNextSortOrder();
        double newSort = computeSortOrder(prev, next);

        // 精度耗尽兜底：取中值后如果不发生变化，则全量重排
        if (Objects.equals(group.getSortOrder(), newSort) || (prev != null && next != null && (next - prev) < 1e-9)) {
            reindexUserGroups(userId);
            // 重排后重新算一次
            newSort = computeSortOrder(prev, next);
        }

        group.setSortOrder(newSort);
        listGroupMapper.updateById(group);

        return listListGroupByUserId(userId);
    }

    private void reindexUserGroups(Long userId) {
        List<ListGroup> groups = listGroupMapper.selectList(new LambdaQueryWrapper<ListGroup>()
                .eq(ListGroup::getUserId, userId)
                .orderByAsc(ListGroup::getSortOrder)
                .orderByAsc(ListGroup::getId));
        double current = GAP;
        for (ListGroup g : groups) {
            g.setSortOrder(current);
            current += GAP;
            listGroupMapper.updateById(g);
        }
    }

    private List<TodoListRespDTO> listListsByGroup(Long userId, Long groupId) {
        List<TodoList> lists = todoListMapper.selectList(new LambdaQueryWrapper<TodoList>()
                .eq(TodoList::getUserId, userId)
                .eq(TodoList::getListGroupId, groupId)
                .orderByAsc(TodoList::getSortOrder)
                .orderByAsc(TodoList::getId));
        return lists.stream().map(this::toTodoListResp).collect(Collectors.toList());
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
            throw new ClientException(ListGroupErrorCodeEnum.LIST_GROUP_SORT_ORDER_ERROR);
        }
        return (prev + next) / 2d;
    }

    private ListGroupRespDTO toListGroupResp(ListGroup group) {
        ListGroupRespDTO dto = new ListGroupRespDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setSortOrder(group.getSortOrder());
        return dto;
    }

    private TodoListRespDTO toTodoListResp(TodoList list) {
        TodoListRespDTO dto = new TodoListRespDTO();
        dto.setId(list.getId());
        dto.setGroupId(list.getListGroupId());
        dto.setName(list.getName());
        dto.setSortOrder(list.getSortOrder());
        return dto;
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
