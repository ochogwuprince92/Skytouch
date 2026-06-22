package com.backend.Skytouch.jobseeker.apimodel;

import com.backend.Skytouch.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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

    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String job;
    private String qualification;
    private String cv;
    private String about;
    private Boolean openToWork;
    private String addressState;
    private String addressLga;
    private String addressLine;

    private String nin;
    private LocalDate birthday;
    private String gender;
    private String addressNo;
}
