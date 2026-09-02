package com.backend.Skytouch.authentication.apimodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OtpSentResponse {

    private final String message;
    private final long expiresIn;
}
