package com.backend.Skytouch.offer.repository;

import com.backend.Skytouch.common.enums.ApplicationStatus;
import com.backend.Skytouch.common.enums.OfferStatus;
import com.backend.Skytouch.offer.entity.JobOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JobOfferRepository extends JpaRepository<JobOffer, UUID> {

    Optional<JobOffer> findByApplication_Id(UUID applicationId);

    Optional<JobOffer> findByIdAndApplication_JobSeeker_User_Email(UUID id, String email);

    @Query("""
            SELECT o FROM JobOffer o
            JOIN o.application a
            JOIN a.jobSeeker js
            WHERE js.user.email = :email
            AND o.status = :status
            ORDER BY o.offeredAt DESC
            """)
    Page<JobOffer> findBySeekerEmailAndStatus(
            @Param("email") String email,
            @Param("status") OfferStatus status,
            Pageable pageable);

    long countByApplication_JobSeeker_User_EmailAndStatus(String email, OfferStatus status);

    @Query("""
            SELECT COUNT(o) FROM JobOffer o
            JOIN o.application a
            JOIN a.job j
            WHERE j.company.id = :companyId
            AND o.status = :status
            """)
    long countByCompanyIdAndStatus(
            @Param("companyId") UUID companyId,
            @Param("status") OfferStatus status);

    @Query("""
            SELECT COUNT(a) FROM JobApplication a
            JOIN a.job j
            WHERE j.company.id = :companyId
            AND a.status = :status
            """)
    long countHiredByCompanyId(
            @Param("companyId") UUID companyId,
            @Param("status") ApplicationStatus status);
}
