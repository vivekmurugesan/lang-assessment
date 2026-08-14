package com.langassessment.controller;

import com.langassessment.dto.AuthDTO;
import com.langassessment.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.ApiResponse<AuthDTO.LoginResponse>> login(
            @RequestBody AuthDTO.LoginRequest request) {
        try {
            AuthDTO.LoginResponse response = userService.authenticate(request);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Login successful", response));
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, "Invalid email or password"));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<AuthDTO.ApiResponse<Boolean>> validateToken(
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = extractToken(token);
            // Token validation is done in JwtAuthenticationFilter
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Token is valid", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, "Invalid token"));
        }
    }

    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Invalid bearer token");
    }
}
