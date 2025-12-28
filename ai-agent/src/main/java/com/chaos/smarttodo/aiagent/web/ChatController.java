package com.chaos.smarttodo.aiagent.web;

import com.chaos.smarttodo.aiagent.service.ChatService;
import com.chaos.smarttodo.aiagent.web.dto.ChatReqDTO;
import com.chaos.smarttodo.aiagent.web.dto.ChatRespDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai-agent")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatRespDTO chat(@RequestHeader("X-User-Id") Long userId,
                            @Valid @RequestBody ChatReqDTO dto) {
        ChatService.ChatResult result = chatService.chat(userId, dto.getConversationId(), dto.getMessage());
        return new ChatRespDTO(result.conversationId(), result.reply());
    }
}
