package com.backend.Skytouch.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI skytouchOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Skytouch API")
                        .description("Skytouch backend API")
                        .version("1.0.0"));
    }
}
