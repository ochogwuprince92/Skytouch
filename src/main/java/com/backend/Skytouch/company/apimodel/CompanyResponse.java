package com.backend.Skytouch.company.apimodel;

import com.backend.Skytouch.common.enums.CompanyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CompanyResponse {

    private final UUID id;
    private final String name;
    private final String description;
    private final String industry;
    private final String website;
    private final String logoUrl;
    private final String addressLine;
    private final String addressLga;
    private final String addressState;
    private final CompanyStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
