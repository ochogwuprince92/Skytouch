package com.backend.Skytouch.admin.service;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.util.CsvUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.repository.CompanyRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminExportService {

    private static final int MAX_EXPORT_ROWS = 10_000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String APPLICATIONS_HEADER =
            "application_id,seeker_name,seeker_email,job_title,company_name,status,cv_url,applied_at,updated_at\n";
    private static final String USERS_HEADER =
            "user_id,email,role,status,email_verified,created_at,updated_at\n";
    private static final String COMPANIES_HEADER =
            "company_id,name,industry,website,address_state,address_lga,status,created_at,updated_at\n";
    private static final String JOBS_HEADER =
            "job_id,title,company_name,employment_type,work_mode,salary_min,salary_max,location_state,status,created_at,updated_at\n";

    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public byte[] exportApplications(String emailFilter) {
        List<JobApplication> applications;
        if (emailFilter != null && !emailFilter.isBlank()) {
            applications = applicationRepository.findAllBySeekerEmailContainingForExport(
                    emailFilter, PageRequest.of(0, MAX_EXPORT_ROWS));
        } else {
            applications = applicationRepository.findAllForExport(PageRequest.of(0, MAX_EXPORT_ROWS));
        }
        return toApplicationsCsv(applications);
    }

    @Transactional(readOnly = true)
    public byte[] exportUsers(String emailFilter) {
        List<Users> users;
        if (emailFilter != null && !emailFilter.isBlank()) {
            users = userRepository.findByEmailContaining(emailFilter, PageRequest.of(0, MAX_EXPORT_ROWS)).getContent();
        } else {
            users = userRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).getContent();
        }
        return toUsersCsv(users);
    }

    @Transactional(readOnly = true)
    public byte[] exportCompanies() {
        List<Company> companies = companyRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).getContent();
        return toCompaniesCsv(companies);
    }

    @Transactional(readOnly = true)
    public byte[] exportJobs() {
        List<Job> jobs = jobRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).getContent();
        return toJobsCsv(jobs);
    }

    private byte[] toApplicationsCsv(List<JobApplication> applications) {
        StringBuilder csv = new StringBuilder(APPLICATIONS_HEADER);
        for (JobApplication application : applications) {
            csv.append(toApplicationRow(application));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String toApplicationRow(JobApplication application) {
        JobSeeker seeker = application.getJobSeeker();
        String seekerEmail = seeker.getUser().getEmail();
        String appliedAt = application.getAppliedAt() != null
                ? application.getAppliedAt().format(DATE_FORMAT) : "";
        String updatedAt = application.getUpdatedAt() != null
                ? application.getUpdatedAt().format(DATE_FORMAT) : "";
        String jobTitle = application.getJob() != null ? application.getJob().getTitle() : "";
        String companyName = application.getJob() != null && application.getJob().getCompany() != null
                ? application.getJob().getCompany().getName() : "";

        return CsvUtils.row(
                application.getId().toString(),
                application.getSeekerName(),
                seekerEmail,
                jobTitle,
                companyName,
                application.getStatus().name(),
                application.getCvUrl(),
                appliedAt,
                updatedAt);
    }

    private byte[] toUsersCsv(List<Users> users) {
        StringBuilder csv = new StringBuilder(USERS_HEADER);
        for (Users user : users) {
            csv.append(toUserRow(user));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String toUserRow(Users user) {
        String createdAt = user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMAT) : "";
        String updatedAt = user.getUpdatedAt() != null ? user.getUpdatedAt().format(DATE_FORMAT) : "";

        return CsvUtils.row(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getEmailVerified().toString(),
                createdAt,
                updatedAt);
    }

    private byte[] toCompaniesCsv(List<Company> companies) {
        StringBuilder csv = new StringBuilder(COMPANIES_HEADER);
        for (Company company : companies) {
            csv.append(toCompanyRow(company));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String toCompanyRow(Company company) {
        String createdAt = company.getCreatedAt() != null ? company.getCreatedAt().format(DATE_FORMAT) : "";
        String updatedAt = company.getUpdatedAt() != null ? company.getUpdatedAt().format(DATE_FORMAT) : "";

        return CsvUtils.row(
                company.getId().toString(),
                company.getName(),
                company.getIndustry() != null ? company.getIndustry().name() : "",
                company.getWebsite(),
                company.getAddressState(),
                company.getAddressLga(),
                company.getStatus().name(),
                createdAt,
                updatedAt);
    }

    private byte[] toJobsCsv(List<Job> jobs) {
        StringBuilder csv = new StringBuilder(JOBS_HEADER);
        for (Job job : jobs) {
            csv.append(toJobRow(job));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String toJobRow(Job job) {
        String createdAt = job.getCreatedAt() != null ? job.getCreatedAt().format(DATE_FORMAT) : "";
        String updatedAt = job.getUpdatedAt() != null ? job.getUpdatedAt().format(DATE_FORMAT) : "";
        String companyName = job.getCompany() != null ? job.getCompany().getName() : "";
        String salaryMin = job.getSalaryMin() != null ? job.getSalaryMin().toString() : "";
        String salaryMax = job.getSalaryMax() != null ? job.getSalaryMax().toString() : "";

        return CsvUtils.row(
                job.getId().toString(),
                job.getTitle(),
                companyName,
                job.getEmploymentType().name(),
                job.getWorkMode().name(),
                salaryMin,
                salaryMax,
                job.getLocationState(),
                job.getStatus().name(),
                createdAt,
                updatedAt);
    }
}
