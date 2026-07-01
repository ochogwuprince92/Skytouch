package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.entity.AuthSession;
import com.backend.Skytouch.authentication.repository.AuthSessionRepository;
import com.backend.Skytouch.authentication.security.JwtTokenService;
import com.backend.Skytouch.authentication.security.TokenHasher;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock private JwtTokenService jwtTokenService;
    @Mock private UserRepository userRepository;
    @Mock private AuthSessionRepository authSessionRepository;
    @Mock private TokenHasher tokenHasher;
    @Mock private AuthProperties authProperties;

    @InjectMocks
    private SessionService sessionService;

    private UUID userId;
    private Users user;
    private String token;
    private String tokenHash;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        token = "jwt-token";
        tokenHash = "hashed-token";
        user = Users.builder()
                .id(userId)
                .email("user@example.com")
                .role(UserRole.JOB_SEEKER)
                .build();
    }

    @Test
    void createSession_persistsHashedToken() {
        AuthProperties.Session session = new AuthProperties.Session();
        session.setExpirationMs(86_400_000L);
        when(authProperties.getSession()).thenReturn(session);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtTokenService.generateToken(userId, user.getEmail(), user.getRole())).thenReturn(token);
        when(tokenHasher.hash(token)).thenReturn(tokenHash);
        when(authSessionRepository.save(any(AuthSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = sessionService.createSession(userId);

        assertThat(result).isEqualTo(token);
        ArgumentCaptor<AuthSession> captor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getTokenHash()).isEqualTo(tokenHash);
        assertThat(captor.getValue().getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void resolveUserId_returnsUserWhenSessionActive() {
        AuthSession session = AuthSession.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(jwtTokenService.resolveUserId(token)).thenReturn(userId);
        when(tokenHasher.hash(token)).thenReturn(tokenHash);
        when(authSessionRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                eq(tokenHash), any(LocalDateTime.class))).thenReturn(Optional.of(session));

        assertThat(sessionService.resolveUserId(token)).isEqualTo(userId);
    }

    @Test
    void resolveUserId_returnsNullWhenSessionRevoked() {
        when(jwtTokenService.resolveUserId(token)).thenReturn(userId);
        when(tokenHasher.hash(token)).thenReturn(tokenHash);
        when(authSessionRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(
                eq(tokenHash), any(LocalDateTime.class))).thenReturn(Optional.empty());

        assertThat(sessionService.resolveUserId(token)).isNull();
    }

    @Test
    void revokeSession_setsRevokedAt() {
        AuthSession session = AuthSession.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenHasher.hash(token)).thenReturn(tokenHash);
        when(authSessionRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(session));
        when(authSessionRepository.save(session)).thenReturn(session);

        sessionService.revokeSession(token);

        assertThat(session.getRevokedAt()).isNotNull();
        verify(authSessionRepository).save(session);
    }
}
