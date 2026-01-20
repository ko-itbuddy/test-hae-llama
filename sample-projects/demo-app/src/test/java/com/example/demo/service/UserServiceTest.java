package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.event.UserCreatedEvent;
import com.example.demo.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.springframework.context.ApplicationEventPublisher;
import org.mockito.MockitoAnnotations;

@DisplayName("UserService 테스트")
public class UserServiceTest {

    @Nested
    class Describe_targetMethod {

        @InjectMocks
        private UserService userService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ApplicationEventPublisher eventPublisher;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
        }

        @Test
        @DisplayName("Should return an empty list when no users exist in the repository")
        void shouldReturnEmptyListWhenNoUsersExist() {
            // given
            given(userRepository.findAll()).willReturn(List.of());
            // when
            List<User> result = userService.findAllUsers();
            // then
            assertThat(result).isEmpty();
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should return a list of users when users exist in the repository")
        public void shouldReturnListOfUsersWhenUsersExist() {
            // given
            List<User> mockUsers = List.of(new User(), new User());
            given(userRepository.findAll()).willReturn(mockUsers);
            // when
            List<User> result = userService.findAllUsers();
            // then
            assertThat(result).isEqualTo(mockUsers);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email is null during user creation")
        public void shouldThrowIllegalArgumentExceptionWhenEmailIsNull() {
            // given
            String name = "John Doe";
            String email = null;
            BDDMockito.given(userRepository.existsByEmail(email)).willReturn(false);
            // when, then
            assertThatThrownBy(() -> userService.createUser(name, email)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email is empty during user creation")
        void shouldThrowIllegalArgumentExceptionWhenEmailIsEmpty() {
            // given
            String name = "John Doe";
            String email = "";
            BDDMockito.given(userRepository.existsByEmail(email)).willReturn(false);
            // when, then
            assertThatThrownBy(() -> userService.createUser(name, email)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email cannot be empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name is null during user creation")
        void shouldThrowIllegalArgumentExceptionWhenNameIsNull() {
            // given
            String name = null;
            String email = "test@example.com";
            // when, then
            assertThatThrownBy(() -> userService.createUser(name, email)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name cannot be null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name is empty during user creation")
        void shouldThrowIllegalArgumentExceptionWhenNameIsEmpty() {
            // given
            String name = "";
            String email = "test@example.com";
            given(userRepository.existsByEmail(email)).willReturn(false);
            // when & then
            assertThatThrownBy(() -> userService.createUser(name, email)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("name cannot be empty");
            verify(userRepository, never()).save(any(User.class));
            verify(eventPublisher, never()).publishEvent(any(UserCreatedEvent.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email already exists during user creation")
        public void shouldThrowIllegalArgumentExceptionWhenEmailAlreadyExists() {
            // given
            String name = "John Doe";
            String email = "john.doe@example.com";
            given(userRepository.existsByEmail(email)).willReturn(true);
            // when, then
            assertThatThrownBy(() -> userService.createUser(name, email)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already exists");
            verify(userRepository).existsByEmail(email);
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("Should create a user and publish UserCreatedEvent when valid data is provided")
        public void testCreateUser_Success() {
            // given
            String name = "John Doe";
            String email = "john.doe@example.com";
            User expectedUser = new User();
            expectedUser.setName(name);
            expectedUser.setEmail(email);
            given(userRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.save(expectedUser)).willReturn(expectedUser);
            // when
            User actualUser = userService.createUser(name, email);
            // then
            assertThat(actualUser).isEqualTo(expectedUser);
            ArgumentCaptor<UserCreatedEvent> eventArgumentCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
            verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
            UserCreatedEvent publishedEvent = eventArgumentCaptor.getValue();
            assertThat(publishedEvent.getSource()).isSameAs(userService);
            assertThat(publishedEvent.getUser()).isEqualTo(expectedUser);
        }
    }

}