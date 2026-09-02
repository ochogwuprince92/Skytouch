package com.backend.Skytouch.analytics.service;

import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.employer.entity.Employer;
import com.backend.Skytouch.employer.repository.EmployerRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployerAnalyticsServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private EmployerAnalyticsService employerAnalyticsService;

    @Test
    void getFunnelAnalytics_returnsEmptyWhenNoCompany() {
        UUID userId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("employer@example.com")
                .role(UserRole.EMPLOYER)
                .status(UserStatus.ACTIVE)
                .build();
        Employer employer = Employer.builder().user(user).company(null).build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(employer));

        var result = employerAnalyticsService.getFunnelAnalytics("employer@example.com");

        assertThat(result.isCompanyLinked()).isFalse();
        assertThat(result.getFunnel().getTotal()).isZero();
    }

    @Test
    void getFunnelAnalytics_returnsFunnelForLinkedCompany() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Users user = Users.builder()
                .id(userId)
                .email("employer@example.com")
                .role(UserRole.EMPLOYER)
                .status(UserStatus.ACTIVE)
                .build();
        Company company = Company.builder().id(companyId).name("Acme").status(CompanyStatus.ACTIVE).build();
        Employer employer = Employer.builder().user(user).company(company).build();

        when(userRepository.findByEmailAndRole("employer@example.com", UserRole.EMPLOYER))
                .thenReturn(Optional.of(user));
        when(employerRepository.findByUser_Id(userId)).thenReturn(Optional.of(employer));
        when(applicationRepository.countGroupedByStatusForCompany(companyId)).thenReturn(List.of(
                new Object[]{com.backend.Skytouch.common.enums.ApplicationStatus.SUBMITTED, 3L},
                new Object[]{com.backend.Skytouch.common.enums.ApplicationStatus.HIRED, 1L}));
        when(applicationRepository.findJobApplicantSummariesForCompany(
                eq(companyId), any(), any())).thenReturn(List.<Object[]>of(
                new Object[]{jobId, "Backend Engineer", 3L, 1L}));

        var result = employerAnalyticsService.getFunnelAnalytics("employer@example.com");

        assertThat(result.isCompanyLinked()).isTrue();
        assertThat(result.getFunnel().getSubmitted()).isEqualTo(3);
        assertThat(result.getFunnel().getHired()).isEqualTo(1);
        assertThat(result.getTopJobsByApplicants()).hasSize(1);
        assertThat(result.getTopJobsByApplicants().get(0).getJobTitle()).isEqualTo("Backend Engineer");
    }

    @Test
    void getJobFunnelAnalytics_returnsFunnelForOwnedJob() {
        UUID jobId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme").status(CompanyStatus.ACTIVE).build();
        Job job = Job.builder()
                .id(jobId)
                .company(company)
                .title("Backend Engineer")
                .status(JobStatus.ACTIVE)
                .build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.countGroupedByStatusForJob(jobId)).thenReturn(List.of(
                new Object[]{ApplicationStatus.SUBMITTED, 4L},
                new Object[]{ApplicationStatus.HIRED, 1L}));

        var result = employerAnalyticsService.getJobFunnelAnalytics("employer@example.com", jobId);

        assertThat(result.getJobId()).isEqualTo(jobId);
        assertThat(result.getJobTitle()).isEqualTo("Backend Engineer");
        assertThat(result.getJobStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(result.getFunnel().getSubmitted()).isEqualTo(4);
        assertThat(result.getFunnel().getHired()).isEqualTo(1);
        assertThat(result.getHireRatePercent()).isEqualTo(20.0);
    }

    @Test
    void getJobFunnelAnalytics_throwsWhenJobBelongsToAnotherCompany() {
        UUID jobId = UUID.randomUUID();
        UUID employerCompanyId = UUID.randomUUID();
        UUID otherCompanyId = UUID.randomUUID();
        Company employerCompany = Company.builder().id(employerCompanyId).name("Acme").build();
        Company otherCompany = Company.builder().id(otherCompanyId).name("Other").build();
        Job job = Job.builder().id(jobId).company(otherCompany).title("Other Job").build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(employerCompany);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> employerAnalyticsService.getJobFunnelAnalytics("employer@example.com", jobId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Job not found");
    }
}
