package com.backend.Skytouch.jobalert.service;

import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.Industry;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.WorkMode;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobalert.config.JobAlertDigestProperties;
import com.backend.Skytouch.jobalert.repository.JobAlertRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobAlertDigestServiceTest {

    @Mock private JobRepository jobRepository;
    @Mock private JobAlertRepository jobAlertRepository;
    @Mock private JobSeekerRepository jobSeekerRepository;
    @Mock private UserRepository userRepository;
    @Mock private JobAlertDeliveryService deliveryService;
    @Mock private NotificationService notificationService;
    @Mock private JobAlertDigestProperties properties;

    @InjectMocks
    private JobAlertDigestService digestService;

    private UUID userId;
    private UUID seekerId;
    private UUID jobId;
    private Job job;
    private Users user;
    private JobSeeker seeker;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        seekerId = UUID.randomUUID();
        jobId = UUID.randomUUID();
        Company company = Company.builder().id(UUID.randomUUID()).name("Acme").industry(Industry.TECHNOLOGY).build();
        job = Job.builder()
                .id(jobId)
                .company(company)
                .title("Backend Engineer")
                .description("Java role")
                .employmentType(EmploymentType.FULL_TIME)
                .workMode(WorkMode.REMOTE)
                .locationState("Lagos")
                .status(JobStatus.ACTIVE)
                .publishedAt(LocalDateTime.now().minusHours(2))
                .build();
        user = Users.builder().id(userId).email("seeker@example.com").role(UserRole.JOB_SEEKER).build();
        seeker = JobSeeker.builder().id(seekerId).user(user).build();
    }

    @Test
    void runDigest_notifiesSeekersWithUndeliveredMatches() {
        when(properties.getLookbackHours()).thenReturn(24);
        when(jobRepository.findRecentlyPublished(
                eq(JobStatus.ACTIVE), eq(CompanyStatus.ACTIVE), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(job));
        when(jobAlertRepository.findMatchingSeekerUserIds(
                eq(job.getTitle()), eq(job.getDescription()), eq(job.getEmploymentType()),
                eq(job.getWorkMode()), eq(job.getLocationState()), eq("TECHNOLOGY")))
                .thenReturn(List.of(userId));
        when(jobSeekerRepository.findByUser_Id(userId)).thenReturn(Optional.of(seeker));
        when(deliveryService.wasDelivered(seekerId, jobId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = digestService.runDigest();

        assertThat(result.getSeekersNotified()).isEqualTo(1);
        assertThat(result.getJobsIncluded()).isEqualTo(1);
        verify(notificationService).notifyOnJobAlertDigest(user, List.of(job));
        verify(deliveryService).recordDelivery(userId, job);
    }

    @Test
    void runDigest_skipsAlreadyDeliveredJobs() {
        when(properties.getLookbackHours()).thenReturn(24);
        when(jobRepository.findRecentlyPublished(
                eq(JobStatus.ACTIVE), eq(CompanyStatus.ACTIVE), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(job));
        when(jobAlertRepository.findMatchingSeekerUserIds(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(userId));
        when(jobSeekerRepository.findByUser_Id(userId)).thenReturn(Optional.of(seeker));
        when(deliveryService.wasDelivered(seekerId, jobId)).thenReturn(true);

        var result = digestService.runDigest();

        assertThat(result.getSeekersNotified()).isZero();
        assertThat(result.getJobsIncluded()).isZero();
        verifyNoInteractions(notificationService);
    }
}
