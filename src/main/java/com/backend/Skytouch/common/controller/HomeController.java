package com.backend.Skytouch.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/swagger-ui.html");
    }

    @GetMapping("/api")
    public Map<String, Object> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Skytouch API");
        response.put("description", "Skytouch is a comprehensive job board platform connecting employers with job seekers");
        response.put("documentation", "Interactive API documentation available at /swagger-ui.html");
        response.put("apiDocs", "OpenAPI specification available at /v3/api-docs");
        response.put("version", "1.0.0");
        return response;
    }
}
