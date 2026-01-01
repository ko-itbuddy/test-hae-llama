package com.example.demo.repository;



package com.example.demo.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PaymentRepositoryTest {

    @Autowired()
    private PaymentRepository paymentRepository;

    @org.junit.jupiter.api.AfterEach()
    public void tearDown() {
        paymentRepository.deleteAll();
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;
}
