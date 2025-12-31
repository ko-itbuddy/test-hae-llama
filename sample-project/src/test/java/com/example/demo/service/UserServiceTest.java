package com.example.demo.service;



package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser_Success() {
        // Arrange
        String name = "John Doe";
        String email = "john.doe@example.com";
        User expectedUser = new User(name, email);
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        // Act
        User result = userService.createUser(name, email);
        // Assert
        assertEquals(expectedUser, result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateEmailHandling() {
        // Arrange
        String name = "John Doe";
        String email = "john.doe@example.com";
        User existingUser = new User();
        existingUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> {
            userService.createUser(name, email);
        });
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_InvalidInput() {
        // Arrange
        String invalidName = "";
        String validEmail = "user@example.com";
        when(userRepository.existsByEmail(validEmail)).thenReturn(false);
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> userService.createUser(invalidName, validEmail));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testFindAllUsers_Success() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(new User(1, "Alice"), new User(2, "Bob"));
        when(userRepository.findAll()).thenReturn(expectedUsers);
        // Act
        List<User> actualUsers = userService.findAllUsers();
        // Assert
        assertEquals(expectedUsers, actualUsers);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        // Act
        List<User> users = userService.findAllUsers();
        // Assert
        assertTrue(users.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindAllUsers_NullValueFromRepository() {
        // Arrange
        when(userRepository.findAll()).thenReturn(null);
        // Act & Assert
        assertThrows(NullPointerException.class, () -> userService.findAllUsers());
    }

    @BeforeEach
    public void setUp() {
        // Setup logic if needed before each test
    }
}
