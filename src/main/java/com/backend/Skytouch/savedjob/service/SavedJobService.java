package com.backend.Skytouch.savedjob.service;

import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ConflictException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.JobMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.job.apimodel.JobResponse;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.savedjob.entity.SavedJob;
import com.backend.Skytouch.savedjob.repository.SavedJobRepository;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private static final UserRole JOB_SEEKER_ROLE = UserRole.JOB_SEEKER;

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Transactional
    public void save(String seekerEmail, UUID jobId) {
        JobSeeker jobSeeker = getJobSeekerProfile(seekerEmail);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new BadRequestException("Only active jobs can be saved");
        }
        if (savedJobRepository.existsByJobSeeker_User_EmailAndJob_Id(seekerEmail, jobId)) {
            throw new ConflictException("Job is already saved");
        }
        savedJobRepository.save(SavedJob.builder().jobSeeker(jobSeeker).job(job).build());
    }

    @Transactional
    public void unsave(String seekerEmail, UUID jobId) {
        if (!savedJobRepository.existsByJobSeeker_User_EmailAndJob_Id(seekerEmail, jobId)) {
            throw new ResourceNotFoundException("Saved job not found: " + jobId);
        }
        savedJobRepository.deleteByJobSeeker_User_EmailAndJob_Id(seekerEmail, jobId);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> findMySavedJobs(String seekerEmail, int page, int size) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "savedAt"));
        Page<SavedJob> results = savedJobRepository.findActiveSavedJobsForSeeker(
                seekerEmail, JobStatus.ACTIVE, pageable);
        return PaginationUtils.mapPage(results, saved -> jobMapper.toResponse(saved.getJob(), true));
    }

    @Transactional(readOnly = true)
    public long countForSeeker(String seekerEmail) {
        return savedJobRepository.countByJobSeeker_User_Email(seekerEmail);
    }

    @Transactional(readOnly = true)
    public boolean isSaved(String seekerEmail, UUID jobId) {
        return savedJobRepository.existsByJobSeeker_User_EmailAndJob_Id(seekerEmail, jobId);
    }

    private JobSeeker getJobSeekerProfile(String email) {
        Users user = userRepository.findByEmailAndRole(email, JOB_SEEKER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + email));
        return jobSeekerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile not found: " + email));
    }
}
