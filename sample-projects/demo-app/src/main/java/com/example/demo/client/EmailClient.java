package com.example.demo.client;

public interface EmailClient {
    boolean sendEmail(String email, String title, String body);
}