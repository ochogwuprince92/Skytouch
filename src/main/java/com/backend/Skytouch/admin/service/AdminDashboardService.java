package com.backend.Skytouch.admin.service;

import com.backend.Skytouch.admin.apimodel.AdminDashboardResponse;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.audit.repository.AuditEventRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final AdminModerationService adminModerationService;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final AuditEventRepository auditEventRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .jobSeekers(userRepository.countByRole(UserRole.JOB_SEEKER))
                .employers(userRepository.countByRole(UserRole.EMPLOYER))
                .admins(userRepository.countByRole(UserRole.ADMIN))
                .pendingEmailVerifications(userRepository.countByEmailVerifiedFalse())
                .pendingAccounts(userRepository.countByStatus(UserStatus.PENDING))
                .pendingCompanies(adminModerationService.countPendingCompanies())
                .activeJobs(jobRepository.countByStatus(JobStatus.ACTIVE))
                .totalApplications(applicationRepository.count())
                .totalHires(applicationRepository.countByStatus(ApplicationStatus.HIRED))
                .totalAuditEvents(auditEventRepository.count())
                .build();
    }
}
