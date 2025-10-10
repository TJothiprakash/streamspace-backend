package com.jp.streamspace.authentication.modal;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class User {
    private Integer userId;
    private String username;
    private String email;       // ✅ standardized to "email"
    private String password;
    private String role;        // ✅ added
    private boolean enabled;    // ✅ added
    private String authProvider;
    private String providerId;
    private LocalDateTime lastLogin;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
}
