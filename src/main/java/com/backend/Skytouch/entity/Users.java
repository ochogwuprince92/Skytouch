package com.backend.Skytouch.entity;

import com.backend.Skytouch.enums.UserRole;
import com.backend.Skytouch.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores authentication and account information.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    /**
     * Unique users identifier.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * User email address.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt encrypted password.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Platform role.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * User lifecycle status.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    /**
     * Email verification flag.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean emailVerified = false;

    /**
     * Soft account activation flag.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Record creation timestamp.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record update timestamp.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}