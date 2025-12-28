package com.chaos.smarttodo.aiagent.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatReqDTO {

    /**
     * If omitted, server generates a new one.
     */
    private String conversationId;

    @NotBlank
    private String message;
}
