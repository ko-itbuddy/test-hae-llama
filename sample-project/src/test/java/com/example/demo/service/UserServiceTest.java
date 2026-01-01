package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Collections;
import java.util.List;
import com.example.demo.event.UserCreatedEvent;
import com.example.demo.model.User;
import org.springframework.dao.DataAccessException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @CsvSource({ "John Doe, john.doe@example.com", "Jane Smith, jane.smith@example.com" })
    void testCreateUserWithValidNameAndEmail(String name, String email) {
        // given
        when(userRepository.existsByEmail(email)).thenReturn(false);
        // when
        User user = userService.createUser(name, email);
        // then
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, times(1)).publishEvent(any(UserCreatedEvent.class));
    }

    @ParameterizedTest
    @CsvSource({ "John Doe, john.doe@example.com", "Jane Smith, jane.smith@example.com" })
    public void createUser_ShouldPublishUserCreatedEvent(String name, String email) {
        // given
        when(userRepository.existsByEmail(email)).thenReturn(false);
        // when
        userService.createUser(name, email);
        // then
        verify(eventPublisher, times(1)).publishEvent(any(UserCreatedEvent.class));
    }

    @ParameterizedTest
    @CsvSource({ "John Doe, john.doe@example.com", "Jane Smith, jane.smith@example.com" })
    public void testCreateUser_duplicateEmail(String name, String email) {
        // given
        when(userRepository.existsByEmail(email)).thenReturn(true);
        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(name, email));
    }

    @Test
    public void testFindAllUsers_emptyList() {
        // given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        // when
        List<User> users = userService.findAllUsers();
        // then
        assertEquals(Collections.emptyList(), users);
    }

    @Test
    public void testFindAllUsers_DataAccessException() {
        // given
        when(userRepository.findAll()).thenThrow(new DataAccessException("Data access error") {});
        // when
        Exception exception = assertThrows(DataAccessException.class, () -> userService.findAllUsers());
        // then
        assertEquals("Data access error", exception.getMessage());
        verify(eventPublisher, never()).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    public void testFindAllUsers_NoUsersDueToDatabaseConnectionIssue() {
        // given
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database connection issue"));
        // when
        Exception exception = assertThrows(RuntimeException.class, () -> userService.findAllUsers());
        // then
        assertEquals("Database connection issue", exception.getMessage());
        verify(userRepository, times(1)).findAll();
    }
}