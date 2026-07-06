package com.backend.Skytouch.employer.apimodel;

import com.backend.Skytouch.common.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EmployerResponse {

    private final UUID id;
    private final String email;
    private final UserStatus status;
    private final boolean emailVerified;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String companyName;
    private final UUID companyId;
    private final String jobTitle;
}
