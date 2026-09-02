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
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }
            components.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .description("Bearer token from POST /api/auth/login (accessToken field)"));
            openApi.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
        };
    }
}
