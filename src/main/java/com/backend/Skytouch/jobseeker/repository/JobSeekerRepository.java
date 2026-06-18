package com.backend.Skytouch.jobseeker.repository;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobSeekerRepository extends JpaRepository<Users, UUID> {

    List<Users> findByRole(UserRole role);

    Optional<Users> findByIdAndRole(UUID id, UserRole role);

    Optional<Users> findByEmailAndRole(String email, UserRole role);
}
