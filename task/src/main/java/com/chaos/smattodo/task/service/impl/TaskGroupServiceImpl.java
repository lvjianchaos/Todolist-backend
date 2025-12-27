package com.chaos.smattodo.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chaos.smattodo.task.activity.enums.ActivityAction;
import com.chaos.smattodo.task.activity.enums.ActivityEntityType;
import com.chaos.smattodo.task.activity.service.ActivityContext;
import com.chaos.smattodo.task.activity.service.ActivityRecorder;
import com.chaos.smattodo.task.common.enums.TaskGroupErrorCodeEnum;
import com.chaos.smattodo.task.common.enums.TodoListErrorCodeEnum;
import com.chaos.smattodo.task.common.exception.ClientException;
import com.chaos.smattodo.task.dto.req.CreateTaskGroupReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderTaskGroupsReqDTO;
import com.chaos.smattodo.task.dto.req.RenameReqDTO;
import com.chaos.smattodo.task.dto.resp.TaskGroupRespDTO;
import com.chaos.smattodo.task.entity.Task;
import com.chaos.smattodo.task.entity.TaskGroup;
import com.chaos.smattodo.task.entity.TodoList;
import com.chaos.smattodo.task.mapper.TaskGroupMapper;
import com.chaos.smattodo.task.mapper.TaskMapper;
import com.chaos.smattodo.task.mapper.TodoListMapper;
import com.chaos.smattodo.task.service.TaskGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskGroupServiceImpl implements TaskGroupService {

    private static final double GAP = 1000D;

    private final TaskGroupMapper taskGroupMapper;
    private final TaskMapper taskMapper;
    private final TodoListMapper todoListMapper;

    private final ActivityRecorder activityRecorder;

    @Override
    public List<TaskGroupRespDTO> listTaskGroups(Long userId, Long listId) {
        ensureListPermission(userId, listId);

        List<TaskGroup> groups = taskGroupMapper.selectList(new LambdaQueryWrapper<TaskGroup>()
                .eq(TaskGroup::getListId, listId)
                .orderByAsc(TaskGroup::getSortOrder)
                .orderByAsc(TaskGroup::getId));
        if (groups.isEmpty()) {
            return Collections.emptyList();
        }
        return groups.stream().map(this::toResp).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskGroupRespDTO createTaskGroup(Long userId, CreateTaskGroupReqDTO dto) {
        ensureListPermission(userId, dto.getListId());

        TaskGroup group = new TaskGroup();
        group.setListId(dto.getListId());
        group.setName(dto.getName());
        group.setSortOrder((dto.getPrevSortOrder() == null ? 0d : dto.getPrevSortOrder()) + GAP);
        group.setIsDefault(0);
        taskGroupMapper.insert(group);

        TodoList list = todoListMapper.selectById(dto.getListId());
        activityRecorder.record(userId,
                ActivityEntityType.TASK_GROUP,
                ActivityAction.CREATE,
                ActivityContext.builder()
                        .listId(group.getListId())
                        .listName(list == null ? null : list.getName())
                        .taskGroupId(group.getId())
                        .taskGroupName(group.getName())
                        .summary("创建 任务分组 " + group.getName())
                        .build());

        return toResp(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskGroupRespDTO renameTaskGroup(Long userId, Long groupId, RenameReqDTO dto) {
        TaskGroup group = taskGroupMapper.selectById(groupId);
        if (group == null) {
            throw new ClientException(TaskGroupErrorCodeEnum.TASK_GROUP_NOT_FOUND);
        }
        ensureListPermission(userId, group.getListId());

        String oldName = group.getName();
        group.setName(dto.getName());
        taskGroupMapper.updateById(group);

        TodoList list = todoListMapper.selectById(group.getListId());
        activityRecorder.record(userId,
                ActivityEntityType.TASK_GROUP,
                ActivityAction.RENAME,
                ActivityContext.builder()
                        .listId(group.getListId())
                        .listName(list == null ? null : list.getName())
                        .taskGroupId(group.getId())
                        .taskGroupName(group.getName())
                        .summary("重命名 任务分组 " + oldName + " 为 " + group.getName())
                        .extraData("{\"oldName\":\"" + escapeJson(oldName) + "\",\"newName\":\"" + escapeJson(group.getName()) + "\"}")
                        .build());

        return toResp(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskGroup(Long userId, Long groupId) {
        TaskGroup group = taskGroupMapper.selectById(groupId);
        if (group == null) {
            throw new ClientException(TaskGroupErrorCodeEnum.TASK_GROUP_NOT_FOUND);
        }
        ensureListPermission(userId, group.getListId());

        if (Objects.equals(group.getIsDefault(), 1)) {
            throw new ClientException(TaskGroupErrorCodeEnum.TASK_GROUP_DEFAULT_CANNOT_DELETE);
        }

        TodoList list = todoListMapper.selectById(group.getListId());
        activityRecorder.record(userId,
                ActivityEntityType.TASK_GROUP,
                ActivityAction.DELETE,
                ActivityContext.builder()
                        .listId(group.getListId())
                        .listName(list == null ? null : list.getName())
                        .taskGroupId(group.getId())
                        .taskGroupName(group.getName())
                        .summary("删除 任务分组 " + group.getName())
                        .build());

        // 级联删除：先删该分组下所有任务，再删分组
        taskMapper.delete(new LambdaQueryWrapper<Task>()
                .eq(Task::getListId, group.getListId())
                .eq(Task::getTaskGroupId, groupId));
        taskGroupMapper.deleteById(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TaskGroupRespDTO> reorderTaskGroups(Long userId, ReorderTaskGroupsReqDTO dto) {
        ensureListPermission(userId, dto.getListId());

        TaskGroup group = taskGroupMapper.selectById(dto.getMovedId());
        if (group == null) {
            throw new ClientException(TaskGroupErrorCodeEnum.TASK_GROUP_NOT_FOUND);
        }
        if (!Objects.equals(group.getListId(), dto.getListId())) {
            throw new ClientException(TaskGroupErrorCodeEnum.TASK_GROUP_SORT_ORDER_ERROR);
        }

        Double prev = dto.getPrevSortOrder();
        Double next = dto.getNextSortOrder();
        double newSort = computeSortOrder(prev, next);

        if (Objects.equals(group.getSortOrder(), newSort) || (prev != null && next != null && (next - prev) < 1e-9)) {
            reindexListGroups(dto.getListId());
            newSort = computeSortOrder(prev, next);
        }

        group.setSortOrder(newSort);
        taskGroupMapper.updateById(group);

        return listTaskGroups(userId, dto.getListId());
    }

    private void reindexListGroups(Long listId) {
        List<TaskGroup> groups = taskGroupMapper.selectList(new LambdaQueryWrapper<TaskGroup>()
                .eq(TaskGroup::getListId, listId)
                .orderByAsc(TaskGroup::getSortOrder)
                .orderByAsc(TaskGroup::getId));
        double current = GAP;
        for (TaskGroup g : groups) {
            g.setSortOrder(current);
            current += GAP;
            taskGroupMapper.updateById(g);
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
            throw new ClientException(TaskGroupErrorCodeEnum.TASK_GROUP_SORT_ORDER_ERROR);
        }
        return (prev + next) / 2d;
    }

    private void ensureListPermission(Long userId, Long listId) {
        TodoList list = todoListMapper.selectById(listId);
        if (list == null) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_NOT_FOUND);
        }
        if (!Objects.equals(list.getUserId(), userId)) {
            throw new ClientException(TaskGroupErrorCodeEnum.TASK_GROUP_NO_PERMISSION);
        }
    }

    private TaskGroupRespDTO toResp(TaskGroup group) {
        TaskGroupRespDTO dto = new TaskGroupRespDTO();
        dto.setId(group.getId());
        dto.setListId(group.getListId());
        dto.setName(group.getName());
        dto.setSortOrder(group.getSortOrder());
        dto.setIsDefault(group.getIsDefault());
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
