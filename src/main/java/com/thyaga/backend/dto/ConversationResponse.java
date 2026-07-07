package com.thyaga.backend.dto;

import com.thyaga.backend.entity.Conversation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String title,
        UUID userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getUser().getId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
