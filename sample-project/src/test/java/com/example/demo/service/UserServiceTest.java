package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.event.UserCreatedEvent;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    // Successful retrieval of all users from the database.
    @ParameterizedTest
    @CsvSource(value = { "user1@example.com, User 1", "user2@example.com, User 2" }, nullValues = "null")
    public void testFindAllUsers_Success(String email, String name) {
        // given
        User user1 = User.builder().email(email).name(name).build();
        List<User> users = Collections.singletonList(user1);
        when(userRepository.findAll()).thenReturn(users);
        // when
        List<User> result = userService.findAllUsers();
        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(email, result.get(0).getEmail());
        assertEquals(name, result.get(0).getName());
    }

    // Handling case where the userRepository returns an empty list.
    @Test
    public void testFindAllUsers_EmptyList() {
        // given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        // when
        List<User> result = userService.findAllUsers();
        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Proper handling and propagation of exceptions thrown by userRepository.findAll().
    @Test
    public void testFindAllUsers_ThrowsException() {
        // given
        RuntimeException exception = new RuntimeException("Database error");
        when(userRepository.findAll()).thenThrow(exception);
        // when & then
        assertThrows(RuntimeException.class, () -> userService.findAllUsers());
    }
}
