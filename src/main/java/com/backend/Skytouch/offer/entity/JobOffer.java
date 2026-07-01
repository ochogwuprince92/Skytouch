package com.backend.Skytouch.offer.entity;

import com.backend.Skytouch.application.entity.JobApplication;
import com.backend.Skytouch.common.enums.OfferStatus;
import com.backend.Skytouch.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_offers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOffer {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private JobApplication application;

    @Column(name = "salary_amount")
    private Long salaryAmount;

    @Column(name = "salary_currency", nullable = false)
    private String salaryCurrency;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @Column(name = "offered_at", nullable = false, updatable = false)
    private LocalDateTime offeredAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offered_by_user_id", nullable = false)
    private Users offeredBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
