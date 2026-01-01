package com.example.demo.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class UserCreatedEventTest {

    @Nested
    @DisplayName("User Created Event Tests")
    public class UserCreatedEventTests {
        
        private final User user = new User();

        @Test
        @DisplayName("Should have a non-null user")
        public void shouldHaveNonNullUser() {
            assertThat(user).isNotNull();
        }
    }
}