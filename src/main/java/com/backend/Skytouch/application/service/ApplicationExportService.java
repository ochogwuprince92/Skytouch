package com.backend.Skytouch.application.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.util.CsvUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationExportService {

    static final int MAX_EXPORT_ROWS = 5_000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String JOB_HEADER =
            "application_id,seeker_name,seeker_email,status,cv_url,applied_at,updated_at\n";
    private static final String COMPANY_HEADER =
            "application_id,job_title,seeker_name,seeker_email,status,cv_url,applied_at,updated_at\n";
    private static final String SEEKER_HEADER =
            "application_id,job_title,company_name,status,applied_at,updated_at\n";

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CompanyService companyService;

    @Transactional(readOnly = true)
    public byte[] exportJobApplications(String employerEmail, UUID jobId) {
        Job job = getOwnedJob(employerEmail, jobId);
        List<JobApplication> applications = applicationRepository.findAllByJobIdForExport(
                job.getId(), PageRequest.of(0, MAX_EXPORT_ROWS));
        return toCsv(JOB_HEADER, applications, false);
    }

    @Transactional(readOnly = true)
    public byte[] exportCompanyApplications(String employerEmail) {
        Company company = companyService.getLinkedCompany(employerEmail);
        List<JobApplication> applications = applicationRepository.findAllByCompanyIdForExport(
                company.getId(), PageRequest.of(0, MAX_EXPORT_ROWS));
        return toCsv(COMPANY_HEADER, applications, true);
    }

    @Transactional(readOnly = true)
    public byte[] exportMyApplications(String seekerEmail) {
        List<JobApplication> applications = applicationRepository.findAllBySeekerEmailForExport(
                seekerEmail, PageRequest.of(0, MAX_EXPORT_ROWS));
        StringBuilder csv = new StringBuilder(SEEKER_HEADER);
        for (JobApplication application : applications) {
            csv.append(toSeekerRow(application));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String toSeekerRow(JobApplication application) {
        String appliedAt = application.getAppliedAt() != null
                ? application.getAppliedAt().format(DATE_FORMAT) : "";
        String updatedAt = application.getUpdatedAt() != null
                ? application.getUpdatedAt().format(DATE_FORMAT) : "";
        return CsvUtils.row(
                application.getId().toString(),
                application.getJob().getTitle(),
                application.getJob().getCompany().getName(),
                application.getStatus().name(),
                appliedAt,
                updatedAt);
    }

    private byte[] toCsv(String header, List<JobApplication> applications, boolean includeJobTitle) {
        StringBuilder csv = new StringBuilder(header);
        for (JobApplication application : applications) {
            csv.append(toRow(application, includeJobTitle));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String toRow(JobApplication application, boolean includeJobTitle) {
        JobSeeker seeker = application.getJobSeeker();
        String seekerEmail = seeker.getUser().getEmail();
        String appliedAt = application.getAppliedAt() != null
                ? application.getAppliedAt().format(DATE_FORMAT) : "";
        String updatedAt = application.getUpdatedAt() != null
                ? application.getUpdatedAt().format(DATE_FORMAT) : "";

        if (includeJobTitle) {
            return CsvUtils.row(
                    application.getId().toString(),
                    application.getJob().getTitle(),
                    application.getSeekerName(),
                    seekerEmail,
                    application.getStatus().name(),
                    application.getCvUrl(),
                    appliedAt,
                    updatedAt);
        }

        return CsvUtils.row(
                application.getId().toString(),
                application.getSeekerName(),
                seekerEmail,
                application.getStatus().name(),
                application.getCvUrl(),
                appliedAt,
                updatedAt);
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
}
