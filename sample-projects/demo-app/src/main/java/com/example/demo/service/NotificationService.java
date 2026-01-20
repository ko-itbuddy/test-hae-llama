package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.client.SmsClient;
import com.example.demo.client.EmailClient;
import com.example.demo.repository.NotificationHistoryRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class NotificationService {
    private final SmsClient smsClient;
    private final EmailClient emailClient;
    private final NotificationHistoryRepository historyRepository;

    public NotificationService(SmsClient smsClient, EmailClient emailClient, NotificationHistoryRepository historyRepository) {
        this.smsClient = smsClient;
        this.emailClient = emailClient;
        this.historyRepository = historyRepository;
    }

    public boolean sendNotification(User user, String message) {
        if (user == null || message == null || message.isEmpty()) {
            throw new IllegalArgumentException("User and message must not be null or empty");
        }

        boolean success = false;
        
        // 1. 사용자 등급별 차등 발송
        if ("VIP".equals(user.getGrade())) {
            // VIP는 SMS와 Email 모두 발송
            boolean smsResult = smsClient.sendSms(user.getPhoneNumber(), message);
            boolean emailResult = emailClient.sendEmail(user.getEmail(), "VIP Notification", message);
            success = smsResult || emailResult;
        } else {
            // 일반 사용자는 Email만 발송
            success = emailClient.sendEmail(user.getEmail(), "General Notification", message);
        }

        // 2. 발송 결과 기록
        if (success) {
            historyRepository.save(user.getId(), message, "SUCCESS");
        } else {
            historyRepository.save(user.getId(), message, "FAILED");
        }

        return success;
    }
}
