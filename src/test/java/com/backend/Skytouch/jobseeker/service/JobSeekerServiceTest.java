package com.backend.Skytouch.jobseeker.service;

import com.backend.Skytouch.application.service.ApplicationService;
import com.backend.Skytouch.interview.service.InterviewService;
import com.backend.Skytouch.jobalert.service.JobAlertService;
import com.backend.Skytouch.offer.service.OfferService;
import com.backend.Skytouch.savedjob.service.SavedJobService;
import com.backend.Skytouch.common.address.AddressValidationService;
import com.backend.Skytouch.common.address.ValidatedAddress;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.mapper.JobSeekerMapper;
import com.backend.Skytouch.common.profile.JobSeekerProfileCompletenessCalculator;
import com.backend.Skytouch.common.profile.ProfileCompleteness;
import com.backend.Skytouch.common.profile.ProfileStep;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerKycRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerOnboardingRequest;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.time.LocalDate;
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

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AddressValidationService addressValidationService;

    @Mock
    private JobSeekerProfileCompletenessCalculator profileCompletenessCalculator;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private SavedJobService savedJobService;

    @Mock
    private InterviewService interviewService;

    @Mock
    private OfferService offerService;

    @Mock
    private JobAlertService jobAlertService;

    @InjectMocks
    private JobSeekerService jobSeekerService;

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
        verifyNoInteractions(addressValidationService);
    }

    @Test
    void updateKyc_validatesAddressWhenProvided() {
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

        JobSeekerKycRequest request = new JobSeekerKycRequest();
        request.setNin("12345678901");
        request.setBirthday(LocalDate.of(1990, 1, 15));
        request.setGender("Female");
        request.setAddress("12 Allen Avenue, Ikeja, Lagos");

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(addressValidationService.validate(request.getAddress()))
                .thenReturn(new ValidatedAddress("12 Allen Avenue", "Ikeja", "Lagos"));
        when(jobSeekerRepository.save(profile)).thenReturn(profile);
        when(jobSeekerMapper.toResponse(user, profile)).thenReturn(
                JobSeekerResponse.builder().email(user.getEmail()).build());

        jobSeekerService.updateKyc("seeker@example.com", request);

        verify(jobSeekerMapper).applyKyc(profile, request);
        verify(addressValidationService).validate(request.getAddress());
        verify(jobSeekerRepository).save(profile);
    }

    @Test
    void getDashboard_returnsCompletenessAndPlaceholderStats() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        JobSeeker profile = JobSeeker.builder()
                .user(user)
                .status(UserStatus.ACTIVE)
                .firstName("Ada")
                .lastName("Okafor")
                .openToWork(true)
                .build();
        ProfileCompleteness completeness = ProfileCompleteness.builder()
                .percentComplete(50)
                .steps(List.of(ProfileStep.builder()
                        .key("email_verified")
                        .label("Verify email")
                        .complete(true)
                        .build()))
                .build();

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(profileCompletenessCalculator.calculate(user, profile)).thenReturn(completeness);
        when(applicationService.countApplicationsForSeeker("seeker@example.com")).thenReturn(0L);
        when(savedJobService.countForSeeker("seeker@example.com")).thenReturn(2L);
        when(interviewService.countUpcomingForSeeker("seeker@example.com")).thenReturn(1L);
        when(offerService.countPendingForSeeker("seeker@example.com")).thenReturn(1L);
        when(jobAlertService.countActiveForSeeker("seeker@example.com")).thenReturn(2L);

        var result = jobSeekerService.getDashboard("seeker@example.com");

        assertThat(result.getDisplayName()).isEqualTo("Ada Okafor");
        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getOpenToWork()).isTrue();
        assertThat(result.getProfileCompleteness().getPercentComplete()).isEqualTo(50);
        assertThat(result.getStats().getApplicationsCount()).isZero();
        assertThat(result.getStats().getSavedJobsCount()).isEqualTo(2);
        assertThat(result.getStats().getInterviewsCount()).isEqualTo(1);
        assertThat(result.getStats().getPendingOffersCount()).isEqualTo(1);
        assertThat(result.getStats().getJobAlertsCount()).isEqualTo(2);
    }
}
