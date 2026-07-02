package com.backend.Skytouch.authentication.service;

import com.backend.Skytouch.authentication.apimodel.AuthResponse;
import com.backend.Skytouch.authentication.apimodel.ChangePasswordRequest;
import com.backend.Skytouch.authentication.apimodel.DeactivateAccountRequest;
import com.backend.Skytouch.authentication.apimodel.LoginRequest;
import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.apimodel.RegisterRequest;
import com.backend.Skytouch.authentication.config.AuthProperties;
import com.backend.Skytouch.authentication.repository.AuthenticationRepository;
import com.backend.Skytouch.authentication.security.PasswordVerifier;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.enums.UserType;
import com.backend.Skytouch.common.exception.UnauthorizedException;
import com.backend.Skytouch.common.mapper.EmployerMapper;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
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
    @Mock private UserRepository userRepository;
    @Mock private JobSeekerRepository jobSeekerRepository;
    @Mock private EmployerRepository employerRepository;
    @Mock private JobSeekerMapper jobSeekerMapper;
    @Mock private EmployerMapper employerMapper;

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

    // ─── Register ───────────────────────────────────────────────────────────

    @Test
    void register_shouldCreateJobSeeker_whenUserTypeIsJobSeeker() {
        RegisterRequest request = new RegisterRequest();
        request.setUserType(UserType.JOB_SEEKER);
        request.setEmail("seeker@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");
        request.setPhone("+2348012345678");

        Users savedUser = Users.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.PENDING)
                .emailVerified(false)
                .active(true)
                .build();
        JobSeeker profile = JobSeeker.builder().user(savedUser).status(UserStatus.PENDING).build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);
        when(jobSeekerMapper.toEntity(request, savedUser)).thenReturn(profile);
        when(emailVerificationService.sendVerificationCode(savedUser))
                .thenReturn(OtpSentResponse.builder().message("sent").expiresIn(600000L).build());

        var response = authenticationService.register(request);

        assertThat(response.getRole()).isEqualTo(UserRole.JOB_SEEKER);
        verify(jobSeekerRepository).save(profile);
        verify(employerRepository, never()).save(any());
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

    @Test
    void changePassword_updatesPasswordAndRevokesSessions() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass123!");
        request.setNewPassword("NewPass123!");
        request.setConfirmPassword("NewPass123!");

        when(authenticationRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeVerifiedUser));
        when(passwordVerifier.matches("OldPass123!", activeVerifiedUser.getPassword(), passwordEncoder))
                .thenReturn(true);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("new-hash");

        var response = authenticationService.changePassword("test@example.com", request);

        assertThat(response.getMessage()).contains("Password updated");
        verify(authenticationRepository).save(activeVerifiedUser);
        verify(sessionService).revokeAllSessions(activeVerifiedUser.getId());
    }

    @Test
    void deactivateAccount_suspendsUserAndRevokesSessions() {
        DeactivateAccountRequest request = new DeactivateAccountRequest();
        request.setPassword("Password123!");

        when(authenticationRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(activeVerifiedUser));
        when(passwordVerifier.matches("Password123!", activeVerifiedUser.getPassword(), passwordEncoder))
                .thenReturn(true);

        var response = authenticationService.deactivateAccount("test@example.com", request);

        assertThat(response.getMessage()).contains("deactivated");
        assertThat(activeVerifiedUser.getActive()).isFalse();
        assertThat(activeVerifiedUser.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        verify(sessionService).revokeAllSessions(activeVerifiedUser.getId());
    }
}