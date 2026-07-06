package com.backend.Skytouch.interview.repository;

import com.backend.Skytouch.common.enums.InterviewStatus;
import com.backend.Skytouch.interview.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    Optional<Interview> findByApplication_Id(UUID applicationId);

    @Query("""
            SELECT i FROM Interview i
            JOIN i.application a
            JOIN a.jobSeeker js
            WHERE js.user.email = :email
            AND i.status = :status
            AND i.scheduledAt >= :from
            ORDER BY i.scheduledAt ASC
            """)
    Page<Interview> findUpcomingForSeeker(
            @Param("email") String email,
            @Param("status") InterviewStatus status,
            @Param("from") LocalDateTime from,
            Pageable pageable);

    @Query("""
            SELECT COUNT(i) FROM Interview i
            JOIN i.application a
            JOIN a.jobSeeker js
            WHERE js.user.email = :email
            AND i.status = :status
            AND i.scheduledAt >= :from
            """)
    long countUpcomingForSeeker(
            @Param("email") String email,
            @Param("status") InterviewStatus status,
            @Param("from") LocalDateTime from);
}
