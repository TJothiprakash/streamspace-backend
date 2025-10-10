package com.jp.streamspace.authentication.service;

import com.jp.streamspace.authentication.mapper.UserMapper;
import com.jp.streamspace.authentication.modal.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserMapper userMapper, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public void requestReset(String email) {
        logger.info("Password reset requested for email: {}", email);

        User user = userMapper.findByEmail(email);
        logger.info("userd id retrieved from email is  " + email + "  " + user.getUserId());
        if (user == null) {
            logger.warn("No user found with email: {}", email);
            return; // silently ignore to prevent email enumeration
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        userMapper.saveResetToken(user.getUserId(), token, expiry);

        logger.info("Generated reset token {} for userId {} (expires at {})", token, user.getUserId(), expiry);

        // send email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText("Click the link to reset password: "
                    + "http://frontend-url/reset-password?token=" + token);
            mailSender.send(message);

            logger.info("Password reset email sent to {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        logger.info("Attempting password reset with token: {}", token);

        User user = userMapper.findByResetToken(token);
        if (user == null) {
            logger.warn("Invalid reset token: {}", token);
            return false;
        }

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            logger.warn("Reset token expired for userId {}: {}", user.getUserId(), token);
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userMapper.updatePassword(user);

        logger.info("Password successfully reset for userId {}", user.getUserId());
        return true;
    }
}
