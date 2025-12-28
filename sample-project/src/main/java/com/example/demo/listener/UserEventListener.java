package com.example.demo.listener;

import com.example.demo.event.UserCreatedEvent;
import com.example.demo.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class UserEventListener {

    private final NotificationService notificationService;

    public UserEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        // log.info("Handling UserCreatedEvent for user: {}", event.getUser().getEmail());
        System.out.println("Handling UserCreatedEvent for user: " + event.getUser().getEmail());
        notificationService.sendNotification(event.getUser(), "Welcome to our service!");
    }
}
