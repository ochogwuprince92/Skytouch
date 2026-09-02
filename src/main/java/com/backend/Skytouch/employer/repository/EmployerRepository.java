package com.backend.Skytouch.employer.repository;

import com.backend.Skytouch.employer.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmployerRepository extends JpaRepository<Employer, UUID> {

    Optional<Employer> findByUser_Id(UUID userId);

    Optional<Employer> findByCompany_Id(UUID companyId);

    /**
     * Same lookup as findByUser_Id, but eagerly fetches the linked Company
     * in the same query.
     *
     * Employer.company is a lazy @ManyToOne. A plain findByUser_Id() only
     * loads a Hibernate proxy for it — fine as long as something accesses
     * it while the transaction/session is still open, but any code path
     * that reads profile.getCompany() to build an API response (e.g.
     * EmployerMapper.toResponse()) needs the real data already loaded.
     * Use this method wherever the caller needs company/companyId/
     * companyName to come back correctly populated.
     */
    @Query("SELECT e FROM Employer e LEFT JOIN FETCH e.company WHERE e.user.id = :userId")
    Optional<Employer> findByUser_IdWithCompany(@Param("userId") UUID userId);
}