package com.chaos.smattodo.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chaos.smattodo.task.common.enums.TaskErrorCodeEnum;
import com.chaos.smattodo.task.common.enums.TodoListErrorCodeEnum;
import com.chaos.smattodo.task.common.exception.ClientException;
import com.chaos.smattodo.task.dto.req.CreateTaskReqDTO;
import com.chaos.smattodo.task.dto.req.MoveTaskReqDTO;
import com.chaos.smattodo.task.dto.req.PatchTaskReqDTO;
import com.chaos.smattodo.task.dto.req.ReorderTasksReqDTO;
import com.chaos.smattodo.task.dto.resp.TaskRespDTO;
import com.chaos.smattodo.task.entity.Task;
import com.chaos.smattodo.task.entity.TodoList;
import com.chaos.smattodo.task.mapper.TaskMapper;
import com.chaos.smattodo.task.mapper.TodoListMapper;
import com.chaos.smattodo.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final double GAP = 1000D;

    private final TaskMapper taskMapper;
    private final TodoListMapper todoListMapper;

    @Override
    public List<TaskRespDTO> listTasks(Long userId, Long listId, Long taskGroupId, Long parentId) {
        ensureListPermission(userId, listId);

        Long normalizedParentId = normalizeParentId(parentId);

        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getListId, listId)
                .eq(taskGroupId != null, Task::getTaskGroupId, taskGroupId)
                .isNull(normalizedParentId == null, Task::getParentId)
                .eq(normalizedParentId != null, Task::getParentId, normalizedParentId)
                .orderByAsc(Task::getSortOrder)
                .orderByAsc(Task::getId));

        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        // hasChildren: 批量查一遍 parentId in (...)，避免 N+1
        List<Long> ids = tasks.stream().map(Task::getId).toList();
        List<Task> children = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .select(Task::getParentId)
                .eq(Task::getUserId, userId)
                .in(Task::getParentId, ids)
                .groupBy(Task::getParentId));
        Set<Long> hasChildrenParents = new HashSet<>();
        for (Task c : children) {
            if (c.getParentId() != null) {
                hasChildrenParents.add(c.getParentId());
            }
        }

        return tasks.stream().map(t -> toResp(t, hasChildrenParents.contains(t.getId()))).toList();
    }

    @Override
    public List<TaskRespDTO> listRootTasks(Long userId, Long parentId) {
        Long normalizedParentId = normalizeParentId(parentId);

        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .isNull(normalizedParentId == null, Task::getParentId)
                .eq(normalizedParentId != null, Task::getParentId, normalizedParentId)
                // 跨清单聚合时，为了前端更稳定，这里增加 listId/taskGroupId 的排序维度
                .orderByAsc(Task::getListId)
                .orderByAsc(Task::getTaskGroupId)
                .orderByAsc(Task::getSortOrder)
                .orderByAsc(Task::getId));

        if (tasks.isEmpty()) {
            return Collections.emptyList();
        }

        // hasChildren: 批量查 parentId in (...)，避免 N+1
        List<Long> ids = tasks.stream().map(Task::getId).toList();
        List<Task> children = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .select(Task::getParentId)
                .eq(Task::getUserId, userId)
                .in(Task::getParentId, ids)
                .groupBy(Task::getParentId));

        Set<Long> hasChildrenParents = new HashSet<>();
        for (Task c : children) {
            if (c.getParentId() != null) {
                hasChildrenParents.add(c.getParentId());
            }
        }

        return tasks.stream().map(t -> toResp(t, hasChildrenParents.contains(t.getId()))).toList();
    }

    @Override
    public List<TaskRespDTO> listChildren(Long userId, Long taskId) {
        Task parent = taskMapper.selectById(taskId);
        if (parent == null) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NOT_FOUND);
        }
        if (!Objects.equals(parent.getUserId(), userId)) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NO_PERMISSION);
        }
        return listTasks(userId, parent.getListId(), parent.getTaskGroupId(), parent.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskRespDTO createTask(Long userId, CreateTaskReqDTO dto) {
        ensureListPermission(userId, dto.getListId());

        Long normalizedParentId = normalizeParentId(dto.getParentId());

        Task task = new Task();
        task.setUserId(userId);
        task.setListId(dto.getListId());
        task.setTaskGroupId(dto.getTaskGroupId());
        task.setParentId(normalizedParentId);
        task.setName(dto.getName());
        task.setContent(null);
        task.setStatus(0);
        task.setPriority(0);

        task.setSortOrder((dto.getPrevSortOrder() == null ? 0d : dto.getPrevSortOrder()) + GAP);

        // 先 insert 拿到 id
        taskMapper.insert(task);

        // path/level
        if (normalizedParentId == null) {
            task.setLevel(1);
            task.setPath("0/");
        } else {
            Task parent = taskMapper.selectById(normalizedParentId);
            if (parent == null || !Objects.equals(parent.getUserId(), userId)) {
                throw new ClientException(TaskErrorCodeEnum.TASK_MOVE_TARGET_ERROR);
            }
            task.setLevel((parent.getLevel() == null ? 1 : parent.getLevel()) + 1);
            task.setPath(parent.getPath() + parent.getId() + "/");
        }
        taskMapper.updateById(task);

        return toResp(task, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskRespDTO patchTask(Long userId, Long taskId, PatchTaskReqDTO dto) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NOT_FOUND);
        }
        if (!Objects.equals(task.getUserId(), userId)) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NO_PERMISSION);
        }

        LambdaUpdateWrapper<Task> update = new LambdaUpdateWrapper<Task>()
                .eq(Task::getId, taskId)
                .eq(Task::getUserId, userId);

        boolean needUpdate = false;

        if (dto.getName() != null) {
            update.set(Task::getName, dto.getName());
            needUpdate = true;
        }
        if (dto.isContentSet()) {
            update.set(Task::getContent, dto.getContent());
            needUpdate = true;
        }
        if (dto.isStartedAtSet()) {
            update.set(Task::getStartedAt, dto.getStartedAt());
            needUpdate = true;
        }
        if (dto.isDueAtSet()) {
            update.set(Task::getDueAt, dto.getDueAt());
            needUpdate = true;
        }
        if (dto.getPriority() != null) {
            update.set(Task::getPriority, dto.getPriority());
            needUpdate = true;
        }
        if (dto.getStatus() != null) {
            update.set(Task::getStatus, dto.getStatus());
            if (Objects.equals(dto.getStatus(), 1)) {
                update.set(Task::getCompletedAt, LocalDateTime.now());
            } else {
                update.set(Task::getCompletedAt, null);
            }
            needUpdate = true;
        }

        if (needUpdate) {
            taskMapper.update(null, update);
        }

        Task updated = taskMapper.selectById(taskId);
        boolean hasChildren = hasChildren(userId, taskId);
        return toResp(updated, hasChildren);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long userId, Long taskId, boolean cascade) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NOT_FOUND);
        }
        if (!Objects.equals(task.getUserId(), userId)) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NO_PERMISSION);
        }

        if (!cascade) {
            taskMapper.deleteById(taskId);
            return;
        }

        // 递归级联删除（BFS）：避免依赖 path 格式
        Set<Long> toDelete = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(taskId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current == null || toDelete.contains(current)) {
                continue;
            }
            toDelete.add(current);

            List<Task> children = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                    .select(Task::getId)
                    .eq(Task::getUserId, userId)
                    .eq(Task::getParentId, current));
            for (Task c : children) {
                queue.add(c.getId());
            }
        }

        taskMapper.deleteBatchIds(toDelete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskRespDTO moveTask(Long userId, Long taskId, MoveTaskReqDTO dto) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NOT_FOUND);
        }
        if (!Objects.equals(task.getUserId(), userId)) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NO_PERMISSION);
        }

        if (dto.getListId() == null) {
            throw new ClientException(TaskErrorCodeEnum.TASK_MOVE_TARGET_ERROR);
        }
        ensureListPermission(userId, dto.getListId());

        Long targetParentId = normalizeParentId(dto.getParentId());

        String oldPrefix = task.getPath() + task.getId() + "/";
        int oldLevel = task.getLevel() == null ? 1 : task.getLevel();

        String newParentPath;
        int newLevel;
        if (targetParentId == null) {
            newParentPath = "0/";
            newLevel = 1;
        } else {
            Task newParent = taskMapper.selectById(targetParentId);
            if (newParent == null || !Objects.equals(newParent.getUserId(), userId)) {
                throw new ClientException(TaskErrorCodeEnum.TASK_MOVE_TARGET_ERROR);
            }
            newParentPath = newParent.getPath() + newParent.getId() + "/";
            newLevel = (newParent.getLevel() == null ? 1 : newParent.getLevel()) + 1;
        }

        double newSort = computeSortOrder(dto.getPrevSortOrder(), dto.getNextSortOrder());
        if (Objects.equals(task.getSortOrder(), newSort) || (dto.getPrevSortOrder() != null && dto.getNextSortOrder() != null
                && (dto.getNextSortOrder() - dto.getPrevSortOrder()) < 1e-9)) {
            reindexContainer(userId, dto.getListId(), dto.getTaskGroupId(), targetParentId);
            newSort = computeSortOrder(dto.getPrevSortOrder(), dto.getNextSortOrder());
        }

        // 更新自身
        task.setListId(dto.getListId());
        if (dto.getTaskGroupId() != null) {
            task.setTaskGroupId(dto.getTaskGroupId());
        }
        task.setParentId(targetParentId);
        task.setPath(newParentPath);
        task.setLevel(newLevel);
        task.setSortOrder(newSort);
        taskMapper.updateById(task);

        // 更新后代 path/level（保持树一致）
        List<Task> descendants = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .likeRight(Task::getPath, oldPrefix));

        if (!descendants.isEmpty()) {
            int levelDelta = newLevel - oldLevel;
            for (Task d : descendants) {
                String p = d.getPath();
                if (p != null && p.startsWith(oldPrefix)) {
                    d.setPath(newParentPath + task.getId() + "/" + p.substring(oldPrefix.length()));
                }
                if (d.getLevel() != null) {
                    d.setLevel(d.getLevel() + levelDelta);
                }
                // 同时对齐 list/taskGroup（跨分组移动时也要更新）
                d.setListId(dto.getListId());
                if (dto.getTaskGroupId() != null) {
                    d.setTaskGroupId(dto.getTaskGroupId());
                }
                taskMapper.updateById(d);
            }
        }

        boolean hasChildren = hasChildren(userId, taskId);
        return toResp(task, hasChildren);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TaskRespDTO> reorderTasks(Long userId, ReorderTasksReqDTO dto) {
        ensureListPermission(userId, dto.getListId());

        Long normalizedParentId = normalizeParentId(dto.getParentId());

        Task task = taskMapper.selectById(dto.getMovedId());
        if (task == null) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NOT_FOUND);
        }
        if (!Objects.equals(task.getUserId(), userId)) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NO_PERMISSION);
        }

        if (!Objects.equals(task.getListId(), dto.getListId())
                || !Objects.equals(task.getTaskGroupId(), dto.getTaskGroupId())
                || !Objects.equals(task.getParentId(), normalizedParentId)) {
            throw new ClientException(TaskErrorCodeEnum.TASK_SORT_ORDER_ERROR);
        }

        double newSort = computeSortOrder(dto.getPrevSortOrder(), dto.getNextSortOrder());
        if (Objects.equals(task.getSortOrder(), newSort) || (dto.getPrevSortOrder() != null && dto.getNextSortOrder() != null
                && (dto.getNextSortOrder() - dto.getPrevSortOrder()) < 1e-9)) {
            reindexContainer(userId, dto.getListId(), dto.getTaskGroupId(), normalizedParentId);
            newSort = computeSortOrder(dto.getPrevSortOrder(), dto.getNextSortOrder());
        }

        task.setSortOrder(newSort);
        taskMapper.updateById(task);

        return listTasks(userId, dto.getListId(), dto.getTaskGroupId(), normalizedParentId);
    }

    private void reindexContainer(Long userId, Long listId, Long taskGroupId, Long parentId) {
        Long normalizedParentId = normalizeParentId(parentId);

        List<Task> siblings = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getListId, listId)
                .eq(Task::getTaskGroupId, taskGroupId)
                .isNull(normalizedParentId == null, Task::getParentId)
                .eq(normalizedParentId != null, Task::getParentId, normalizedParentId)
                .orderByAsc(Task::getSortOrder)
                .orderByAsc(Task::getId));
        double current = GAP;
        for (Task t : siblings) {
            t.setSortOrder(current);
            current += GAP;
            taskMapper.updateById(t);
        }
    }

    private Long normalizeParentId(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return parentId <= 0 ? null : parentId;
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
            throw new ClientException(TaskErrorCodeEnum.TASK_SORT_ORDER_ERROR);
        }
        return (prev + next) / 2d;
    }

    private boolean hasChildren(Long userId, Long taskId) {
        Long count = taskMapper.selectCount(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getParentId, taskId));
        return count != null && count > 0;
    }

    private void ensureListPermission(Long userId, Long listId) {
        TodoList list = todoListMapper.selectById(listId);
        if (list == null) {
            throw new ClientException(TodoListErrorCodeEnum.TODO_LIST_NOT_FOUND);
        }
        if (!Objects.equals(list.getUserId(), userId)) {
            throw new ClientException(TaskErrorCodeEnum.TASK_NO_PERMISSION);
        }
    }

    private TaskRespDTO toResp(Task task, boolean hasChildren) {
        TaskRespDTO dto = new TaskRespDTO();
        dto.setId(task.getId());
        dto.setUserId(task.getUserId());
        dto.setListId(task.getListId());
        dto.setTaskGroupId(task.getTaskGroupId());
        dto.setParentId(task.getParentId());
        dto.setName(task.getName());
        dto.setContent(task.getContent());
        dto.setSortOrder(task.getSortOrder());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setStartedAt(task.getStartedAt());
        dto.setDueAt(task.getDueAt());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setHasChildren(hasChildren);
        return dto;
    }
}
