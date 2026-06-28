package com.backend.Skytouch.application.repository;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    boolean existsByJob_IdAndJobSeeker_Id(UUID jobId, UUID jobSeekerId);

    Page<JobApplication> findByJobSeeker_User_EmailOrderByAppliedAtDesc(String email, Pageable pageable);

    Page<JobApplication> findByJob_IdOrderByAppliedAtDesc(UUID jobId, Pageable pageable);

    Optional<JobApplication> findByIdAndJobSeeker_User_Email(UUID id, String email);

    Optional<JobApplication> findByIdAndJob_Id(UUID id, UUID jobId);

    long countByJobSeeker_User_EmailAndStatusNot(String email, ApplicationStatus status);

    long countByJob_Company_IdAndStatusNot(UUID companyId, ApplicationStatus status);
}
