package com.backend.Skytouch.messaging.apimodel;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationMessageCreateRequest {

    @NotBlank
    private String body;
}
