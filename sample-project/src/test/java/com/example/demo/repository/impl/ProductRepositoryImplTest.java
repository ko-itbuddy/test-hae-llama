package com.example.demo.repository.impl;



package com.example.demo.repository.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ProductRepositoryImplTest {

    @Autowired()
    private ProductRepositoryImpl productRepositoryImpl;

    @org.junit.jupiter.api.AfterEach()
    public void tearDown() {
        productRepositoryImpl.deleteAll();
    }

    @MockBean
    private JPAQueryFactory queryFactory;

    @SpyBean
    private ProductRepositoryImpl productRepositoryImpl;

    @BeforeEach
    public void setUp() {
        // Setup logic if needed using TestEntityManager or other means
    }
}
