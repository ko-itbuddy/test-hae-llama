package com.example.demo;



package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class HelloServiceTest {

    @Mock
    private SomeDependency someDependency;

    @InjectMocks
    private HelloService helloService;

    @Mock
    private DependencyRepository // Replace with actual dependency
    dependencyRepository;

    @Test
    void testCalculateAge_NormalCalculation() {
        // Arrange
        int birthYear = 1990;
        int currentYear = 2023;
        int expectedAge = 33;
        // Act
        int actualAge = helloService.calculateAge(birthYear, currentYear);
        // Assert
        assertEquals(expectedAge, actualAge);
    }

    @Test
    void testCalculateAge_BirthYearInFuture() {
        // Arrange
        int birthYear = 2025;
        int currentYear = 2023;
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            helloService.calculateAge(birthYear, currentYear);
        });
    }

    @Test
    void testCalculateAge_BirthYearEqualToCurrentYear() {
        // Arrange
        int birthYear = 2023;
        int currentYear = 2023;
        // Act
        int age = helloService.calculateAge(birthYear, currentYear);
        // Assert
        assertEquals(0, age);
    }

    @Test
    void testGreet_Success() {
        // Arrange
        String name = "Alice";
        String expectedResponse = "Hello, Alice!";
        when(mockDependency.someMethod(name)).thenReturn(expectedResponse);
        // Act
        String result = helloService.greet(name);
        // Assert
        assertEquals(expectedResponse, result);
        verify(mockDependency, times(1)).someMethod(name);
    }

    @Test
    void testGreetWithValidName() {
        // Arrange
        String input = "Alice";
        String expectedOutput = "Hello, Alice!";
        when(mockRepository.findByName(input)).thenReturn(Optional.of(new User(input)));
        // Act
        String result = helloService.greet(input);
        // Assert
        assertEquals(expectedOutput, result);
        verify(mockRepository, times(1)).findByName(input);
    }

    @Test
    void testGreet() {
        // Arrange
        String name = "Alice";
        String expectedOutput = "Hello, Alice!";
        // Act
        String result = helloService.greet(name);
        // Assert
        assertEquals(expectedOutput, result);
        verify(dependencyRepository, never()).anyMethod();
    }

    @Test
    void testGreet() {
        // Arrange
        String name = "Alice";
        String expectedGreeting = "Hello, Alice!";
        // Act
        String result = helloService.greet(name);
        // Assert
        assertEquals(expectedGreeting, result);
        // Verify interactions if necessary
        // Example interaction verification
        verify(someDependency, times(1)).someMethod();
    }

    @Test
    void testGreetWithNullName() {
        // Arrange
        String name = null;
        String expectedResponse = "Hello, Guest!";
        // Act
        String result = helloService.greet(name);
        // Assert
        assertEquals(expectedResponse, result);
        verify(mockDependency, times(1)).someMethod();
    }

    @Test
    void testGreetWithNullInput() {
        // Arrange
        when(mockDependency.someMethod()).thenReturn("Mocked Response");
        // Act
        String result = helloService.greet(null);
        // Assert
        assertEquals("Expected Result for Null", result);
        verify(mockDependency, times(1)).someMethod();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGreet_WithNullName_ShouldThrowIllegalArgumentException() {
        // Arrange
        String name = null;
        // Act
        helloService.greet(name);
        // Assert
        // Exception is expected, no need to verify interactions
    }

    @Test
    void testGreetWithNullNameThrowsException() {
        // Arrange
        String name = null;
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            helloService.greet(name);
        });
    }

    @Test
    void testGreetWithEmptyName() {
        // Arrange
        String name = "";
        String expectedResponse = "Hello, Guest!";
        when(mockRepository.findByName(name)).thenReturn(null);
        // Act
        String result = helloService.greet(name);
        // Assert
        assertEquals(expectedResponse, result);
        verify(mockRepository, times(1)).findByName(name);
    }

    @Test
    void testGreetWithEmptyName() {
        // Arrange
        when(helloRepository.findByName("")).thenReturn(Optional.empty());
        // Act
        String result = helloService.greet("");
        // Assert
        assertEquals("Hello, Guest!", result);
        verify(helloRepository, times(1)).findByName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGreetWithNullName() {
        // Arrange
        String name = null;
        // Act
        helloService.greet(name);
        // Assert
        verify(helloRepository, never()).save(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGreetWithEmptyName() {
        // Arrange
        String name = "";
        // Act
        helloService.greet(name);
        // Assert (handled by expected exception)
    }

    @Test
    void testGreetWithEmptyName() {
        // Arrange
        String name = "";
        String expectedResponse = "Hello, Guest!";
        // Act
        String result = helloService.greet(name);
        // Assert
        assertEquals(expectedResponse, result);
        verify(userRepository, times(0)).findById(anyLong());
    }

    @Test
    void testGreetWithLongName() {
        // Arrange
        String longName = "A".repeat(1000);
        when(mockDependency.someMethod(longName)).thenReturn("Mocked Response");
        // Act
        String result = helloService.greet(longName);
        // Assert
        assertEquals("Expected Result", result);
        verify(mockDependency, times(1)).someMethod(longName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGreetWithNullName() {
        // Arrange
        String name = null;
        // Act
        helloService.greet(name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGreetWithLongName() {
        // Create a name with 1001 characters
        String longName = "a".repeat(1001);
        helloService.greet(longName);
    }

    @BeforeEach
    public void setUp() {
        // Initialization logic if needed
    }
}
