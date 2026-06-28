package com.backend.Skytouch.authentication.apimodel;

import com.backend.Skytouch.common.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AuthResponse {

    private final String accessToken;
    @Builder.Default
    private final String tokenType = "Bearer";
    private final long expiresIn;
    private final UUID userId;
    private final String email;
    private final UserRole role;
}
