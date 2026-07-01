package com.backend.Skytouch.analytics.service;

import com.backend.Skytouch.admin.service.AdminModerationService;
import com.backend.Skytouch.analytics.apimodel.AdminPlatformAnalyticsResponse;
import com.backend.Skytouch.analytics.apimodel.ApplicationFunnelCounts;
import com.backend.Skytouch.analytics.util.FunnelCountBuilder;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final AdminModerationService adminModerationService;

    @Transactional(readOnly = true)
    public AdminPlatformAnalyticsResponse getPlatformAnalytics() {
        ApplicationFunnelCounts funnel = FunnelCountBuilder.fromGroupedResults(
                applicationRepository.countGroupedByStatusPlatform());

        return AdminPlatformAnalyticsResponse.builder()
                .totalUsers(userRepository.count())
                .activeJobs(jobRepository.countByStatus(JobStatus.ACTIVE))
                .totalApplications(applicationRepository.count())
                .totalHires(applicationRepository.countByStatus(ApplicationStatus.HIRED))
                .pendingCompanies(adminModerationService.countPendingCompanies())
                .applicationFunnel(funnel)
                .platformHireRatePercent(FunnelCountBuilder.hireRatePercent(funnel))
                .build();
    }
}
