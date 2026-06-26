package com.backend.Skytouch.jobseeker.entity;

import com.backend.Skytouch.common.enums.UserStatus;
import com.backend.Skytouch.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_seekers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSeeker {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_username", referencedColumnName = "email", nullable = false, unique = true)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String phone;

    private String gender;

    private LocalDate birthday;

    private String religion;

    private String nin;

    private String job;

    private String qualification;

    @Column
    private String cvUrl;

    @Column(columnDefinition = "TEXT")
    private String about;

    @Builder.Default
    @Column(name = "open_to_work", nullable = false)
    private Boolean openToWork = false;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "address_no")
    private String addressNo;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "address_lga")
    private String addressLga;

    @Column(name = "address_state")
    private String addressState;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
