package com.backend.Skytouch.common.enums;

/**
 * Account types allowed at public registration.
 * Maps to {@link UserRole} — {@link UserRole#ADMIN} is excluded.
 */
public enum UserType {
    JOB_SEEKER,
    EMPLOYER;

    public UserRole toUserRole() {
        return UserRole.valueOf(name());
    }
}
