package com.example.demo.listener;

import com.example.demo.event.OrderPlacedEvent;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class OrderEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public OrderEventListener(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        // log.info("Handling OrderPlacedEvent for order: {}", event.getOrderId());
        System.out.println("Handling OrderPlacedEvent for order: " + event.getOrderId());
        
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            String message = String.format("Order %s placed successfully. Amount: %s", event.getOrderId(), event.getAmount());
            notificationService.sendNotification(user, message);
        });
    }
}
