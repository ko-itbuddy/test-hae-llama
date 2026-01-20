package com.example.demo.client.impl;

import com.example.demo.client.SmsClient;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SmsClientImpl implements SmsClient {
    @Override
    public boolean sendSms(String phone, String msg) {
        // log.info("Sending SMS to {}: {}", phone, msg);
        System.out.println("Sending SMS to " + phone + ": " + msg);
        return true;
    }
}
