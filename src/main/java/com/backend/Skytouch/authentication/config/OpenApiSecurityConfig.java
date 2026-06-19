package com.backend.Skytouch.authentication.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiSecurityConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenApiCustomizer bearerAuthOpenApiCustomizer() {
        return openApi -> {
            openApi.components(new Components()
                    .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .description("Session token from POST /api/auth/otp/verify")));
            openApi.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
        };
    }
}
