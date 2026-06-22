package com.backend.Skytouch.jobseeker.apimodel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterJobSeekerRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 255, message = "First name must not exceed 255 characters")
    private String firstName;

    @Size(max = 255, message = "Middle name must not exceed 255 characters")
    private String middleName;

    @Size(max = 255, message = "Last name must not exceed 255 characters")
    private String lastName;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;
}
