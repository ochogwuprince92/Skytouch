package com.backend.Skytouch.jobseeker.apimodel;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class JobSeekerOnboardingRequest {

    @Size(max = 255, message = "Job must not exceed 255 characters")
    private String job;

    @Size(max = 255, message = "Qualification must not exceed 255 characters")
    private String qualification;

    private MultipartFile cv;

    private String about;

    private Boolean openToWork;

    @Size(max = 100, message = "Address state must not exceed 100 characters")
    private String addressState;

    @Size(max = 100, message = "Address LGA must not exceed 100 characters")
    private String addressLga;

    @Size(max = 255, message = "Address line must not exceed 255 characters")
    private String addressLine;
}
