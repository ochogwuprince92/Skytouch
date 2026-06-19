package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.authentication.apimodel.OtpSentResponse;
import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
import com.backend.Skytouch.jobseeker.apimodel.RegisterJobSeekerResponse;
import com.backend.Skytouch.user.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class JobSeekerMapper {

    public JobSeekerResponse toResponse(Users user) {
        return JobSeekerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public RegisterJobSeekerResponse toRegisterResponse(Users user, OtpSentResponse verification) {
        return RegisterJobSeekerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .verificationMessage(verification.getMessage())
                .verificationExpiresIn(verification.getExpiresIn())
                .build();
    }
}
