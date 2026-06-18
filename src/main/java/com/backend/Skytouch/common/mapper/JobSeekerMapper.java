package com.backend.Skytouch.common.mapper;

import com.backend.Skytouch.jobseeker.apimodel.JobSeekerResponse;
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
}
