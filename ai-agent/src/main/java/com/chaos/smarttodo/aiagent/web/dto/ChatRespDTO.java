package com.chaos.smarttodo.aiagent.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRespDTO {
    private String conversationId;
    private String reply;
}
