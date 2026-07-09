package com.thyaga.backend.dto;

public record UpdateMessageRequest(
        String content,
        String imageData,
        String documentName,
        String documentText
) {}
