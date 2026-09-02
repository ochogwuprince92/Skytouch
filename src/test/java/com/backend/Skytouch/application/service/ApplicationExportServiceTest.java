package com.backend.Skytouch.application.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.user.entity.Users;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationExportServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private ApplicationExportService applicationExportService;

    @Test
    void exportJobApplications_returnsCsvWithHeaderAndRow() {
        UUID jobId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme").status(CompanyStatus.ACTIVE).build();
        Job job = Job.builder().id(jobId).company(company).title("Backend Engineer").status(JobStatus.ACTIVE).build();
        Users user = Users.builder()
                .id(UUID.randomUUID())
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .build();
        JobSeeker seeker = JobSeeker.builder().id(UUID.randomUUID()).user(user).build();
        JobApplication application = JobApplication.builder()
                .id(applicationId)
                .job(job)
                .jobSeeker(seeker)
                .status(ApplicationStatus.SUBMITTED)
                .seekerName("Ada Okafor")
                .cvUrl("https://example.com/cv.pdf")
                .appliedAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.findAllByJobIdForExport(eq(jobId), any(Pageable.class)))
                .thenReturn(List.of(application));

        String csv = new String(
                applicationExportService.exportJobApplications("employer@example.com", jobId),
                StandardCharsets.UTF_8);

        assertThat(csv).startsWith("application_id,seeker_name,seeker_email,status,cv_url,applied_at,updated_at");
        assertThat(csv).contains(applicationId.toString());
        assertThat(csv).contains("Ada Okafor");
        assertThat(csv).contains("seeker@example.com");
        assertThat(csv).contains("SUBMITTED");
    }

    @Test
    void exportJobApplications_throwsWhenJobNotOwned() {
        UUID jobId = UUID.randomUUID();
        UUID employerCompanyId = UUID.randomUUID();
        UUID otherCompanyId = UUID.randomUUID();
        Company employerCompany = Company.builder().id(employerCompanyId).name("Acme").build();
        Company otherCompany = Company.builder().id(otherCompanyId).name("Other").build();
        Job job = Job.builder().id(jobId).company(otherCompany).title("Other Job").build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(employerCompany);
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> applicationExportService.exportJobApplications("employer@example.com", jobId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void exportCompanyApplications_includesJobTitleColumn() {
        UUID companyId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Company company = Company.builder().id(companyId).name("Acme").status(CompanyStatus.ACTIVE).build();
        Job job = Job.builder().id(jobId).company(company).title("Data Analyst").status(JobStatus.ACTIVE).build();
        Users user = Users.builder()
                .id(UUID.randomUUID())
                .email("seeker@example.com")
                .role(UserRole.JOB_SEEKER)
                .build();
        JobSeeker seeker = JobSeeker.builder().id(UUID.randomUUID()).user(user).build();
        JobApplication application = JobApplication.builder()
                .id(UUID.randomUUID())
                .job(job)
                .jobSeeker(seeker)
                .status(ApplicationStatus.REVIEWING)
                .seekerName("John Doe")
                .appliedAt(LocalDateTime.of(2026, 6, 2, 9, 30))
                .build();

        when(companyService.getLinkedCompany("employer@example.com")).thenReturn(company);
        when(applicationRepository.findAllByCompanyIdForExport(eq(companyId), any(Pageable.class)))
                .thenReturn(List.of(application));

        String csv = new String(
                applicationExportService.exportCompanyApplications("employer@example.com"),
                StandardCharsets.UTF_8);

        assertThat(csv).startsWith("application_id,job_title,seeker_name,seeker_email,status,cv_url,applied_at,updated_at");
        assertThat(csv).contains("Data Analyst");
        assertThat(csv).contains("John Doe");
        assertThat(csv).contains("REVIEWING");
    }
}
