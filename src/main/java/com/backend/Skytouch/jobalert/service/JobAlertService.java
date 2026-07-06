package com.backend.Skytouch.jobalert.service;

import com.backend.Skytouch.common.apimodel.PageResponse;
import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.exception.BadRequestException;
import com.backend.Skytouch.common.exception.ResourceNotFoundException;
import com.backend.Skytouch.common.mapper.JobAlertMapper;
import com.backend.Skytouch.common.util.PaginationUtils;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.jobalert.apimodel.JobAlertCreateRequest;
import com.backend.Skytouch.jobalert.apimodel.JobAlertResponse;
import com.backend.Skytouch.jobalert.apimodel.JobAlertUpdateRequest;
import com.backend.Skytouch.jobalert.entity.JobAlert;
import com.backend.Skytouch.jobalert.repository.JobAlertRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobAlertService {

    private static final UserRole JOB_SEEKER_ROLE = UserRole.JOB_SEEKER;

    private final JobAlertRepository jobAlertRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final JobAlertMapper jobAlertMapper;
    private final NotificationService notificationService;
    private final JobAlertDeliveryService deliveryService;

    @Transactional
    public JobAlertResponse create(String seekerEmail, JobAlertCreateRequest request) {
        validateCriteria(request.getKeyword(), request.getEmploymentType(), request.getWorkMode(),
                request.getLocationState(), request.getIndustry());
        JobSeeker jobSeeker = getJobSeekerProfile(seekerEmail);
        JobAlert alert = jobAlertMapper.toEntity(jobSeeker, request);
        return jobAlertMapper.toResponse(jobAlertRepository.save(alert));
    }

    @Transactional(readOnly = true)
    public PageResponse<JobAlertResponse> findMyAlerts(String seekerEmail, int page, int size) {
        Pageable pageable = PaginationUtils.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobAlert> results = jobAlertRepository.findByJobSeeker_User_EmailOrderByCreatedAtDesc(
                seekerEmail, pageable);
        return PaginationUtils.mapPage(results, jobAlertMapper::toResponse);
    }

    @Transactional
    public JobAlertResponse update(String seekerEmail, UUID alertId, JobAlertUpdateRequest request) {
        JobAlert alert = jobAlertRepository.findByIdAndJobSeeker_User_Email(alertId, seekerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Job alert not found: " + alertId));
        jobAlertMapper.applyUpdate(alert, request);
        validateCriteria(alert.getKeyword(), alert.getEmploymentType(), alert.getWorkMode(),
                alert.getLocationState(), alert.getIndustry());
        return jobAlertMapper.toResponse(jobAlertRepository.save(alert));
    }

    @Transactional
    public void delete(String seekerEmail, UUID alertId) {
        JobAlert alert = jobAlertRepository.findByIdAndJobSeeker_User_Email(alertId, seekerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Job alert not found: " + alertId));
        jobAlertRepository.delete(alert);
    }

    @Transactional(readOnly = true)
    public long countActiveForSeeker(String seekerEmail) {
        return jobAlertRepository.countByJobSeeker_User_EmailAndActiveTrue(seekerEmail);
    }

    @Transactional
    public void notifyMatchingSeekers(Job job) {
        String industry = job.getCompany() != null ? job.getCompany().getIndustry() : null;
        List<UUID> userIds = jobAlertRepository.findMatchingSeekerUserIds(
                job.getTitle(),
                job.getDescription(),
                job.getEmploymentType(),
                job.getWorkMode(),
                job.getLocationState(),
                industry);

        for (UUID userId : userIds) {
            userRepository.findById(userId).ifPresent(user -> {
                notificationService.notifyOnJobAlertMatch(user, job);
                deliveryService.recordDelivery(user.getId(), job);
            });
        }
    }

    private void validateCriteria(
            String keyword,
            com.backend.Skytouch.common.enums.EmploymentType employmentType,
            com.backend.Skytouch.common.enums.WorkMode workMode,
            String locationState,
            String industry) {
        boolean hasCriteria = StringUtils.hasText(keyword)
                || employmentType != null
                || workMode != null
                || StringUtils.hasText(locationState)
                || StringUtils.hasText(industry);
        if (!hasCriteria) {
            throw new BadRequestException("At least one search criterion is required for a job alert");
        }
    }

    private JobSeeker getJobSeekerProfile(String email) {
        Users user = userRepository.findByEmailAndRole(email, JOB_SEEKER_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found: " + email));
        return jobSeekerRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile not found: " + email));
    }
}
