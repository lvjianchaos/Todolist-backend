package com.chaos.smarttodo.aiagent.service;

import com.chaos.smarttodo.aiagent.config.AiMemoryProperties;
import com.chaos.smarttodo.aiagent.model.ConversationMemory;
import com.chaos.smarttodo.aiagent.repo.ConversationRepository;
import com.chaos.smarttodo.aiagent.service.llm.DeepSeekPlanner;
import com.chaos.smarttodo.aiagent.service.nlp.SimpleChineseTaskParser;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ChatServiceTrimTest {

    @Test
    void shouldTrimToMaxMessagesAndPersist() {
        AtomicReference<ConversationMemory> saved = new AtomicReference<>();

        ConversationRepository repo = new ConversationRepository() {
            @Override
            public Optional<ConversationMemory> find(long userId, String conversationId) {
                return Optional.ofNullable(saved.get());
            }

            @Override
            public void save(long userId, String conversationId, ConversationMemory memory, long ttlSeconds) {
                saved.set(memory);
                assertThat(ttlSeconds).isEqualTo(1800);
            }
        };

        AiMemoryProperties props = new AiMemoryProperties();
        props.setMaxMessages(20);
        props.setTtlSeconds(1800);

        SimpleChineseTaskParser parser = new SimpleChineseTaskParser();
        DefaultTaskOrchestrator orchestrator = new DefaultTaskOrchestrator(null);

        // we don't need real DeepSeek planner in this unit test
        DeepSeekPlanner planner = new DeepSeekPlanner(null, null);

        ChatService service = new ChatService(repo, props, parser, orchestrator, planner, null);

        String cid = null;
        for (int i = 0; i < 25; i++) {
            ChatService.ChatResult result = service.chat(1L, cid, "msg" + i);
            cid = result.conversationId();
        }

        assertThat(saved.get()).isNotNull();
        assertThat(saved.get().getMessages()).hasSize(20);
        // last user message should be msg24
        assertThat(saved.get().getMessages().get(18).getContent()).isEqualTo("msg24");
        assertThat(saved.get().getMessages().get(18).getRole()).isEqualTo("user");
    }
}
