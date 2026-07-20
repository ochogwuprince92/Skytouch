package com.backend.Skytouch.company.apimodel;

import com.backend.Skytouch.common.enums.Industry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyCreateRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String name;

    private String description;

    private Industry industry;

    @Size(max = 500, message = "Website must not exceed 500 characters")
    private String website;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
}
