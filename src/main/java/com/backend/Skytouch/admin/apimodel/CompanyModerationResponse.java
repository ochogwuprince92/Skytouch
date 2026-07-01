package com.backend.Skytouch.admin.apimodel;

import com.backend.Skytouch.common.enums.CompanyStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CompanyModerationResponse {

    private final UUID id;
    private final String name;
    private final String industry;
    private final CompanyStatus status;
}
