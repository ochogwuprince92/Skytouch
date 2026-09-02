package com.backend.Skytouch.savedjob.repository;

import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.savedjob.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {

    boolean existsByJobSeeker_User_EmailAndJob_Id(String email, UUID jobId);

    long countByJobSeeker_User_Email(String email);

    Optional<SavedJob> findByJobSeeker_User_EmailAndJob_Id(String email, UUID jobId);

    void deleteByJobSeeker_User_EmailAndJob_Id(String email, UUID jobId);

    @Query("""
            SELECT sj FROM SavedJob sj
            JOIN sj.job j
            WHERE sj.jobSeeker.user.email = :email
            AND j.status = :status
            ORDER BY sj.savedAt DESC
            """)
    Page<SavedJob> findActiveSavedJobsForSeeker(
            @Param("email") String email,
            @Param("status") JobStatus status,
            Pageable pageable);
}
