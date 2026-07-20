package com.backend.Skytouch.admin.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserModerationResponse {

    private final String id;
    private final String email;
    private final String role;
    private final String status;
    private final boolean emailVerified;
    private final String createdAt;
}
