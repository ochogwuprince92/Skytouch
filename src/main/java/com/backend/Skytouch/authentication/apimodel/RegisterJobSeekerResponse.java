package com.backend.Skytouch.authentication.apimodel;

import com.backend.Skytouch.common.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RegisterJobSeekerResponse {

    private final UUID id;
    private final String email;
    private final UserStatus status;
    private final boolean emailVerified;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final String verificationMessage;
    private final long verificationExpiresIn;
}
