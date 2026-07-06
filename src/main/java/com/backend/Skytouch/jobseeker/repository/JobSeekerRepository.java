package com.backend.Skytouch.jobseeker.repository;

import com.backend.Skytouch.jobseeker.entity.JobSeeker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobSeekerRepository extends JpaRepository<JobSeeker, UUID> {

    Optional<JobSeeker> findByUser_Email(String email);

    Optional<JobSeeker> findByUser_Id(UUID userId);
}
