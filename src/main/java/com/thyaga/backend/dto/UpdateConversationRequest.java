package com.thyaga.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateConversationRequest(
        @NotBlank String title
) {}
