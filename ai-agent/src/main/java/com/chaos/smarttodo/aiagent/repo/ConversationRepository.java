package com.chaos.smarttodo.aiagent.repo;

import com.chaos.smarttodo.aiagent.model.ConversationMemory;

import java.util.Optional;

public interface ConversationRepository {

    Optional<ConversationMemory> find(long userId, String conversationId);

    void save(long userId, String conversationId, ConversationMemory memory, long ttlSeconds);
}

