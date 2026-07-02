package com.arya.rbac_policy_manager.auth.service;

import com.arya.rbac_policy_manager.auth.dto.LoginRequest;
import com.arya.rbac_policy_manager.auth.dto.LoginResponse;
import com.arya.rbac_policy_manager.auth.jwt.JwtService;
import com.arya.rbac_policy_manager.auth.security.PlatformUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        // Spring Security already knows how to compare passwords - never do it manually.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        PlatformUserDetails userDetails = (PlatformUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token);
    }
}
