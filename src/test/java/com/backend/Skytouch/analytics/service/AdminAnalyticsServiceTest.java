package com.backend.Skytouch.analytics.service;

import com.backend.Skytouch.admin.service.AdminModerationService;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private AdminModerationService adminModerationService;

    @InjectMocks
    private AdminAnalyticsService adminAnalyticsService;

    @Test
    void getPlatformAnalytics_aggregatesCounts() {
        when(userRepository.count()).thenReturn(100L);
        when(jobRepository.countByStatus(JobStatus.ACTIVE)).thenReturn(12L);
        when(applicationRepository.count()).thenReturn(50L);
        when(applicationRepository.countByStatus(ApplicationStatus.HIRED)).thenReturn(5L);
        when(adminModerationService.countPendingCompanies()).thenReturn(3L);
        when(applicationRepository.countGroupedByStatusPlatform()).thenReturn(List.of(
                new Object[]{ApplicationStatus.SUBMITTED, 20L},
                new Object[]{ApplicationStatus.HIRED, 5L},
                new Object[]{ApplicationStatus.WITHDRAWN, 5L}));

        var result = adminAnalyticsService.getPlatformAnalytics();

        assertThat(result.getTotalUsers()).isEqualTo(100);
        assertThat(result.getActiveJobs()).isEqualTo(12);
        assertThat(result.getTotalApplications()).isEqualTo(50);
        assertThat(result.getTotalHires()).isEqualTo(5);
        assertThat(result.getPendingCompanies()).isEqualTo(3);
        assertThat(result.getApplicationFunnel().getSubmitted()).isEqualTo(20);
        assertThat(result.getPlatformHireRatePercent()).isEqualTo(20.0);
    }
}
