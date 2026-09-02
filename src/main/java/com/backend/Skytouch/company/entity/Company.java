package com.backend.Skytouch.company.entity;

import com.backend.Skytouch.common.enums.CompanyStatus;
import com.backend.Skytouch.common.enums.Industry;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Industry industry;

    private String website;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "address_lga")
    private String addressLga;

    @Column(name = "address_state")
    private String addressState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
