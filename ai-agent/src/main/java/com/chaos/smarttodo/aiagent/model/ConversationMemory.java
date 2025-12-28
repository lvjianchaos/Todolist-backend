package com.chaos.smarttodo.aiagent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemory {

    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * For intents like "把刚刚那个任务...", we remember the last created task.
     */
    private Long lastTaskId;

    private Long lastListId;
}
