package com.backend.Skytouch.common.enums;

public enum UserType {
    JOB_SEEKER,
    EMPLOYER;

    public UserRole toUserRole() {
        return UserRole.valueOf(name());
    }
}
