package com.example.demo.repository;

public interface NotificationHistoryRepository {
    void save(Long userId, String message, String status);
}