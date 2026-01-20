package com.example.demo.repository;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(com.example.demo.config.QueryDslConfig.class)
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");
        testEntityManager.persist(user1);
        User user2 = new User();
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        testEntityManager.persist(user2);
    }

    @Nested
    @DisplayName("Tests for existsByEmail method")
    class ExistsByEmailTests {

        @ParameterizedTest(name = "Email: {0}, Expected: {1}")
        @CsvSource({ "john.doe@example.com, true", "jane.smith@example.com, true", "nonexistent@example.com, false" })
        void testExistsByEmail(String email, boolean expected) {
            boolean result = userRepository.existsByEmail(email);
            assertThat(result).isEqualTo(expected);
        }
    }
}