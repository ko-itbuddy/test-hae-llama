package com.example.demo.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.params.provider.CsvSource.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.ValueSource;
import javax.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    private User user;

    @ParameterizedTest
    @CsvSource({ "John Doe, john.doe@example.com", "Jane Smith, jane.smith@example.com" })
    void testGetEmail(String name, String email) {
        // given
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        // when
        String result = user.getEmail();
        // then
        assertThat(result).isEqualTo(email);
    }

    @BeforeEach
    public void setUp() {
        user = new User();
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", null })
    public void testGetEmailWithInvalidValues(String email) {
        // given
        user.setEmail(email);
        // when
        String result = user.getEmail();
        // then
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({ "valid.email@example.com, valid.email@example.com", "another.valid-email@domain.org, another.valid-email@domain.org" })
    public void testGetEmailWithValidValues(String inputEmail, String expectedEmail) {
        // given
        user.setEmail(inputEmail);
        // when
        String result = user.getEmail();
        // then
        assertThat(result).isEqualTo(expectedEmail);
    }

    @Test
    public void testGetEmailWhenNotSet() {
        // given
        // email is not set
        // when
        String result = user.getEmail();
        // then
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({ "''", "\" \"", "\"  \"" })
    void testSetEmptyEmail(String emailValue) {
        // given
        User user = new User();
        // when & then
        assertThrows(ConstraintViolationException.class, () -> {
            user.setEmail(emailValue);
        });
    }

    @ParameterizedTest
    @CsvSource({ "1234567890", "+1-800-555-0199", "9876543210" })
    void testGetPhoneNumber(String phoneNumber) {
        // given
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        // when
        String result = user.getPhoneNumber();
        // then
        assertThat(result).isEqualTo(phoneNumber);
    }

    @ParameterizedTest
    @CsvSource({ "null" })
    void testGetPhoneNumber_returnsNull_whenPhoneNumberIsSetToNull(String phoneNumber) {
        // given
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        // when
        String result = user.getPhoneNumber();
        // then
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @CsvSource({ "   ", "\t", "\n" })
    void testGetPhoneNumber_returnsNullForWhitespace(String phoneNumber) {
        // given
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        // when
        String result = user.getPhoneNumber();
        // then
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testGetGrade_whenGradeIsNull(String grade) {
        // given
        User user = new User();
        user.setGrade(grade);
        // when
        String result = user.getGrade();
        // then
        assertThat(result).isNull();
    }

    @ParameterizedTest
    @CsvSource({ "A, A", "B, B", "C, C" })
    void testGetGrade_whenGradeIsNotNull(String inputGrade, String expectedGrade) {
        // given
        User user = new User();
        user.setGrade(inputGrade);
        // when
        String result = user.getGrade();
        // then
        assertThat(result).isEqualTo(expectedGrade);
    }

    @Test
    public void testGetGrade_whenGradeIsEmptyString() {
        // given
        User user = new User();
        user.setGrade("");
        // when
        String grade = user.getGrade();
        // then
        assertThat(grade).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({ "A", "B+", "C" })
    void testGetGrade(String grade) {
        // given
        User user = new User();
        user.setGrade(grade);
        // when
        String result = user.getGrade();
        // then
        assertThat(result).isEqualTo(grade);
    }
}