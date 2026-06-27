package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.apimodel.AuthResponse;
import com.backend.Skytouch.authentication.apimodel.LoginRequest;
import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.repository.AuthenticationRepository;
import com.backend.Skytouch.authentication.security.PasswordVerifier;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import com.backend.Skytouch.user.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private AuthenticationRepository authenticationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordVerifier passwordVerifier;
    @Mock private OtpService otpService;
    @Mock private SessionService sessionService;
    @Mock private EmailVerificationService emailVerificationService;
    @Mock private AuthProperties authProperties;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Users activeVerifiedUser;

    @BeforeEach
    void setUp() {
        activeVerifiedUser = Users.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("$2a$10$hashedpassword")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .active(true)
                .build();
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        AuthProperties.Session session = mock(AuthProperties.Session.class);
        when(session.getExpirationMs()).thenReturn(86_400_000L);
        when(authProperties.getSession()).thenReturn(session);
        when(authenticationRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeVerifiedUser));
        when(passwordVerifier.matches("Password123!", activeVerifiedUser.getPassword(), passwordEncoder))
                .thenReturn(true);
        when(passwordVerifier.isLegacyPassword(activeVerifiedUser.getPassword()))
                .thenReturn(false);
        when(sessionService.createSession(activeVerifiedUser.getId()))
                .thenReturn("mock-session-token");

        AuthResponse response = authenticationService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("mock-session-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.JOB_SEEKER);
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@example.com");
        request.setPassword("Password123!");

        when(authenticationRepository.findByEmail("ghost@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_shouldThrow_whenEmailNotVerified() {
        activeVerifiedUser.setEmailVerified(false);
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        when(authenticationRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeVerifiedUser));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Email not verified");
    }

    @Test
    void login_shouldThrow_whenPasswordWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword!");

        when(authenticationRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeVerifiedUser));
        when(passwordVerifier.matches("WrongPassword!", activeVerifiedUser.getPassword(), passwordEncoder))
                .thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_shouldThrow_whenAccountSuspended() {
        activeVerifiedUser.setStatus(UserStatus.SUSPENDED);
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        when(authenticationRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeVerifiedUser));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("suspended");
    }
}