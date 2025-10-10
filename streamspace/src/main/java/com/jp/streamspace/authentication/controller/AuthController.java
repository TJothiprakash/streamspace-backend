package com.jp.streamspace.authentication.controller;

import com.jp.streamspace.authentication.config.JwtUtils;
import com.jp.streamspace.authentication.modal.User;
import com.jp.streamspace.authentication.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletResponse response) {
        logger.info("🔹 Received login request for user: {}", loginRequest.getEmail());

        ResponseEntity<?> loginResp;
        try {
            logger.debug("➡ Calling AuthService.login() for authentication...");
            loginResp = authService.login(loginRequest);
            logger.debug("✅ AuthService.login() completed successfully.");
        } catch (Exception e) {
            logger.error("❌ Error during authentication: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Login failed"));
        }

        // Extract token from response
        String token = null;
        if (loginResp.getBody() instanceof Map) {
            token = ((Map<String, String>) loginResp.getBody()).get("token");
        }

        if (token == null) {
            logger.warn("⚠ No JWT token found in login response body.");
            return ResponseEntity.badRequest().body(Map.of("error", "Token missing in response"));
        }

        logger.info("🔑 JWT token successfully generated for user: {}", loginRequest.getEmail());

        // Set JWT cookie
        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(false); // Consider changing to true in production
        jwtCookie.setMaxAge(60 * 60);

        response.addCookie(jwtCookie);
        logger.info("🍪 JWT cookie added to response for userID: {}", loginRequest.getUserId());

        logger.info("✅ Login process completed successfully for user: {}", loginRequest.getEmail());
        return loginResp;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User registerRequest, HttpServletResponse response) {
        logger.info("📝 Received registration request for user: {}", registerRequest.getUsername());

        try {
            ResponseEntity<?> resp = authService.register(registerRequest, response);
            logger.info("✅ Registration successful for user: {}", registerRequest.getUsername());
            return resp;
        } catch (Exception e) {
            logger.error("❌ Error during registration for user {}: {}", registerRequest.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Registration failed"));
        }
    }
}
