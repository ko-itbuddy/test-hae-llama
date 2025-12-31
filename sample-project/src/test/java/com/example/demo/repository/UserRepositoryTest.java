package com.example.demo.repository;



package com.example.demo.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Autowired()
    private UserRepository userRepository;

    @org.junit.jupiter.api.AfterEach()
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Autowired
    private TestEntityManager testEntityManager;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRepositoryImpl // Assuming UserRepositoryImpl is the implementation of UserRepository
    userRepositoryImpl;

    @BeforeEach
    public void setUp() {
        // Setup logic can be added here if needed, e.g., initializing entities or setting up mocks
    }
}
