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
}
