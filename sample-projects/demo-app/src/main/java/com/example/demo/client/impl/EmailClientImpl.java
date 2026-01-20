package com.example.demo.client.impl;

import com.example.demo.client.EmailClient;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailClientImpl implements EmailClient {
    @Override
    public boolean sendEmail(String email, String title, String body) {
        // log.info("Sending Email to {}: {} - {}", email, title, body);
        System.out.println("Sending Email to " + email + ": " + title);
        return true;
    }
}
