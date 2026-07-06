package com.backend.Skytouch.authentication.apimodel;

import com.backend.Skytouch.common.enums.UserRole;
import com.backend.Skytouch.common.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RegisterResponse {

    private final UUID id;
    private final String email;
    private final UserRole role;
    private final UserStatus status;
    private final boolean emailVerified;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final String verificationMessage;
    private final long verificationExpiresIn;
}
