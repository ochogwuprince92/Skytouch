package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.entity.AuthSession;
import com.backend.Skytouch.authentication.repository.AuthSessionRepository;
import com.backend.Skytouch.authentication.security.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final AuthSessionRepository authSessionRepository;
    private final AuthProperties authProperties;
    private final TokenHasher tokenHasher;

    @Transactional
    public String createSession(UUID userId) {
        String sessionToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        AuthSession session = AuthSession.builder()
                .userId(userId)
                .tokenHash(tokenHasher.hash(sessionToken))
                .expiresAt(now.plus(Duration.ofMillis(authProperties.getSession().getExpirationMs())))
                .build();

        authSessionRepository.save(session);
        return sessionToken;
    }

    @Transactional(readOnly = true)
    public UUID resolveUserId(String sessionToken) {
        return authSessionRepository
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                        tokenHasher.hash(sessionToken), LocalDateTime.now())
                .map(AuthSession::getUserId)
                .orElse(null);
    }
}
