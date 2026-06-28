package com.backend.Skytouch.application.service;

import com.backend.Skytouch.application.apimodel.ApplicationCreateRequest;
import com.backend.Skytouch.application.apimodel.ApplicationResponse;
import com.backend.Skytouch.application.apimodel.ApplicationStatusUpdateRequest;
import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.application.repository.JobApplicationRepository;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.ApplicationMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final UserRole JOB_SEEKER_ROLE = UserRole.JOB_SEEKER;
    private static final Set<ApplicationStatus> EMPLOYER_STATUSES = EnumSet.of(
            ApplicationStatus.REVIEWING,
            ApplicationStatus.SHORTLISTED,
            ApplicationStatus.REJECTED
    );
    private static final Set<ApplicationStatus> WITHDRAWABLE_STATUSES = EnumSet.of(
            ApplicationStatus.SUBMITTED,
            ApplicationStatus.REVIEWING,
            ApplicationStatus.SHORTLISTED
    );

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public ApplicationResponse apply(String seekerEmail, UUID jobId, ApplicationCreateRequest request) {
        JobSeeker jobSeeker = getJobSeekerProfile(seekerEmail);
        validateSeekerCanApply(jobSeeker);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new BadRequestException("Applications are only accepted for active jobs");
        }

        if (applicationRepository.existsByJob_IdAndJobSeeker_Id(jobId, jobSeeker.getId())) {
            throw new ConflictException("You have already applied to this job");
        }

        JobApplication application = applicationMapper.toEntity(
                job, jobSeeker, request != null ? request.getCoverLetter() : null);
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> findMyApplications(String seekerEmail, int page, int size) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<JobApplication> results = applicationRepository
                .findByJobSeeker_User_EmailOrderByAppliedAtDesc(seekerEmail, pageable);
        return PaginationUtils.mapPage(results, applicationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findMyApplication(String seekerEmail, UUID applicationId) {
        JobApplication application = applicationRepository.findByIdAndJobSeeker_User_Email(applicationId, seekerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        return applicationMapper.toResponse(application);
    }

    @Transactional
    public ApplicationResponse withdraw(String seekerEmail, UUID applicationId) {
        JobApplication application = applicationRepository.findByIdAndJobSeeker_User_Email(applicationId, seekerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        if (!WITHDRAWABLE_STATUSES.contains(application.getStatus())) {
            throw new BadRequestException("This application cannot be withdrawn");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> findApplicationsForJob(
            String employerEmail, UUID jobId, int page, int size) {
        Job job = getOwnedJob(employerEmail, jobId);
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<JobApplication> results = applicationRepository.findByJob_IdOrderByAppliedAtDesc(job.getId(), pageable);
        return PaginationUtils.mapPage(results, applicationMapper::toResponse);
    }

    @Transactional
    public ApplicationResponse updateStatus(
            String employerEmail,
            UUID jobId,
            UUID applicationId,
            ApplicationStatusUpdateRequest request) {
        Job job = getOwnedJob(employerEmail, jobId);
        JobApplication application = applicationRepository.findByIdAndJob_Id(applicationId, job.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("Withdrawn applications cannot be updated");
        }
        if (!EMPLOYER_STATUSES.contains(request.getStatus())) {
            throw new BadRequestException("Invalid status for employer update");
        }

        application.setStatus(request.getStatus());
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public long countApplicationsForSeeker(String seekerEmail) {
        return applicationRepository.countByJobSeeker_User_EmailAndStatusNot(
                seekerEmail, ApplicationStatus.WITHDRAWN);
    }

    @Transactional(readOnly = true)
    public long countApplicationsForCompany(UUID companyId) {
        return applicationRepository.countByJob_Company_IdAndStatusNot(
                companyId, ApplicationStatus.WITHDRAWN);
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

    private JobSeeker getJobSeekerProfile(String email) {
        Users user = userRepository.findByEmailAndRole(email, JOB_SEEKER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + email));
        return jobSeekerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile not found: " + email));
    }

    private void validateSeekerCanApply(JobSeeker jobSeeker) {
        Users user = jobSeeker.getUser();
        if (!Boolean.TRUE.equals(user.getEmailVerified()) || user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Verify your email before applying to jobs");
        }
        if (!StringUtils.hasText(jobSeeker.getCvUrl())) {
            throw new BadRequestException("Upload your CV in onboarding before applying to jobs");
        }
    }
}
