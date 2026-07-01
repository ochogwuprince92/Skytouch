package com.backend.Skytouch.jobalert.service;

import com.backend.Skytouch.job.entity.Job;
import com.backend.Skytouch.jobalert.entity.JobAlertDigestDelivery;
import com.backend.Skytouch.jobalert.repository.JobAlertDigestDeliveryRepository;
import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import com.backend.Skytouch.jobseeker.repository.JobSeekerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobAlertDeliveryService {

    private final JobAlertDigestDeliveryRepository deliveryRepository;
    private final JobSeekerRepository jobSeekerRepository;

    @Transactional(readOnly = true)
    public boolean wasDelivered(UUID jobSeekerId, UUID jobId) {
        return deliveryRepository.existsByJobSeeker_IdAndJob_Id(jobSeekerId, jobId);
    }

    @Transactional
    public void recordDelivery(UUID userId, Job job) {
        JobSeeker seeker = jobSeekerRepository.findByUser_Id(userId).orElse(null);
        if (seeker == null || deliveryRepository.existsByJobSeeker_IdAndJob_Id(seeker.getId(), job.getId())) {
            return;
        }
        deliveryRepository.save(JobAlertDigestDelivery.builder()
                .jobSeeker(seeker)
                .job(job)
                .build());
    }
}
