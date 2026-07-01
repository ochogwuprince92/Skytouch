package com.backend.Skytouch.jobalert.service;

import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.job.repository.JobRepository;
import com.backend.Skytouch.jobalert.apimodel.JobAlertDigestRunResponse;
import com.backend.Skytouch.jobalert.config.JobAlertDigestProperties;
import com.backend.Skytouch.jobalert.repository.JobAlertRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import com.backend.Skytouch.notification.service.NotificationService;
import com.backend.Skytouch.user.entity.Users;
import com.backend.Skytouch.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobAlertDigestService {

    private static final int MAX_JOBS_PER_RUN = 500;

    private final JobRepository jobRepository;
    private final JobAlertRepository jobAlertRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final JobAlertDeliveryService deliveryService;
    private final NotificationService notificationService;
    private final JobAlertDigestProperties properties;

    @Transactional
    public JobAlertDigestRunResponse runDigest() {
        LocalDateTime since = LocalDateTime.now().minusHours(properties.getLookbackHours());
        List<Job> recentJobs = jobRepository.findRecentlyPublished(
                JobStatus.ACTIVE,
                CompanyStatus.ACTIVE,
                since,
                PageRequest.of(0, MAX_JOBS_PER_RUN));

        Map<UUID, List<Job>> jobsByUserId = new HashMap<>();
        for (Job job : recentJobs) {
            String industry = job.getCompany() != null ? job.getCompany().getIndustry() : null;
            List<UUID> userIds = jobAlertRepository.findMatchingSeekerUserIds(
                    job.getTitle(),
                    job.getDescription(),
                    job.getEmploymentType(),
                    job.getWorkMode(),
                    job.getLocationState(),
                    industry);

            for (UUID userId : userIds) {
                JobSeeker seeker = jobSeekerRepository.findByUser_Id(userId).orElse(null);
                if (seeker == null || deliveryService.wasDelivered(seeker.getId(), job.getId())) {
                    continue;
                }
                jobsByUserId.computeIfAbsent(userId, ignored -> new ArrayList<>()).add(job);
            }
        }

        int seekersNotified = 0;
        int jobsIncluded = 0;
        for (Map.Entry<UUID, List<Job>> entry : jobsByUserId.entrySet()) {
            Users seeker = userRepository.findById(entry.getKey()).orElse(null);
            if (seeker == null || entry.getValue().isEmpty()) {
                continue;
            }
            notificationService.notifyOnJobAlertDigest(seeker, entry.getValue());
            for (Job job : entry.getValue()) {
                deliveryService.recordDelivery(seeker.getId(), job);
                jobsIncluded++;
            }
            seekersNotified++;
        }

        log.info("Job alert digest completed: {} seekers, {} jobs", seekersNotified, jobsIncluded);
        return JobAlertDigestRunResponse.builder()
                .seekersNotified(seekersNotified)
                .jobsIncluded(jobsIncluded)
                .build();
    }
}
