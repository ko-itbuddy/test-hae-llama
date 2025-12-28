package com.example.demo.client;

public interface SmsClient {
    boolean sendSms(String phone, String msg);
}