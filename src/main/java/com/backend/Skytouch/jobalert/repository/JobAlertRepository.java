package com.backend.Skytouch.jobalert.repository;

import com.backend.Skytouch.common.enums.EmploymentType;
import com.backend.Skytouch.common.enums.WorkMode;
import com.backend.Skytouch.jobalert.entity.JobAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobAlertRepository extends JpaRepository<JobAlert, UUID> {

    Page<JobAlert> findByJobSeeker_User_EmailOrderByCreatedAtDesc(String email, Pageable pageable);

    Optional<JobAlert> findByIdAndJobSeeker_User_Email(UUID id, String email);

    long countByJobSeeker_User_EmailAndActiveTrue(String email);

    @Query("""
            SELECT DISTINCT a.jobSeeker.user.id FROM JobAlert a
            WHERE a.active = true
            AND (a.keyword IS NULL OR a.keyword = ''
                 OR LOWER(:title) LIKE LOWER(CONCAT('%', a.keyword, '%'))
                 OR LOWER(:description) LIKE LOWER(CONCAT('%', a.keyword, '%')))
            AND (a.employmentType IS NULL OR a.employmentType = :employmentType)
            AND (a.workMode IS NULL OR a.workMode = :workMode)
            AND (a.locationState IS NULL OR a.locationState = '' OR a.locationState = :locationState)
            AND (a.industry IS NULL OR a.industry = '' OR a.industry = :industry)
            """)
    List<UUID> findMatchingSeekerUserIds(
            @Param("title") String title,
            @Param("description") String description,
            @Param("employmentType") EmploymentType employmentType,
            @Param("workMode") WorkMode workMode,
            @Param("locationState") String locationState,
            @Param("industry") String industry);
}
