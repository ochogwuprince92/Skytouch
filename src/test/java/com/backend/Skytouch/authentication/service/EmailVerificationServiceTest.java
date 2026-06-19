package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.apimodel.EmailVerifiedResponse;
import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.apimodel.VerifyOtpRequest;
import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.repository.AuthenticationRepository;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import com.backend.Skytouch.user.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private AuthenticationRepository authenticationRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private Users pendingUser;

    @BeforeEach
    void setUp() {
        pendingUser = Users.builder()
                .id(UUID.randomUUID())
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.PENDING)
                .active(true)
                .emailVerified(false)
                .build();
    }

    @Test
    void sendVerificationCode_sendsOtpAndReturnsMaskedMessage() {
        AuthProperties.Otp otpProps = new AuthProperties.Otp();
        otpProps.setExpirationMs(600_000L);
        when(authProperties.getOtp()).thenReturn(otpProps);

        OtpSentResponse response = emailVerificationService.sendVerificationCode(pendingUser);

        verify(otpService).sendEmailVerificationOtp(pendingUser.getId(), pendingUser.getEmail());
        assertThat(response.getMessage()).contains("@example.com");
        assertThat(response.getExpiresIn()).isEqualTo(600_000L);
    }

    @Test
    void verifyEmail_withValidCode_activatesAccount() {
        when(authenticationRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(pendingUser));

        EmailVerifiedResponse response = emailVerificationService.verifyEmail(
                new VerifyOtpRequest("seeker@example.com", "123456"));

        verify(otpService).verifyEmailVerificationOtp(pendingUser.getId(), "123456");
        verify(authenticationRepository).save(pendingUser);
        assertThat(pendingUser.getEmailVerified()).isTrue();
        assertThat(pendingUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getMessage()).isEqualTo("Email verified successfully");
    }

    @Test
    void verifyEmail_whenAlreadyVerified_throwsBadRequest() {
        pendingUser.setEmailVerified(true);
        when(authenticationRepository.findByEmail("seeker@example.com")).thenReturn(Optional.of(pendingUser));

        assertThatThrownBy(() -> emailVerificationService.verifyEmail(
                new VerifyOtpRequest("seeker@example.com", "123456")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already verified");

        verify(otpService, never()).verifyEmailVerificationOtp(eq(pendingUser.getId()), eq("123456"));
    }

    @Test
    void resendVerificationCode_whenAccountMissing_throwsNotFound() {
        when(authenticationRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.resendVerificationCode("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(otpService, never()).sendEmailVerificationOtp(eq(pendingUser.getId()), eq(pendingUser.getEmail()));
    }
}
