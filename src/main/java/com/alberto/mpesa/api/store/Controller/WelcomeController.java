package com.alberto.mpesa.api.store.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class WelcomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Cookie Store API");
        response.put("status", "running");
        response.put("endpoints", Map.of(
                "admin_login", "/auth/admin/login",
                "health", "/health",
                "h2_console", "/h2-console"
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Cookie Store API is running");
        return ResponseEntity.ok(response);
    }
}
