package com.chaos.smarttodo.aiagent.service;

import com.chaos.smarttodo.aiagent.client.TaskClient;
import com.chaos.smarttodo.aiagent.client.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefaultTaskOrchestrator {

    public static final String DEFAULT_LIST_GROUP_NAME = "默认分组";
    public static final String DEFAULT_LIST_NAME = "默认清单";

    private final TaskClient taskClient;

    public CreatedTask createDefaultTask(long userId,
                                        String name,
                                        String content,
                                        LocalDate startedAt,
                                        LocalDate dueAt,
                                        Integer priority) {
        // 1) ensure default list group
        ListGroupRespDTO group = ensureDefaultListGroup(userId);

        // 2) ensure default list
        TodoListRespDTO list = ensureDefaultList(userId, group);

        // 3) ensure task group
        TaskGroupRespDTO taskGroup = ensureDefaultTaskGroup(userId, list.getId());

        // 4) create task
        CreateTaskReqDTO createTaskReq = new CreateTaskReqDTO();
        createTaskReq.setListId(list.getId());
        createTaskReq.setTaskGroupId(taskGroup.getId());
        createTaskReq.setParentId(null);
        createTaskReq.setName(name);
        createTaskReq.setPrevSortOrder(0D);

        TaskRespDTO created = requireSuccess(taskClient.createTask(userId, createTaskReq), "createTask");

        // 5) patch details
        PatchTaskReqDTO patch = new PatchTaskReqDTO();
        boolean needPatch = false;
        if (content != null && !content.isBlank()) {
            patch.setContent(content);
            needPatch = true;
        }
        if (startedAt != null) {
            patch.setStartedAt(startedAt);
            needPatch = true;
        }
        if (dueAt != null) {
            patch.setDueAt(dueAt);
            needPatch = true;
        }
        if (priority != null) {
            patch.setPriority(priority);
            needPatch = true;
        }
        if (needPatch) {
            created = requireSuccess(taskClient.patchTask(userId, created.getId(), patch), "patchTask");
        }

        return new CreatedTask(group.getId(), list.getId(), taskGroup.getId(), created.getId(), created);
    }

    // Backward compatible overload
    public CreatedTask createDefaultTask(long userId,
                                        String name,
                                        String content,
                                        LocalDate startedAt,
                                        LocalDate dueAt) {
        return createDefaultTask(userId, name, content, startedAt, dueAt, null);
    }

    private ListGroupRespDTO ensureDefaultListGroup(long userId) {
        List<ListGroupRespDTO> groups = requireSuccess(taskClient.listListGroups(userId), "listListGroups");
        if (groups != null) {
            for (ListGroupRespDTO g : groups) {
                if (Objects.equals(DEFAULT_LIST_GROUP_NAME, g.getName())) {
                    return g;
                }
            }
        }

        CreateListGroupReqDTO req = new CreateListGroupReqDTO();
        req.setName(DEFAULT_LIST_GROUP_NAME);
        req.setPrevSortOrder(0D);
        return requireSuccess(taskClient.createListGroup(userId, req), "createListGroup");
    }

    private TodoListRespDTO ensureDefaultList(long userId, ListGroupRespDTO group) {
        if (group.getList() != null) {
            for (TodoListRespDTO l : group.getList()) {
                if (Objects.equals(DEFAULT_LIST_NAME, l.getName())) {
                    return l;
                }
            }
        }

        CreateListReqDTO req = new CreateListReqDTO();
        req.setGroupId(group.getId());
        req.setName(DEFAULT_LIST_NAME);

        double prevSort = 0D;
        if (group.getList() != null && !group.getList().isEmpty()) {
            prevSort = group.getList().stream()
                    .map(TodoListRespDTO::getSortOrder)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(0D);
        }
        req.setPrevSortOrder(prevSort);

        return requireSuccess(taskClient.createList(userId, req), "createList");
    }

    private TaskGroupRespDTO ensureDefaultTaskGroup(long userId, long listId) {
        List<TaskGroupRespDTO> groups = requireSuccess(taskClient.listTaskGroups(userId, listId), "listTaskGroups");
        if (groups != null && !groups.isEmpty()) {
            for (TaskGroupRespDTO g : groups) {
                if (g.getIsDefault() != null && g.getIsDefault() == 1) {
                    return g;
                }
            }
            return groups.get(0);
        }

        CreateTaskGroupReqDTO req = new CreateTaskGroupReqDTO();
        req.setListId(listId);
        req.setName("默认任务组");
        req.setPrevSortOrder(0D);
        return requireSuccess(taskClient.createTaskGroup(userId, req), "createTaskGroup");
    }

    private <T> T requireSuccess(TaskResult<T> result, String op) {
        if (result == null) {
            throw new IllegalStateException(op + " failed: empty response");
        }
        if (!result.isSuccess()) {
            throw new IllegalStateException(op + " failed: " + result.getCode() + " " + result.getMessage());
        }
        return result.getData();
    }

    public record CreatedTask(Long listGroupId, Long listId, Long taskGroupId, Long taskId, TaskRespDTO task) {
    }
}
