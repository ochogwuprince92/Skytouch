package com.backend.Skytouch.subscription.repository;

import com.backend.Skytouch.company.entity.Company;
import com.backend.Skytouch.subscription.entity.EmployerSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<EmployerSubscription, UUID> {
    Optional<EmployerSubscription> findByCompany(Company company);
    Optional<EmployerSubscription> findByCompanyId(UUID companyId);
    boolean existsByCompanyId(UUID companyId);
}
