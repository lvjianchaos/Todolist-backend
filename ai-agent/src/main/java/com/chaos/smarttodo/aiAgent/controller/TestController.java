package com.chaos.smarttodo.aiAgent.controller;

import com.chaos.smarttodo.aiAgent.assistant.Assistant;
import com.chaos.smarttodo.aiAgent.entity.ChatForm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aiAgent")
@RequiredArgsConstructor
public class TestController {
    @Autowired
    private Assistant assistant;
    @PostMapping("/chat")
    public String chat(@RequestBody ChatForm chatForm)  {
        return assistant.chat(chatForm.getMemoryId(), chatForm.getMessage());
    }
}
