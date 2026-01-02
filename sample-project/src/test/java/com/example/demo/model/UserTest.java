package com.example.demo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("User 엔티티 테스트")
public class UserTest {

    @Nested
    @DisplayName("Email 필드는")
    class Describe_email {

        @ParameterizedTest
        @CsvSource(value = {
            "test@example.com, test@example.com",
            "null, null"
        }, nullValues = "null")
        @DisplayName("이메일 값을 정확히 저장하고 반환한다")
        void it_handles_email_cases(String inputEmail, String expectedEmail) {
            // given
            User user = User.builder().email(inputEmail).build();

            // when
            String result = user.getEmail();

            // then
            assertThat(result).isEqualTo(expectedEmail);
        }
    }

    @Nested
    @DisplayName("Grade 필드는")
    class Describe_grade {

        @ParameterizedTest
        @CsvSource(value = {
            "VIP, VIP",
            "null, null"
        }, nullValues = "null")
        @DisplayName("등급 정보를 정확히 관리한다")
        void it_handles_grade_cases(String inputGrade, String expectedGrade) {
            // given
            User user = User.builder().grade(inputGrade).build();

            // when
            String result = user.getGrade();

            // then
            assertThat(result).isEqualTo(expectedGrade);
        }
    }
}