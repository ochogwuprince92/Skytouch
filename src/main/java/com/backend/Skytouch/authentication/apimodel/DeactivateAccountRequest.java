package com.backend.Skytouch.authentication.apimodel;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeactivateAccountRequest {

    @NotBlank(message = "Password is required to deactivate account")
    private String password;
}
