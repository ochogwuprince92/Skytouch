package com.backend.Skytouch.authentication.apimodel;

import com.backend.Skytouch.common.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerifiedResponse {

    private final String message;
    private final boolean emailVerified;
    private final UserStatus status;
}
