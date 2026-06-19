package com.backend.Skytouch.jobseeker.service;

import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.service.EmailVerificationService;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.apimodel.RegisterJobSeekerRequest;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSeekerServiceTest {

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobSeekerMapper jobSeekerMapper;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private JobSeekerService jobSeekerService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        jobSeekerService = new JobSeekerService(
                jobSeekerRepository,
                userRepository,
                jobSeekerMapper,
                passwordEncoder,
                emailVerificationService
        );
    }

    @Test
    void register_hashesPasswordBeforeSaveAndSendsVerificationCode() {
        RegisterJobSeekerRequest request = new RegisterJobSeekerRequest("seeker@example.com", "password123");
        when(userRepository.existsByEmail("seeker@example.com")).thenReturn(false);

        Users savedUser = Users.builder()
                .id(UUID.randomUUID())
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.PENDING)
                .build();
        when(jobSeekerRepository.save(any(Users.class))).thenReturn(savedUser);
        when(emailVerificationService.sendVerificationCode(savedUser)).thenReturn(
                OtpSentResponse.builder()
                        .message("Verification code sent to s***@example.com")
                        .expiresIn(600_000L)
                        .build());
        when(jobSeekerMapper.toRegisterResponse(eq(savedUser), any(OtpSentResponse.class))).thenReturn(
                com.backend.Skytouch.jobseeker.apimodel.RegisterJobSeekerResponse.builder()
                        .id(savedUser.getId())
                        .email(savedUser.getEmail())
                        .build());

        jobSeekerService.register(request);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(jobSeekerRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).startsWith("$2a$");
        assertThat(passwordEncoder.matches("password123", userCaptor.getValue().getPassword())).isTrue();
        verify(emailVerificationService).sendVerificationCode(savedUser);
    }
}
