package com.backend.Skytouch.company.apimodel;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyUpdateRequest {

    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String name;

    private String description;

    @Size(max = 255, message = "Industry must not exceed 255 characters")
    private String industry;

    @Size(max = 500, message = "Website must not exceed 500 characters")
    private String website;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
}
