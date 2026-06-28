package com.backend.Skytouch.authentication.apimodel;

import com.backend.Skytouch.common.enums.UserType;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotNull(message = "User type is required")
    @Schema(
            description = "Account type to register",
            allowableValues = {"JOB_SEEKER", "EMPLOYER"},
            example = "JOB_SEEKER"
    )
    @JsonAlias("role")
    private UserType userType;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "confirm your password")
    private String confirmPassword;

    @Size(max = 255, message = "First name must not exceed 255 characters")
    private String firstName;

    @Size(max = 255, message = "Middle name must not exceed 255 characters")
    private String middleName;

    @Size(max = 255, message = "Last name must not exceed 255 characters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must contain 10 to 15 digits and may start with +"
    )
    private String phone;

    /** Optional at signup; used when userType is EMPLOYER. */
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;
}
