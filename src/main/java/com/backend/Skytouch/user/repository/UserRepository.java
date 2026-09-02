package com.backend.Skytouch.user.repository;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Users> findByRole(UserRole role);

    Page<Users> findByRole(UserRole role, Pageable pageable);

    Optional<Users> findByIdAndRole(UUID id, UserRole role);

    Optional<Users> findByEmailAndRole(String email, UserRole role);

    Page<Users> findByEmailContaining(String email, Pageable pageable);

    Page<Users> findByStatus(UserStatus status, Pageable pageable);

    Page<Users> findByEmailVerifiedFalse(Pageable pageable);

    long countByRole(UserRole role);

    long countByEmailVerifiedFalse();

    long countByStatus(UserStatus status);
}
