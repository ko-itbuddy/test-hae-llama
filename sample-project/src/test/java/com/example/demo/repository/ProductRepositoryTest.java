package com.example.demo.repository;



package com.example.demo.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProductRepositoryTest {

    @Autowired()
    private ProductRepository productRepository;

    @org.junit.jupiter.api.AfterEach()
    public void tearDown() {
        productRepository.deleteAll();
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        // No specific setup required for this test case
    }
}
