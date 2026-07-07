package com.thyaga.backend.repository;

import com.thyaga.backend.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {

    boolean existsByToken(String token);
}
