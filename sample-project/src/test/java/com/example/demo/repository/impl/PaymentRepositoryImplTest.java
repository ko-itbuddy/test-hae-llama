package com.example.demo.repository.impl;



package com.example.demo.repository.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PaymentRepositoryImplTest {

    @Autowired()
    private PaymentRepositoryImpl paymentRepositoryImpl;

    @org.junit.jupiter.api.AfterEach()
    public void tearDown() {
        paymentRepositoryImpl.deleteAll();
    }

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    public void setUp() {
        // Setup logic if needed using testEntityManager
    }
}
