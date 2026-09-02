package com.backend.Skytouch.job.service;

import com.backend.Skytouch.authentication.security.SecurityUtils;
import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.WorkMode;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.JobMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.repository.CompanyRepository;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.job.apimodel.JobCreateRequest;
import com.backend.Skytouch.job.apimodel.JobResponse;
import com.backend.Skytouch.job.apimodel.JobUpdateRequest;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobalert.service.JobAlertService;
import com.backend.Skytouch.savedjob.service.SavedJobService;
import com.backend.Skytouch.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;
    private final JobMapper jobMapper;
    private final SavedJobService savedJobService;
    private final JobAlertService jobAlertService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public JobResponse create(String email, JobCreateRequest request) {
        var currentUser = SecurityUtils.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        Company company;
        if (isAdmin) {
            // Admin can either specify existing companyId or create new company inline
            if (request.getNewCompany() != null) {
                if (request.getCompanyId() != null) {
                    throw new BadRequestException("Cannot specify both companyId and newCompany");
                }
                company = companyService.createForAdmin(request.getNewCompany());
            } else if (request.getCompanyId() != null) {
                company = companyRepository.findById(request.getCompanyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + request.getCompanyId()));
                if (company.getStatus() != CompanyStatus.ACTIVE) {
                    throw new BadRequestException("Cannot post job for inactive company");
                }
            } else {
                throw new BadRequestException("Either companyId or newCompany is required for admin job posting");
            }
        } else {
            // Employer uses their linked company
            if (request.getCompanyId() != null || request.getNewCompany() != null) {
                throw new BadRequestException("Employers cannot specify companyId or newCompany");
            }
            company = companyService.getLinkedCompany(email);
        }

        // Validate subscription before creating job (skip for admins)
        if (!isAdmin) {
            subscriptionService.validateCanPublishJob(company.getId());
        }

        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());
        Job job = jobMapper.toEntity(request, company);
        Job savedJob = jobRepository.save(job);

        // Increment slots used for non-admin job postings
        if (!isAdmin) {
            try {
                subscriptionService.incrementSlotsUsed(company.getId());
            } catch (Exception e) {
                log.warn("Failed to increment slots used for company {}: {}", company.getId(), e.getMessage());
            }
        }

        return jobMapper.toResponse(savedJob);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> findMyCompanyJobs(String employerEmail, int page, int size) {
        Company company = companyService.getLinkedCompany(employerEmail);
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Job> results = jobRepository.findByCompany_IdOrderByCreatedAtDesc(company.getId(), pageable);
        return PaginationUtils.mapPage(results, jobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> search(
            String keyword,
            EmploymentType employmentType,
            WorkMode workMode,
            String state,
            String industry,
            int page,
            int size) {
        Pageable pageable = PaginationUtils.pageable(
                page, size, Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt"));
        Page<Job> results = jobRepository.search(
                JobStatus.ACTIVE,
                keyword,
                employmentType,
                workMode,
                state,
                industry,
                CompanyStatus.ACTIVE,
                pageable);
        return PaginationUtils.mapPage(results, jobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse findById(UUID id, String viewerEmail, boolean isEmployer, boolean isJobSeeker) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));

        if (job.getStatus() == JobStatus.ACTIVE) {
            Boolean saved = isJobSeeker ? savedJobService.isSaved(viewerEmail, id) : null;
            return jobMapper.toResponse(job, saved);
        }

        if (isEmployer && ownsJob(job, viewerEmail)) {
            return jobMapper.toResponse(job, null);
        }

        throw new ResourceNotFoundException("Job not found: " + id);
    }

    @Transactional(readOnly = true)
    public JobResponse findByIdPublic(UUID id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));

        // Only show active jobs to unauthenticated users
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new ResourceNotFoundException("Job not found: " + id);
        }

        return jobMapper.toResponse(job, null);
    }

    @Transactional
    public JobResponse update(String email, UUID id, JobUpdateRequest request) {
        var currentUser = SecurityUtils.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        
        Job job;
        if (isAdmin) {
            job = jobRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        } else {
            job = getOwnedJob(email, id);
        }
        
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("Closed jobs cannot be updated");
        }
        validateSalaryRange(
                request.getSalaryMin() != null ? request.getSalaryMin() : job.getSalaryMin(),
                request.getSalaryMax() != null ? request.getSalaryMax() : job.getSalaryMax());
        jobMapper.applyUpdate(job, request);
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse publish(String email, UUID id) {
        var currentUser = SecurityUtils.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isAdmin) {
            companyService.requireActiveCompany(email);
        }

        Job job;
        if (isAdmin) {
            job = jobRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
            if (job.getCompany().getStatus() != CompanyStatus.ACTIVE) {
                throw new BadRequestException("Cannot publish job for inactive company");
            }
        } else {
            job = getOwnedJob(email, id);
        }

        if (job.getStatus() != JobStatus.DRAFT) {
            throw new BadRequestException("Only draft jobs can be published");
        }

        // Validate subscription before publishing job (skip for admins)
        if (!isAdmin) {
            subscriptionService.validateCanPublishJob(job.getCompany().getId());
        }

        job.setStatus(JobStatus.ACTIVE);
        job.setPublishedAt(LocalDateTime.now());
        Job saved = jobRepository.save(job);
        jobAlertService.notifyMatchingSeekers(saved);
        return jobMapper.toResponse(saved);
    }

    @Transactional
    public JobResponse close(String email, UUID id) {
        var currentUser = SecurityUtils.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        
        Job job;
        if (isAdmin) {
            job = jobRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        } else {
            job = getOwnedJob(email, id);
        }
        
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new BadRequestException("Only active jobs can be closed");
        }
        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(LocalDateTime.now());
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public long countActiveJobs(UUID companyId) {
        return jobRepository.countByCompany_IdAndStatus(companyId, JobStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countDraftJobs(UUID companyId) {
        return jobRepository.countByCompany_IdAndStatus(companyId, JobStatus.DRAFT);
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

    private boolean ownsJob(Job job, String employerEmail) {
        try {
            Company company = companyService.getLinkedCompany(employerEmail);
            return job.getCompany().getId().equals(company.getId());
        } catch (BadRequestException | ResourceNotFoundException ex) {
            return false;
        }
    }

    private void validateSalaryRange(Long min, Long max) {
        if (min != null && max != null && min > max) {
            throw new BadRequestException("Minimum salary cannot exceed maximum salary");
        }
    }
}
