package com.chaos.smarttodo.aiagent.repo;

import com.chaos.smarttodo.aiagent.model.ConversationMemory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisConversationRepository implements ConversationRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<ConversationMemory> find(long userId, String conversationId) {
        String key = key(userId, conversationId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, ConversationMemory.class));
        } catch (Exception e) {
            // If corrupted/unexpected, treat as missing to avoid breaking chat.
            return Optional.empty();
        }
    }

    @Override
    public void save(long userId, String conversationId, ConversationMemory memory, long ttlSeconds) {
        String key = key(userId, conversationId);
        try {
            String json = objectMapper.writeValueAsString(memory);
            redisTemplate.opsForValue().set(key, json, Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ConversationMemory", e);
        } catch (DataAccessException e) {
            throw e;
        }
    }

    private String key(long userId, String conversationId) {
        return "ai:conv:" + userId + ":" + conversationId;
    }
}

