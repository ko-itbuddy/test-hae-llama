package com.example.demo.service;



package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private SmsClient smsClient;

    @Mock
    private EmailClient emailClient;

    @Mock
    private NotificationHistoryRepository historyRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        // Additional setup if needed
    }
}
