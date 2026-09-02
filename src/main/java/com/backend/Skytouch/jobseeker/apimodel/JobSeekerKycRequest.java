package com.backend.Skytouch.jobseeker.apimodel;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class JobSeekerKycRequest {

    @Size(max = 50, message = "NIN must not exceed 50 characters")
    private String nin;

    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;

    @Size(max = 50, message = "Gender must not exceed 50 characters")
    private String gender;

    @Size(max = 100, message = "Address number must not exceed 100 characters")
    private String addressNo;

    @Size(max = 255, message = "Address line must not exceed 255 characters")
    private String addressLine;

    @Size(max = 100, message = "Address LGA must not exceed 100 characters")
    private String addressLga;

    @Size(max = 100, message = "Address state must not exceed 100 characters")
    private String addressState;

    /** Free-text address validated via Google Geocoding when configured. */
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
}
