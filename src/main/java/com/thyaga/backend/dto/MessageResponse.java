package com.thyaga.backend.dto;

import com.thyaga.backend.entity.Message;
import com.thyaga.backend.entity.MessageRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        MessageRole role,
        String content,
        String imageData,
        UUID conversationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getImageData(),
                message.getConversation().getId(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
