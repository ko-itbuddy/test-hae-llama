package com.example.demo.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.math.BigDecimal;

@Getter
public class OrderPlacedEvent extends ApplicationEvent {
    private final String orderId;
    private final Long userId;
    private final BigDecimal amount;

    public OrderPlacedEvent(Object source, String orderId, Long userId, BigDecimal amount) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
    }
}
