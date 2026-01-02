package com.example.demo.service;

import com.example.demo.event.UserCreatedEvent;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 비즈니스 로직 테스트")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("createUser 메서드는")
    class Describe_createUser {

        @Test
        @DisplayName("중복되지 않은 이메일이면 사용자를 저장하고 이벤트를 발행한다")
        void it_saves_user_and_publishes_event() {
            // given
            String name = "Tester";
            String email = "test@example.com";
            User savedUser = User.builder().id(1L).name(name).email(email).build();

            when(userRepository.existsByEmail(email)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            // when
            User result = userService.createUser(name, email);

            // then
            assertNotNull(result);
            assertEquals(email, result.getEmail());
            verify(userRepository, times(1)).save(any(User.class));
            verify(eventPublisher, times(1)).publishEvent(any(UserCreatedEvent.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일이면 IllegalArgumentException을 던진다")
        void it_throws_exception_for_duplicate_email() {
            // given
            String email = "duplicate@example.com";
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> userService.createUser("Name", email));
        }
    }

    @Nested
    @DisplayName("findAllUsers 메서드는")
    class Describe_findAllUsers {

        @Test
        @DisplayName("저장된 모든 사용자 목록을 반환한다")
        void it_returns_all_users() {
            // given
            List<User> mockUsers = List.of(
                User.builder().id(1L).name("User1").build(),
                User.builder().id(2L).name("User2").build()
            );
            when(userRepository.findAll()).thenReturn(mockUsers);

            // when
            List<User> result = userService.findAllUsers();

            // then
            assertEquals(2, result.size());
            verify(userRepository, times(1)).findAll();
        }
    }
}