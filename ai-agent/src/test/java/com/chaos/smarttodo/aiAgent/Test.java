package com.chaos.smarttodo.aiAgent;

import com.chaos.smarttodo.aiAgent.assistant.Assistant;
import com.chaos.smarttodo.aiAgent.entity.ChatMessages;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
public class Test {
    @Autowired
    private QwenChatModel qwenChatModel;
    @Autowired
    private Assistant assistant;
    @Autowired
    private MongoTemplate mongoTemplate;

    @org.junit.jupiter.api.Test
    public void test1() {
        String answer1 = assistant.chat(1L, "你是谁");
        System.out.println(answer1);
    }
    @org.junit.jupiter.api.Test
    public void test2() {
        ChatMessages chatMessages = new ChatMessages();
        chatMessages.setContent("聊天记录列表");
        mongoTemplate.insert(chatMessages);
    }
}

