package com.thyaga.backend.dto;

import java.util.UUID;

public record AuthResponse(
        UUID id,
        String email
) {}
