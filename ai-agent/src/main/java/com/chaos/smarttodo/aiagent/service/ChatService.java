package com.chaos.smarttodo.aiagent.service;

import com.chaos.smarttodo.aiagent.config.AiMemoryProperties;
import com.chaos.smarttodo.aiagent.client.TaskClient;
import com.chaos.smarttodo.aiagent.client.dto.PatchTaskReqDTO;
import com.chaos.smarttodo.aiagent.client.dto.TaskRespDTO;
import com.chaos.smarttodo.aiagent.model.ChatMessage;
import com.chaos.smarttodo.aiagent.model.ConversationMemory;
import com.chaos.smarttodo.aiagent.repo.ConversationRepository;
import com.chaos.smarttodo.aiagent.service.llm.AiPlan;
import com.chaos.smarttodo.aiagent.service.llm.DeepSeekPlanner;
import com.chaos.smarttodo.aiagent.service.nlp.SimpleChineseTaskParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final AiMemoryProperties memoryProperties;

    private final SimpleChineseTaskParser simpleChineseTaskParser;
    private final DefaultTaskOrchestrator defaultTaskOrchestrator;
    private final DeepSeekPlanner deepSeekPlanner;

    private final TaskClient taskClient;

    public ChatResult chat(long userId, String conversationId, String message) {
        String cid = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString()
                : conversationId;

        ConversationMemory memory = conversationRepository.find(userId, cid)
                .orElseGet(ConversationMemory::new);

        List<ChatMessage> messages = memory.getMessages();
        if (messages == null) {
            messages = new ArrayList<>();
            memory.setMessages(messages);
        }

        messages.add(ChatMessage.user(message));

        String assistantReply = handleMessage(userId, message, memory);
        messages.add(ChatMessage.assistant(assistantReply));

        trimToMax(messages, memoryProperties.getMaxMessages());
        conversationRepository.save(userId, cid, memory, memoryProperties.getTtlSeconds());

        return new ChatResult(cid, assistantReply, memory);
    }

    private String handleMessage(long userId, String message, ConversationMemory memory) {
        AiPlan plan = deepSeekPlanner.plan(message);
        if (plan != null) {
            String intent = plan.getIntent() == null ? "unknown" : plan.getIntent();
            switch (intent) {
                case "create_task" -> {
                    AiPlan.TaskDraft task = plan.getTask();
                    if (task == null || task.getName() == null || task.getName().isBlank()) {
                        return "我理解你想创建任务，但任务名称不明确。你可以说：帮我创建任务XXX，截止到下周五。";
                    }
                    try {
                        DefaultTaskOrchestrator.CreatedTask created = defaultTaskOrchestrator.createDefaultTask(
                                userId,
                                task.getName(),
                                task.getContent(),
                                task.getStartedAt(),
                                task.getDueAt(),
                                task.getPriority()
                        );
                        memory.setLastTaskId(created.taskId());
                        memory.setLastListId(created.listId());

                        return "已为你创建任务：" + created.task().getName()
                                + "（taskId=" + created.taskId() + "）";
                    } catch (Exception e) {
                        return "我理解你想创建任务，但执行失败了：" + e.getMessage();
                    }
                }
                case "update_task" -> {
                    Long taskId = memory.getLastTaskId();
                    if (taskId == null) {
                        return "我可以帮你更新任务，但我还不知道你要改哪一个任务。你可以先创建一个任务，或在指令里带上任务ID。";
                    }
                    AiPlan.TaskDraft task = plan.getTask();
                    if (task == null) {
                        return "你想更新任务的哪些字段？比如：把刚刚那个任务截止日期改到下周五。";
                    }

                    PatchTaskReqDTO patch = new PatchTaskReqDTO();
                    boolean changed = false;
                    if (task.getName() != null) {
                        patch.setName(task.getName());
                        changed = true;
                    }
                    if (task.getContent() != null) {
                        patch.setContent(task.getContent());
                        changed = true;
                    }
                    if (task.getStartedAt() != null) {
                        patch.setStartedAt(task.getStartedAt());
                        changed = true;
                    }
                    if (task.getDueAt() != null) {
                        patch.setDueAt(task.getDueAt());
                        changed = true;
                    }
                    if (task.getPriority() != null) {
                        patch.setPriority(task.getPriority());
                        changed = true;
                    }
                    if (task.getStatus() != null) {
                        patch.setStatus(task.getStatus());
                        changed = true;
                    }

                    if (!changed) {
                        return "我没有识别到需要修改的字段。你可以说：把刚刚那个任务的截止日期改到yyyy-MM-dd。";
                    }

                    try {
                        TaskRespDTO updated = requireSuccess(taskClient.patchTask(userId, taskId, patch), "patchTask");
                        return "已更新任务：" + updated.getName() + "（taskId=" + updated.getId() + "）";
                    } catch (Exception e) {
                        return "更新任务失败：" + e.getMessage();
                    }
                }
                case "query_tasks" -> {
                    AiPlan.QueryDraft query = plan.getQuery();
                    LocalDate from = query == null ? null : query.getFrom();
                    LocalDate to = query == null ? null : query.getTo();
                    String field = query == null ? null : query.getDateField();
                    if (field == null || field.isBlank()) {
                        field = "dueAt";
                    }
                    if (from == null && to == null) {
                        from = LocalDate.now();
                        to = from.plusDays(7);
                    } else if (from == null) {
                        from = to;
                    } else if (to == null) {
                        to = from;
                    }

                    final LocalDate finalFrom = from;
                    final LocalDate finalTo = to;
                    final String finalField = field;

                    try {
                        List<TaskRespDTO> tasks = requireSuccess(taskClient.listRootTasks(userId, 0L), "listRootTasks");
                        List<TaskRespDTO> filtered = tasks.stream()
                                .filter(t -> inRange(finalField, t, finalFrom, finalTo))
                                .toList();

                        if (filtered.isEmpty()) {
                            return "在" + finalFrom + "到" + finalTo + "之间没有找到相关任务。";
                        }

                        StringBuilder sb = new StringBuilder();
                        sb.append("在").append(finalFrom).append("到").append(finalTo).append("之间的任务：\n");
                        for (TaskRespDTO t : filtered) {
                            sb.append("- [").append(t.getId()).append("] ")
                                    .append(t.getName());
                            if ("startedAt".equals(finalField) && t.getStartedAt() != null) {
                                sb.append("（开始：").append(t.getStartedAt()).append("）");
                            }
                            if ("dueAt".equals(finalField) && t.getDueAt() != null) {
                                sb.append("（截止：").append(t.getDueAt()).append("）");
                            }
                            sb.append("\n");
                        }
                        return sb.toString();
                    } catch (Exception e) {
                        return "查询任务失败：" + e.getMessage();
                    }
                }
                default -> {
                    // fall through to deterministic parser
                }
            }
        }

        // Fallback to deterministic parser (create only)
        SimpleChineseTaskParser.ParseResult parsed = simpleChineseTaskParser.parse(message);
        if (parsed != null && parsed.isMatched()) {
            try {
                DefaultTaskOrchestrator.CreatedTask created = defaultTaskOrchestrator.createDefaultTask(
                        userId,
                        parsed.getTaskName(),
                        parsed.getContent(),
                        parsed.getStartedAt(),
                        parsed.getDueAt()
                );
                memory.setLastTaskId(created.taskId());
                memory.setLastListId(created.listId());

                return "已为你创建任务：" + created.task().getName()
                        + "（taskId=" + created.taskId() + "）";
            } catch (Exception e) {
                return "我理解你想创建任务，但执行失败了：" + e.getMessage();
            }
        }

        return "已收到：" + message + "。我还不太确定你的意图。你可以说：创建任务XXX / 把刚刚那个任务截止日期改到下周五 / 我这周有哪些任务。";
    }

    private boolean inRange(String field, TaskRespDTO t, LocalDate from, LocalDate to) {
        LocalDate d = "startedAt".equals(field) ? t.getStartedAt() : t.getDueAt();
        if (d == null) {
            return false;
        }
        return (d.isEqual(from) || d.isAfter(from)) && (d.isEqual(to) || d.isBefore(to));
    }

    private <T> T requireSuccess(com.chaos.smarttodo.aiagent.client.dto.TaskResult<T> result, String op) {
        if (result == null) {
            throw new IllegalStateException(op + " failed: empty response");
        }
        if (!result.isSuccess()) {
            throw new IllegalStateException(op + " failed: " + result.getCode() + " " + result.getMessage());
        }
        return result.getData();
    }

    private void trimToMax(List<ChatMessage> messages, int max) {
        if (max <= 0) {
            return;
        }
        int size = messages.size();
        if (size <= max) {
            return;
        }
        int from = size - max;
        messages.subList(0, from).clear();
    }

    public record ChatResult(String conversationId, String reply, ConversationMemory memory) {
    }
}
