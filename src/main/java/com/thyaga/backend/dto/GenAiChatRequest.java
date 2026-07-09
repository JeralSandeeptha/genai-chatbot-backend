package com.thyaga.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record GenAiChatRequest(
        @NotEmpty @Valid List<GenAiMessage> messages,
        UUID conversationId,
        String model
) {
    public record GenAiMessage(
            @NotBlank String role,
            @NotBlank String content,
            String imageData,
            String documentName,
            String documentText
    ) {
    }
}
