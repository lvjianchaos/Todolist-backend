package com.chaos.smarttodo.aiAgent.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, chatModel = "qwenChatModel",  chatMemory = "chatMemory",  chatMemoryProvider = "chatMemoryProvider")
public interface Assistant {
    @SystemMessage(fromResource = "prompt.txt")
    String chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
