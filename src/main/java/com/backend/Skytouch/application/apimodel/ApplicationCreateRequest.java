package com.backend.Skytouch.application.apimodel;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationCreateRequest {

    private String coverLetter;
    private MultipartFile cv;
//    private String cvUrl;
}
