package com.example.demo.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
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
import org.junit.jupiter.params.provider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserTest {

    @ParameterizedTest
    @CsvSource({ "valid.email@example.com, valid.email@example.com", "another.valid-email@domain.co.uk, another.valid-email@domain.co.uk" })
    void testGetEmail_withValidEmail(String inputEmail, String expectedEmail) {
        // given
        User user = new User();
        user.setEmail(inputEmail);
        // when
        String result = user.getEmail();
        // then
        assertThat(result).isEqualTo(expectedEmail);
    }

    @ParameterizedTest
    @CsvSource({ ", null", "invalid-email.com, invalid-email.com", "special!char@domain.com, special!char@domain.com" })
    void testGetEmail_withEdgeCases(String inputEmail, String expectedEmail) {
        // given
        User user = new User();
        if (inputEmail != null) {
            user.setEmail(inputEmail);
        }
        // when
        String result = user.getEmail();
        // then
        assertThat(result).isEqualTo(expectedEmail);
    }

    @Test
    void testGetEmail_withUninitializedEmail() {
        // given
        User user = new User();
        // when & then
        assertThat(user.getEmail()).isNull();
    }

    @ParameterizedTest
    @CsvSource({ "1234567890, 1234567890", ", null", "null, null" })
    void testGetPhoneNumber(String phoneNumberInput, String expected) {
        // given
        User user = new User();
        if (phoneNumberInput != null) {
            user.setPhoneNumber(phoneNumberInput);
        }
        // when
        String result = user.getPhoneNumber();
        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({ "John Doe, john.doe@example.com, A", "Jane Smith, jane.smith@example.com, B" })
    void testGetGrade_withValidUserAndNonEmptyGrade(String name, String email, String grade) {
        // given
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setGrade(grade);
        // when
        String result = user.getGrade();
        // then
        assertThat(result).isEqualTo(grade);
    }

    @ParameterizedTest
    @CsvSource({ "John Doe, john.doe@example.com", "Jane Smith, jane.smith@example.com" })
    void testGetGrade_withValidUserAndEmptyGrade(String name, String email) {
        // given
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        // when
        String result = user.getGrade();
        // then
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({ "John Doe, john.doe@example.com", "Jane Smith, jane.smith@example.com" })
    void testGetGrade_withValidUserAndNullGrade(String name, String email) {
        // given
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setGrade(null);
        // when
        String result = user.getGrade();
        // then
        assertThat(result).isEmpty();
    }
}
