package com.jp.streamspace.authentication.controller;

import com.jp.streamspace.authentication.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService resetService;

    public PasswordResetController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> body) {
        resetService.requestReset(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        boolean success = resetService.resetPassword(body.get("token"), body.get("newPassword"));
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
}
