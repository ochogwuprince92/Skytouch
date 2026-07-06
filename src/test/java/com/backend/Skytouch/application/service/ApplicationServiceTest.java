package com.backend.Skytouch.application.service;

import com.backend.Skytouch.application.apimodel.ApplicationCreateRequest;
import com.backend.Skytouch.application.apimodel.ApplicationStatusUpdateRequest;
import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.mapper.ApplicationMapper;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobSeekerRepository jobSeekerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void apply_createsSubmittedApplicationForActiveJob() {
        UUID jobId = UUID.randomUUID();
        UUID seekerId = UUID.randomUUID();
        Users user = activeJobSeekerUser(seekerId);
        JobSeeker seeker = jobSeekerProfile(user, "https://cdn.example.com/cv.pdf");
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme Ltd").build();
        Job job = Job.builder().id(jobId).company(company).title("Backend Engineer").status(JobStatus.ACTIVE).build();
        ApplicationCreateRequest request = new ApplicationCreateRequest();
        request.setCoverLetter("I am interested");
        JobApplication entity = JobApplication.builder().id(UUID.randomUUID()).job(job).jobSeeker(seeker)
                .status(ApplicationStatus.SUBMITTED).build();

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(seekerId)).thenReturn(Optional.of(seeker));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJob_IdAndJobSeeker_Id(jobId, seekerId)).thenReturn(false);
        when(applicationMapper.toEntity(job, seeker, "I am interested")).thenReturn(entity);
        when(applicationRepository.save(entity)).thenReturn(entity);
        when(applicationMapper.toResponse(entity)).thenReturn(
                com.backend.Skytouch.application.apimodel.ApplicationResponse.builder()
                        .id(entity.getId())
                        .status(ApplicationStatus.SUBMITTED)
                        .jobTitle("Backend Engineer")
                        .build());

        var response = applicationService.apply("seeker@example.com", jobId, request);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        verify(applicationRepository).save(entity);
        verify(notificationService).notifyOnApplicationSubmitted(entity);
    }

    @Test
    void apply_rejectsWhenCvMissing() {
        UUID seekerId = UUID.randomUUID();
        Users user = activeJobSeekerUser(seekerId);
        JobSeeker seeker = jobSeekerProfile(user, null);

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(seekerId)).thenReturn(Optional.of(seeker));

        assertThatThrownBy(() -> applicationService.apply("seeker@example.com", UUID.randomUUID(), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("CV");
    }

    @Test
    void apply_rejectsNonActiveJob() {
        UUID jobId = UUID.randomUUID();
        UUID seekerId = UUID.randomUUID();
        Users user = activeJobSeekerUser(seekerId);
        JobSeeker seeker = jobSeekerProfile(user, "https://cdn.example.com/cv.pdf");
        Job job = Job.builder().id(jobId).status(JobStatus.DRAFT).build();

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(seekerId)).thenReturn(Optional.of(seeker));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> applicationService.apply("seeker@example.com", jobId, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("active jobs");
    }

    @Test
    void apply_rejectsDuplicateApplication() {
        UUID jobId = UUID.randomUUID();
        UUID seekerId = UUID.randomUUID();
        Users user = activeJobSeekerUser(seekerId);
        JobSeeker seeker = jobSeekerProfile(user, "https://cdn.example.com/cv.pdf");
        Job job = Job.builder().id(jobId).status(JobStatus.ACTIVE).build();

        when(userRepository.findByEmailAndRole("seeker@example.com", UserRole.JOB_SEEKER))
                .thenReturn(Optional.of(user));
        when(jobSeekerRepository.findByUser_Id(seekerId)).thenReturn(Optional.of(seeker));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJob_IdAndJobSeeker_Id(jobId, seekerId)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply("seeker@example.com", jobId, null))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already applied");
    }

    @Test
    void withdraw_marksApplicationWithdrawn() {
        UUID applicationId = UUID.randomUUID();
        JobApplication application = JobApplication.builder()
                .id(applicationId)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        when(applicationRepository.findByIdAndJobSeeker_User_Email(applicationId, "seeker@example.com"))
                .thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);
        when(applicationMapper.toResponse(application)).thenReturn(
                com.backend.Skytouch.application.apimodel.ApplicationResponse.builder()
                        .id(applicationId)
                        .status(ApplicationStatus.WITHDRAWN)
                        .build());

        var response = applicationService.withdraw("seeker@example.com", applicationId);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    void updateStatus_allowsEmployerReview() {
        UUID companyId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme Ltd").build();
        Job job = Job.builder().id(jobId).company(company).status(JobStatus.ACTIVE).build();
        JobApplication application = JobApplication.builder()
                .id(applicationId)
                .job(job)
                .status(ApplicationStatus.SUBMITTED)
                .build();
        ApplicationStatusUpdateRequest request = new ApplicationStatusUpdateRequest();
        request.setStatus(ApplicationStatus.REVIEWING);

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.findByIdAndJob_Id(applicationId, jobId)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);
        when(applicationMapper.toResponse(application)).thenReturn(
                com.backend.Skytouch.application.apimodel.ApplicationResponse.builder()
                        .id(applicationId)
                        .status(ApplicationStatus.REVIEWING)
                        .build());

        var response = applicationService.updateStatus("employer@example.com", jobId, applicationId, request);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.REVIEWING);
        verify(notificationService).notifyOnStatusUpdated(application);
    }

    @Test
    void findMyApplications_returnsPaginatedResults() {
        JobApplication application = JobApplication.builder().id(UUID.randomUUID()).build();
        Page<JobApplication> page = new PageImpl<>(List.of(application));

        when(applicationRepository.findByJobSeeker_User_EmailOrderByAppliedAtDesc(
                eq("seeker@example.com"), any(Pageable.class))).thenReturn(page);
        when(applicationMapper.toResponse(application)).thenReturn(
                com.backend.Skytouch.application.apimodel.ApplicationResponse.builder()
                        .id(application.getId())
                        .status(ApplicationStatus.SUBMITTED)
                        .build());

        var response = applicationService.findMyApplications("seeker@example.com", 0, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    private Users activeJobSeekerUser(UUID id) {
        return Users.builder()
                .id(id)
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }

    private JobSeeker jobSeekerProfile(Users user, String cvUrl) {
        return JobSeeker.builder()
                .id(user.getId())
                .user(user)
                .firstName("Ada")
                .lastName("Okafor")
                .phone("+2348012345678")
                .cvUrl(cvUrl)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
