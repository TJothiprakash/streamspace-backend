package com.jp.streamspace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.jp.streamspace")
public class StreamspaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamspaceApplication.class, args);
    }

}

