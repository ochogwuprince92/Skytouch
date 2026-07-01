package com.backend.Skytouch.messaging.repository;

import com.backend.Skytouch.messaging.entity.ApplicationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ApplicationMessageRepository extends JpaRepository<ApplicationMessage, UUID> {

    Page<ApplicationMessage> findByApplication_IdOrderBySentAtAsc(UUID applicationId, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE ApplicationMessage m
            SET m.readAt = CURRENT_TIMESTAMP
            WHERE m.application.id = :applicationId
            AND m.sender.id <> :readerUserId
            AND m.readAt IS NULL
            """)
    int markThreadReadForRecipient(
            @Param("applicationId") UUID applicationId,
            @Param("readerUserId") UUID readerUserId);
}
