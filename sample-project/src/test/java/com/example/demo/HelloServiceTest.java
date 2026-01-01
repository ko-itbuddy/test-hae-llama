package com.example.demo;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

public class HelloServiceTest {

    @InjectMocks
    private HelloService helloService;

    @ParameterizedTest
    @CsvSource({ "2023, 2022", "2000, 1999" })
    void testCalculateAge_ThrowsExceptionWhenBirthYearGreaterThanCurrentYear(int birthYear, int currentYear) {
        // given
        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            helloService.calculateAge(birthYear, currentYear);
        });
        // then
        // Verify that the correct exception is thrown
        assertEquals("Birth year cannot be greater than the current year", exception.getMessage());
    }

    @BeforeEach
    public void setUp() {
        // Additional setup logic if needed
    }

    @ParameterizedTest
    @CsvSource({ "1990, 2023, 33", "1985, 2023, 38", "2000, 2023, 23" })
    public void testCalculateAge(int birthYear, int currentYear, int expectedAge) {
        // given
        // when
        int age = helloService.calculateAge(birthYear, currentYear);
        // then
        assertEquals(expectedAge, age);
    }

    @ParameterizedTest
    @CsvSource({ "2000, 2000", "1990, 1990", "2020, 2020" })
    public void testCalculateAge_whenBirthYearEqualsCurrentYear(int birthYear, int currentYear) {
        // given
        int expectedAge = 0;
        // when
        int actualAge = helloService.calculateAge(birthYear, currentYear);
        // then
        assertEquals(expectedAge, actualAge);
    }

    @Test
    public void testGreetWithValidName() {
        // given
        String name = "Alice";
        // when
        String result = helloService.greet(name);
        // then
        assertEquals("Hello, Alice!", result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "\t" })
    public void testGreetWithEmptyStringThrowsIllegalArgumentException(String name) {
        // given
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // when
            helloService.greet(name);
        });
        // then
        assertEquals("Name cannot be empty or blank", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t", "\n" })
    public void greet_withNullOrEmptyOrWhitespaceInput_returnsDefaultMessage(String name) {
        // given
        String expectedMessage = "Hello, Guest!";
        // when
        String result = helloService.greet(name);
        // then
        assertEquals(expectedMessage, result);
    }
}