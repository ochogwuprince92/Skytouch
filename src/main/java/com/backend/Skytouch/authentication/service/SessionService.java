package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.entity.AuthSession;
import com.backend.Skytouch.authentication.repository.AuthSessionRepository;
import com.backend.Skytouch.authentication.security.JwtTokenService;
import com.backend.Skytouch.authentication.security.TokenHasher;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final TokenHasher tokenHasher;
    private final AuthProperties authProperties;

    @Transactional
    public String createSession(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String token = jwtTokenService.generateToken(user.getId(), user.getEmail(), user.getRole());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plus(authProperties.getSession().getExpirationMs(), ChronoUnit.MILLIS);

        AuthSession session = AuthSession.builder()
                .userId(userId)
                .tokenHash(tokenHasher.hash(token))
                .expiresAt(expiresAt)
                .build();
        authSessionRepository.save(session);

        return token;
    }

    @Transactional(readOnly = true)
    public UUID resolveUserId(String sessionToken) {
        UUID userId = jwtTokenService.resolveUserId(sessionToken);
        if (userId == null) {
            return null;
        }

        return authSessionRepository
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                        tokenHasher.hash(sessionToken),
                        LocalDateTime.now())
                .map(session -> userId)
                .orElse(null);
    }

    @Transactional
    public void revokeSession(String sessionToken) {
        authSessionRepository.findByTokenHash(tokenHasher.hash(sessionToken))
                .filter(session -> session.getRevokedAt() == null)
                .ifPresent(session -> {
                    session.setRevokedAt(LocalDateTime.now());
                    authSessionRepository.save(session);
                });
    }
}
