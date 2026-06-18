package com.backend.Skytouch.jobseeker.apimodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterJobSeekerRequest {

    private String email;
    private String password;
}
