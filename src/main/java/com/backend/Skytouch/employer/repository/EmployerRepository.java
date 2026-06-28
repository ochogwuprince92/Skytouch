package com.backend.Skytouch.employer.repository;

import com.backend.Skytouch.employer.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployerRepository extends JpaRepository<Employer, UUID> {

    Optional<Employer> findByUser_Id(UUID userId);
}
