package com.backend.Skytouch.jobalert.repository;

import com.backend.Skytouch.jobalert.entity.JobAlertDigestDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobAlertDigestDeliveryRepository extends JpaRepository<JobAlertDigestDelivery, UUID> {

    boolean existsByJobSeeker_IdAndJob_Id(UUID jobSeekerId, UUID jobId);
}
