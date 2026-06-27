package com.backend.Skytouch.jobseeker.service;

import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.authentication.apimodel.RegisterJobSeekerResponse;
import com.backend.Skytouch.authentication.service.EmailVerificationService;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerOnboardingRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.authentication.apimodel.RegisterJobSeekerRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSeekerServiceTest {

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobSeekerMapper jobSeekerMapper;

    @InjectMocks
    private JobSeekerService jobSeekerService;

    @Mock
    FileStorageService fileStorageService;

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

        MultipartFile mockCv = mock(MultipartFile.class);
        when(mockCv.isEmpty()).thenReturn(false);
        when(mockCv.getContentType()).thenReturn("application/pdf");

        JobSeekerOnboardingRequest request = new JobSeekerOnboardingRequest();
        request.setJob("Software Engineer");
        request.setOpenToWork(true);
        request.setCv(mockCv);

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(fileStorageService.uploadPdf(mockCv))
                .thenReturn("expected-cv-url");
        when(jobSeekerRepository.save(profile)).thenReturn(profile);
        when(jobSeekerMapper.toResponse(user, profile)).thenReturn(
                JobSeekerResponse.builder()
                        .email(user.getEmail())
                        .job("Software Engineer")
                        .openToWork(true)
                        .build());

        jobSeekerService.updateOnboarding("seeker@example.com", request);

        verify(jobSeekerMapper).applyOnboarding(eq(profile), eq(request), eq("expected-cv-url"));
        verify(jobSeekerRepository).save(profile);
    }
}
