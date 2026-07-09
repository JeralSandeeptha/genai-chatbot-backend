package com.thyaga.backend.dto;

import com.thyaga.backend.entity.MessageRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMessageRequest(
        @NotNull UUID conversationId,
        @NotNull MessageRole role,
        @NotBlank String content,
        String imageData,
        String documentName,
        String documentText
) {}
