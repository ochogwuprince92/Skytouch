package com.backend.Skytouch.job.repository;

import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.JobStatus;
import com.backend.Skytouch.common.enums.WorkMode;
import com.backend.Skytouch.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Page<Job> findByCompany_IdOrderByCreatedAtDesc(UUID companyId, Pageable pageable);

    long countByCompany_IdAndStatus(UUID companyId, JobStatus status);

    long countByStatus(JobStatus status);

    @Query("""
            SELECT j FROM Job j JOIN j.company c
            WHERE j.status = :status
            AND (:keyword IS NULL OR :keyword = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:employmentType IS NULL OR j.employmentType = :employmentType)
            AND (:workMode IS NULL OR j.workMode = :workMode)
            AND (:state IS NULL OR :state = '' OR j.locationState = :state)
            AND (:industry IS NULL OR :industry = '' OR c.industry = :industry)
            AND c.status = :companyStatus
            """)
    Page<Job> search(
            @Param("status") JobStatus status,
            @Param("keyword") String keyword,
            @Param("employmentType") EmploymentType employmentType,
            @Param("workMode") WorkMode workMode,
            @Param("state") String state,
            @Param("industry") String industry,
            @Param("companyStatus") CompanyStatus companyStatus,
            Pageable pageable);

    @Query("""
            SELECT j FROM Job j
            JOIN FETCH j.company c
            WHERE j.status = :status
            AND c.status = :companyStatus
            AND j.publishedAt IS NOT NULL
            AND j.publishedAt >= :since
            ORDER BY j.publishedAt DESC
            """)
    List<Job> findRecentlyPublished(
            @Param("status") JobStatus status,
            @Param("companyStatus") CompanyStatus companyStatus,
            @Param("since") LocalDateTime since,
            Pageable pageable);
}
