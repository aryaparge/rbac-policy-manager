package com.arya.rbac_policy_manager.api.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Not part of the RBAC domain - exists purely to prove that login, JWT
 * issuance, and the JWT filter all work end to end before building out
 * the real API surface.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/ping")
    public Map<String, String> ping(Authentication authentication) {
        return Map.of(
                "status", "ok",
                "authenticatedAs", authentication.getName()
        );
    }
}
