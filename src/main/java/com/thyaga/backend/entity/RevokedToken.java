package com.thyaga.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "revoked_tokens")
public class RevokedToken extends BaseEntity {

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
