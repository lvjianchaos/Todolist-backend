package com.chaos.smarttodo.aiagent.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Uses Spring AI (OpenAI-compatible) to ask DeepSeek for a strict JSON plan.
 */
@Component
@RequiredArgsConstructor
public class DeepSeekPlanner {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.system-prompt:}")
    private String systemPrompt;

    public AiPlan plan(String userInput) {
        if (chatClient == null || objectMapper == null) {
            return unknown();
        }

        String schemaHint = "{\n" +
                "  \"intent\": \"create_task\" | \"update_task\" | \"query_tasks\" | \"unknown\",\n" +
                "  \"task\": {\n" +
                "    \"name\": string|null,\n" +
                "    \"content\": string|null,\n" +
                "    \"startedAt\": \"yyyy-MM-dd\"|null,\n" +
                "    \"dueAt\": \"yyyy-MM-dd\"|null,\n" +
                "    \"priority\": 0|1|2|3|null,\n" +
                "    \"status\": 0|1|2|null\n" +
                "  },\n" +
                "  \"query\": {\n" +
                "    \"dateField\": \"dueAt\" | \"startedAt\" | null,\n" +
                "    \"from\": \"yyyy-MM-dd\"|null,\n" +
                "    \"to\": \"yyyy-MM-dd\"|null\n" +
                "  }\n" +
                "}";

        String strictRule = "IMPORTANT: Output ONLY a single JSON object. " +
                "Do not use markdown/code fences. Do not add explanations. Do not add extra keys.";

        String raw = chatClient.prompt()
                .system((systemPrompt == null ? "" : systemPrompt) + "\n" + strictRule + "\n\nJSON schema hint:\n" + schemaHint)
                .user(userInput)
                .call()
                .content();

        AiPlan first = tryParse(raw);
        if (first != null) {
            return first;
        }

        // 2nd pass: ask the model to repair its own output to valid JSON.
        String repairPrompt = "Your previous output was not valid JSON for the schema. " +
                "Fix it and output ONLY the corrected JSON object.\n\n" +
                "Schema hint:\n" + schemaHint + "\n\n" +
                "Previous output:\n" + raw;

        String repairedRaw = chatClient.prompt()
                .system(strictRule)
                .user(repairPrompt)
                .call()
                .content();

        AiPlan repaired = tryParse(repairedRaw);
        return repaired != null ? repaired : unknown();
    }

    private AiPlan tryParse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String json = JsonExtractors.extractFirstJsonObject(raw);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, AiPlan.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private AiPlan unknown() {
        AiPlan fallback = new AiPlan();
        fallback.setIntent("unknown");
        return fallback;
    }
}
