package com.chaos.smarttodo.aiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.memory")
public class AiMemoryProperties {

    /**
     * Sliding expiration for conversation memory.
     */
    private long ttlSeconds = 1800;

    /**
     * Max number of messages stored per conversation.
     * 10 rounds ~= 20 messages (user+assistant).
     */
    private int maxMessages = 20;
}
