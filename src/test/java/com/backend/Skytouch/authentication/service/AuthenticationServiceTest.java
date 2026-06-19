package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.apimodel.AuthResponse;
import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.apimodel.RequestOtpRequest;
import com.backend.Skytouch.authentication.apimodel.VerifyOtpRequest;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationRepository authenticationRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private SessionService sessionService;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordVerifier passwordVerifier = new PasswordVerifier();

    private Users activeUser;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                authenticationRepository,
                passwordEncoder,
                passwordVerifier,
                otpService,
                sessionService,
                authProperties
        );

        activeUser = Users.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .password("plain-password")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .active(true)
                .emailVerified(true)
                .build();
    }

    @Test
    void requestOtp_withLegacyPassword_upgradesHashAndSendsOtp() {
        when(authenticationRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        AuthProperties.Otp otpProps = new AuthProperties.Otp();
        otpProps.setExpirationMs(600_000L);
        when(authProperties.getOtp()).thenReturn(otpProps);

        OtpSentResponse response = authenticationService.requestOtp(
                new RequestOtpRequest("user@example.com", "plain-password"));

        assertThat(response.getExpiresIn()).isEqualTo(600_000L);
        assertThat(response.getMessage()).contains("@example.com");
        verify(authenticationRepository).save(activeUser);
        assertThat(activeUser.getPassword()).startsWith("$2a$");
        verify(otpService).sendLoginOtp(activeUser.getId(), activeUser.getEmail());
    }

    @Test
    void requestOtp_withInvalidPassword_throwsUnauthorized() {
        when(authenticationRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authenticationService.requestOtp(
                new RequestOtpRequest("user@example.com", "wrong-password")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        verify(otpService, never()).sendLoginOtp(any(), any());
    }

    @Test
    void requestOtp_withSuspendedAccount_throwsUnauthorized() {
        activeUser.setStatus(UserStatus.SUSPENDED);
        when(authenticationRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authenticationService.requestOtp(
                new RequestOtpRequest("user@example.com", "plain-password")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Account is suspended");
    }

    @Test
    void verifyOtp_withValidCode_returnsSessionToken() {
        when(authenticationRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        when(sessionService.createSession(activeUser.getId())).thenReturn("session-token");
        AuthProperties.Session sessionProps = new AuthProperties.Session();
        sessionProps.setExpirationMs(86_400_000L);
        when(authProperties.getSession()).thenReturn(sessionProps);

        AuthResponse response = authenticationService.verifyOtp(
                new VerifyOtpRequest("user@example.com", "123456"));

        assertThat(response.getAccessToken()).isEqualTo("session-token");
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.JOB_SEEKER);
        verify(otpService).verifyLoginOtp(activeUser.getId(), "123456");
        verify(sessionService).createSession(activeUser.getId());
    }

    @Test
    void requestOtp_withUnverifiedEmail_throwsUnauthorized() {
        activeUser.setEmailVerified(false);
        when(authenticationRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authenticationService.requestOtp(
                new RequestOtpRequest("user@example.com", "plain-password")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Email verification required. Check your inbox or request a new code.");

        verify(otpService, never()).sendLoginOtp(any(), any());
    }

    @Test
    void verifyOtp_withInvalidCode_throwsUnauthorized() {
        when(authenticationRepository.findByEmail("user@example.com")).thenReturn(Optional.of(activeUser));
        doThrow(new UnauthorizedException("Invalid or expired OTP"))
                .when(otpService).verifyLoginOtp(eq(activeUser.getId()), eq("000000"));

        assertThatThrownBy(() -> authenticationService.verifyOtp(
                new VerifyOtpRequest("user@example.com", "000000")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired OTP");

        verify(sessionService, never()).createSession(any());
    }
}
