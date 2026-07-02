package com.backend.Skytouch.application.repository;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.common.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    long countByStatus(ApplicationStatus status);

    @Query("""
            SELECT a.status, COUNT(a) FROM JobApplication a
            JOIN a.job j
            WHERE j.company.id = :companyId
            GROUP BY a.status
            """)
    List<Object[]> countGroupedByStatusForCompany(@Param("companyId") UUID companyId);

    @Query("""
            SELECT a.status, COUNT(a) FROM JobApplication a
            WHERE a.job.id = :jobId
            GROUP BY a.status
            """)
    List<Object[]> countGroupedByStatusForJob(@Param("jobId") UUID jobId);

    @Query("""
            SELECT a.status, COUNT(a) FROM JobApplication a
            GROUP BY a.status
            """)
    List<Object[]> countGroupedByStatusPlatform();

    @Query("""
            SELECT j.id, j.title, COUNT(a),
                   SUM(CASE WHEN a.status = :hiredStatus THEN 1 ELSE 0 END)
            FROM JobApplication a
            JOIN a.job j
            WHERE j.company.id = :companyId
            GROUP BY j.id, j.title
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> findJobApplicantSummariesForCompany(
            @Param("companyId") UUID companyId,
            @Param("hiredStatus") ApplicationStatus hiredStatus,
            Pageable pageable);

    @Query("""
            SELECT a FROM JobApplication a
            JOIN FETCH a.job j
            JOIN FETCH j.company
            JOIN FETCH a.jobSeeker s
            JOIN FETCH s.user
            WHERE j.id = :jobId
            ORDER BY a.appliedAt DESC
            """)
    List<JobApplication> findAllByJobIdForExport(@Param("jobId") UUID jobId, Pageable pageable);

    @Query("""
            SELECT a FROM JobApplication a
            JOIN FETCH a.job j
            JOIN FETCH j.company c
            JOIN FETCH a.jobSeeker s
            JOIN FETCH s.user
            WHERE c.id = :companyId
            ORDER BY a.appliedAt DESC
            """)
    List<JobApplication> findAllByCompanyIdForExport(@Param("companyId") UUID companyId, Pageable pageable);

    @Query("""
            SELECT a FROM JobApplication a
            JOIN FETCH a.job j
            JOIN FETCH j.company
            JOIN FETCH a.jobSeeker s
            JOIN FETCH s.user
            WHERE s.user.email = :email
            ORDER BY a.appliedAt DESC
            """)
    List<JobApplication> findAllBySeekerEmailForExport(@Param("email") String email, Pageable pageable);

    @Query("""
            SELECT a FROM JobApplication a
            JOIN FETCH a.job j
            JOIN FETCH j.company
            JOIN FETCH a.jobSeeker s
            JOIN FETCH s.user
            ORDER BY a.appliedAt DESC
            """)
    List<JobApplication> findAllForPlatformExport(Pageable pageable);
}
