package com.example.demo.repository.impl;



package com.example.demo.repository.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class NotificationHistoryRepositoryImplTest {

    @Autowired()
    private NotificationHistoryRepositoryImpl notificationHistoryRepositoryImpl;

    @org.junit.jupiter.api.AfterEach()
    public void tearDown() {
        notificationHistoryRepositoryImpl.deleteAll();
    }

    @MockBean
    private TestEntityManager testEntityManager;

    @SpyBean
    private NotificationHistoryRepository notificationHistoryRepository;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }
}
