package com.jp.streamspace.authentication.service;

import com.jp.streamspace.authentication.mapper.UserMapper;
import com.jp.streamspace.authentication.modal.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Looking up user with email: {}", email);
        User user = userMapper.findByEmail(email);
        logger.debug("User found: {}", user);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singleton(() -> "ROLE_" + user.getRole())
        );
    }
}
