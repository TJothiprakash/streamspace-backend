package com.jp.streamsspce;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
public class MailTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void sendTestMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("jothiprakash.thangaraj@gmail.com");
        message.setSubject("Test Email");
        message.setText("Hello! This is a test email from Spring Boot.");
        mailSender.send(message);
        System.out.println("Mail sent!");
    }
}
