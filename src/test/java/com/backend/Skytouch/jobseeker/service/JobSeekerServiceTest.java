package com.backend.Skytouch.jobseeker.service;

import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.service.EmailVerificationService;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerOnboardingRequest;
import com.backend.Skytouch.jobseeker.apimodel.RegisterJobSeekerRequest;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
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

import java.util.Optional;
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
        RegisterJobSeekerRequest request = new RegisterJobSeekerRequest();
        request.setEmail("seeker@example.com");
        request.setPassword("password123");
        request.setFirstName("Ada");
        request.setLastName("Okafor");
        request.setPhone("08012345678");
        when(userRepository.existsByEmail("seeker@example.com")).thenReturn(false);

        Users savedUser = Users.builder()
                .id(UUID.randomUUID())
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.PENDING)
                .build();
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);
        when(jobSeekerMapper.toEntity(eq(request), eq(savedUser))).thenReturn(
                JobSeeker.builder()
                        .user(savedUser)
                        .status(UserStatus.PENDING)
                        .firstName("Ada")
                        .lastName("Okafor")
                        .phone("08012345678")
                        .openToWork(false)
                        .build());
        when(jobSeekerRepository.save(any(JobSeeker.class))).thenAnswer(invocation -> invocation.getArgument(0));
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
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).startsWith("$2a$");
        assertThat(passwordEncoder.matches("password123", userCaptor.getValue().getPassword())).isTrue();

        ArgumentCaptor<JobSeeker> profileCaptor = ArgumentCaptor.forClass(JobSeeker.class);
        verify(jobSeekerMapper).toEntity(request, savedUser);
        verify(jobSeekerRepository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getUser()).isEqualTo(savedUser);
        assertThat(profileCaptor.getValue().getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(profileCaptor.getValue().getFirstName()).isEqualTo("Ada");
        assertThat(profileCaptor.getValue().getLastName()).isEqualTo("Okafor");
        assertThat(profileCaptor.getValue().getOpenToWork()).isFalse();

        verify(emailVerificationService).sendVerificationCode(savedUser);
    }

    @Test
    void updateOnboarding_appliesProfileFields() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .build();
        JobSeeker profile = JobSeeker.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(UserStatus.ACTIVE)
                .build();
        JobSeekerOnboardingRequest request = new JobSeekerOnboardingRequest();
        request.setJob("Software Engineer");
        request.setOpenToWork(true);

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(jobSeekerRepository.save(profile)).thenReturn(profile);
        when(jobSeekerMapper.toResponse(user, profile)).thenReturn(
                com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse.builder()
                        .email(user.getEmail())
                        .job("Software Engineer")
                        .openToWork(true)
                        .build());

        jobSeekerService.updateOnboarding("seeker@example.com", request);

        verify(jobSeekerMapper).applyOnboarding(profile, request);
        verify(jobSeekerRepository).save(profile);
    }
}
