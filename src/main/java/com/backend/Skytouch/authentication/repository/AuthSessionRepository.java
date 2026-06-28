package com.backend.Skytouch.authentication.repository;

import com.backend.Skytouch.authentication.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
            String tokenHash, LocalDateTime now);
}
