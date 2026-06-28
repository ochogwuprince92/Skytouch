package com.backend.Skytouch.authentication.repository;

import com.backend.Skytouch.authentication.entity.OtpCode;
import com.backend.Skytouch.common.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findByUserIdAndPurposeAndConsumedAtIsNullAndExpiresAtAfter(
            UUID userId, OtpPurpose purpose, LocalDateTime now);

    @Modifying
    @Query("UPDATE OtpCode o SET o.consumedAt = :now WHERE o.userId = :userId AND o.purpose = :purpose AND o.consumedAt IS NULL")
    void invalidatePending(@Param("userId") UUID userId,
                           @Param("purpose") OtpPurpose purpose,
                           @Param("now") LocalDateTime now);
}
