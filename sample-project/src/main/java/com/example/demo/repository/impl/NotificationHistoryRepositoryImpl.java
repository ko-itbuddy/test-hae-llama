package com.example.demo.repository.impl;

import com.example.demo.repository.NotificationHistoryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationHistoryRepositoryImpl implements NotificationHistoryRepository {
    @Override
    public void save(Long userId, String message, String status) {
        System.out.println("Saved notification history for user " + userId + ": " + status);
    }
}
