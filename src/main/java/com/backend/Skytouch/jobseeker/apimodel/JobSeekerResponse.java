package com.backend.Skytouch.jobseeker.apimodel;

import com.backend.Skytouch.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerResponse {

    private UUID id;
    private String email;
    private UserStatus status;
    private boolean emailVerified;
    private boolean active;
    private LocalDateTime createdAt;
}
