package com.backend.Skytouch.job.service;

import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.WorkMode;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.JobMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.company.service.CompanyService;
import com.backend.Skytouch.job.apimodel.JobCreateRequest;
import com.backend.Skytouch.job.apimodel.JobResponse;
import com.backend.Skytouch.job.apimodel.JobUpdateRequest;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyService companyService;
    private final JobMapper jobMapper;

    @Transactional
    public JobResponse create(String employerEmail, JobCreateRequest request) {
        Company company = companyService.getLinkedCompany(employerEmail);
        validateSalaryRange(request.getSalaryMin(), request.getSalaryMax());
        Job job = jobMapper.toEntity(request, company);
        return jobMapper.toResponse(jobRepository.save(job));
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
                pageable);
        return PaginationUtils.mapPage(results, jobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse findById(UUID id, String viewerEmail, boolean isEmployer) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));

        if (job.getStatus() == JobStatus.ACTIVE) {
            return jobMapper.toResponse(job);
        }

        if (isEmployer && ownsJob(job, viewerEmail)) {
            return jobMapper.toResponse(job);
        }

        throw new ResourceNotFoundException("Job not found: " + id);
    }

    @Transactional
    public JobResponse update(String employerEmail, UUID id, JobUpdateRequest request) {
        Job job = getOwnedJob(employerEmail, id);
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
    public JobResponse publish(String employerEmail, UUID id) {
        Job job = getOwnedJob(employerEmail, id);
        if (job.getStatus() != JobStatus.DRAFT) {
            throw new BadRequestException("Only draft jobs can be published");
        }
        job.setStatus(JobStatus.ACTIVE);
        job.setPublishedAt(LocalDateTime.now());
        return jobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse close(String employerEmail, UUID id) {
        Job job = getOwnedJob(employerEmail, id);
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
