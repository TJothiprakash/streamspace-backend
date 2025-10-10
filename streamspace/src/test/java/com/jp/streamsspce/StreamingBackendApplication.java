package com.jp.streamsspce;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class StreamingBackendApplication implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public StreamingBackendApplication(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(StreamingBackendApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        System.out.println("DB Connection Test: " + result);
    }
}
