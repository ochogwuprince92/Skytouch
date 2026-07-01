package com.backend.Skytouch.analytics.service;

import com.backend.Skytouch.analytics.apimodel.ApplicationFunnelCounts;
import com.backend.Skytouch.analytics.apimodel.EmployerFunnelAnalyticsResponse;
import com.backend.Skytouch.analytics.apimodel.JobApplicantSummary;
import com.backend.Skytouch.analytics.apimodel.JobFunnelAnalyticsResponse;
import com.backend.Skytouch.analytics.util.FunnelCountBuilder;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployerAnalyticsService {

    private static final UserRole EMPLOYER_ROLE = UserRole.EMPLOYER;
    private static final int TOP_JOBS_LIMIT = 5;

    private final JobApplicationRepository applicationRepository;
    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final CompanyService companyService;

    @Transactional(readOnly = true)
    public EmployerFunnelAnalyticsResponse getFunnelAnalytics(String employerEmail) {
        Employer employer = getEmployerProfile(employerEmail);
        if (employer.getCompany() == null) {
            return EmployerFunnelAnalyticsResponse.builder()
                    .companyLinked(false)
                    .funnel(FunnelCountBuilder.empty())
                    .hireRatePercent(0.0)
                    .shortlistToHireRatePercent(0.0)
                    .topJobsByApplicants(List.of())
                    .build();
        }

        UUID companyId = employer.getCompany().getId();
        ApplicationFunnelCounts funnel = FunnelCountBuilder.fromGroupedResults(
                applicationRepository.countGroupedByStatusForCompany(companyId));

        List<JobApplicantSummary> topJobs = applicationRepository
                .findJobApplicantSummariesForCompany(
                        companyId,
                        ApplicationStatus.HIRED,
                        PageRequest.of(0, TOP_JOBS_LIMIT))
                .stream()
                .map(this::toJobSummary)
                .toList();

        return EmployerFunnelAnalyticsResponse.builder()
                .companyLinked(true)
                .funnel(funnel)
                .hireRatePercent(FunnelCountBuilder.hireRatePercent(funnel))
                .shortlistToHireRatePercent(FunnelCountBuilder.shortlistToHireRatePercent(funnel))
                .topJobsByApplicants(topJobs)
                .build();
    }

    @Transactional(readOnly = true)
    public JobFunnelAnalyticsResponse getJobFunnelAnalytics(String employerEmail, UUID jobId) {
        Job job = getOwnedJob(employerEmail, jobId);
        ApplicationFunnelCounts funnel = FunnelCountBuilder.fromGroupedResults(
                applicationRepository.countGroupedByStatusForJob(jobId));

        return JobFunnelAnalyticsResponse.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .jobStatus(job.getStatus())
                .funnel(funnel)
                .hireRatePercent(FunnelCountBuilder.hireRatePercent(funnel))
                .shortlistToHireRatePercent(FunnelCountBuilder.shortlistToHireRatePercent(funnel))
                .build();
    }

    private Job getOwnedJob(String employerEmail, UUID jobId) {
        Company company = companyService.getLinkedCompany(employerEmail);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        if (!job.getCompany().getId().equals(company.getId())) {
            throw new ResourceNotFoundException("Job not found: " + jobId);
        }
        return job;
    }

    private JobApplicantSummary toJobSummary(Object[] row) {
        return JobApplicantSummary.builder()
                .jobId((UUID) row[0])
                .jobTitle((String) row[1])
                .applicantCount((Long) row[2])
                .hiredCount(row[3] != null ? ((Number) row[3]).longValue() : 0L)
                .build();
    }

    private Employer getEmployerProfile(String email) {
        Users user = userRepository.findByEmailAndRole(email, EMPLOYER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found: " + email));
        return employerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employer profile not found: " + email));
    }
}
