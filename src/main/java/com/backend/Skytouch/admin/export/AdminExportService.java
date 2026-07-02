package com.backend.Skytouch.admin.export;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.util.CsvUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.repository.CompanyRepository;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
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

    static final int MAX_EXPORT_ROWS = 5_000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public byte[] export(AdminExportType type) {
        return switch (type) {
            case USERS -> exportUsers();
            case JOBS -> exportJobs();
            case APPLICATIONS -> exportApplications();
            case COMPANIES -> exportCompanies();
        };
    }

    private byte[] exportUsers() {
        StringBuilder csv = new StringBuilder(
                "user_id,email,role,status,email_verified,active,created_at\n");
        List<Users> users = userRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).getContent();
        for (Users user : users) {
            csv.append(CsvUtils.row(
                    user.getId().toString(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getStatus().name(),
                    String.valueOf(user.getEmailVerified()),
                    String.valueOf(user.getActive()),
                    formatDate(user.getCreatedAt())));
        }
        return toBytes(csv);
    }

    private byte[] exportJobs() {
        StringBuilder csv = new StringBuilder(
                "job_id,title,company_name,status,employment_type,work_mode,location_state,published_at,created_at\n");
        List<Job> jobs = jobRepository.findAllForExport(PageRequest.of(0, MAX_EXPORT_ROWS));
        for (Job job : jobs) {
            csv.append(CsvUtils.row(
                    job.getId().toString(),
                    job.getTitle(),
                    job.getCompany().getName(),
                    job.getStatus().name(),
                    job.getEmploymentType() != null ? job.getEmploymentType().name() : "",
                    job.getWorkMode() != null ? job.getWorkMode().name() : "",
                    job.getLocationState(),
                    formatDate(job.getPublishedAt()),
                    formatDate(job.getCreatedAt())));
        }
        return toBytes(csv);
    }

    private byte[] exportApplications() {
        StringBuilder csv = new StringBuilder(
                "application_id,job_title,company_name,seeker_name,seeker_email,status,applied_at\n");
        List<JobApplication> applications = applicationRepository.findAllForPlatformExport(
                PageRequest.of(0, MAX_EXPORT_ROWS));
        for (JobApplication application : applications) {
            csv.append(CsvUtils.row(
                    application.getId().toString(),
                    application.getJob().getTitle(),
                    application.getJob().getCompany().getName(),
                    application.getSeekerName(),
                    application.getJobSeeker().getUser().getEmail(),
                    application.getStatus().name(),
                    formatDate(application.getAppliedAt())));
        }
        return toBytes(csv);
    }

    private byte[] exportCompanies() {
        StringBuilder csv = new StringBuilder(
                "company_id,name,industry,status,website,created_at\n");
        List<Company> companies = companyRepository.findAll(PageRequest.of(0, MAX_EXPORT_ROWS)).getContent();
        for (Company company : companies) {
            csv.append(CsvUtils.row(
                    company.getId().toString(),
                    company.getName(),
                    company.getIndustry(),
                    company.getStatus().name(),
                    company.getWebsite(),
                    formatDate(company.getCreatedAt())));
        }
        return toBytes(csv);
    }

    public static AdminExportType parseType(String type) {
        try {
            return AdminExportType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid export type: " + type
                    + ". Valid values: users, jobs, applications, companies");
        }
    }

    private String formatDate(java.time.LocalDateTime value) {
        return value != null ? value.format(DATE_FORMAT) : "";
    }

    private byte[] toBytes(StringBuilder csv) {
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
