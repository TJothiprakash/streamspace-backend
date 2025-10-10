package com.jp.streamspace.authentication.service;

import com.jp.streamspace.authentication.config.JwtUtils;
import com.jp.streamspace.authentication.mapper.UserMapper;
import com.jp.streamspace.authentication.modal.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       UserMapper userMapper,
                       JwtUtils jwtUtils,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Handles user login.
     */
    public ResponseEntity<?> login(User request) {
        logger.info("🔹 Login attempt for user: {}", request.getEmail());

        try {
            logger.debug("➡ Authenticating user with AuthenticationManager...");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            logger.debug("✅ Authentication successful for user: {}", request.getEmail());
        } catch (BadCredentialsException e) {
            logger.warn("⚠ Invalid credentials for user: {}", request.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        } catch (DisabledException e) {
            logger.warn("⚠ User account disabled: {}", request.getEmail());
            return ResponseEntity.status(403).body(Map.of("error", "Account disabled"));
        } catch (LockedException e) {
            logger.warn("⚠ User account locked: {}", request.getEmail());
            return ResponseEntity.status(403).body(Map.of("error", "Account locked"));
        } catch (Exception e) {
            logger.error("❌ Unexpected error during login for user {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Login failed"));
        }

        logger.debug("➡ Generating JWT token for user: {}", request.getEmail());
        String token = jwtUtils.generateToken(request.getEmail());
        logger.info("🔑 JWT token successfully generated for user: {}", request.getEmail());

        logger.info("✅ Login process completed successfully for user: {}", request.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Handles user registration.
     */
    public ResponseEntity<?> register(User request, HttpServletResponse response) {
        logger.info("📝 Registering new user with email: {}", request.getEmail());

        try {
            logger.debug("➡ Encoding password for user: {}", request.getEmail());
            request.setPassword(passwordEncoder.encode(request.getPassword()));
            logger.debug("✅ Password encoded successfully for user: {}", request.getEmail());

            logger.debug("➡ Inserting new user record into database...");
            userMapper.insertUser(request);
            logger.debug("✅ User inserted into database with userId: {}", request.getUserId());

            logger.debug("➡ Generating JWT token for new user...");
            String token = jwtUtils.generateToken(request.getEmail());
            logger.info("🔑 JWT token generated for new user: {}", request.getEmail());

            // Set JWT in cookie
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(false);   // JS accessible (make true in prod)
            cookie.setSecure(false);     // true for HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);   // 1 hour
            response.addCookie(cookie);

            logger.info("🍪 JWT cookie added for user: {}", request.getEmail());
            logger.info("✅ Registration successful for user: {}", request.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "User registered successfully",
                    "userId", request.getUserId()
            ));
        } catch (Exception e) {
            logger.error("❌ Error during registration for user {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Registration failed"));
        }
    }
}
