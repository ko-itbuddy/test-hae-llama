package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("HelloService 로직 검증 테스트")
public class HelloServiceTest {

    @InjectMocks
    private HelloService helloService;

    @Nested
    @DisplayName("greet 메서드는")
    class Describe_greet {

        @ParameterizedTest
        @CsvSource(value = {
            "'Alice', 'Hello, Alice!'",
            "' Bob ', 'Hello,  Bob !'",
            "'Charlie', 'Hello, Charlie!'"
        })
        @DisplayName("유효한 이름이 들어오면 환영 인사를 반환한다")
        void it_returns_greeting_message(String name, String expected) {
            // given
            // when
            String result = helloService.greet(name);

            // then
            assertEquals(expected, result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("이름이 비어있거나 공백이면 IllegalArgumentException을 던진다")
        void it_throws_exception_when_name_is_empty(String name) {
            // given
            // when & then
            assertThrows(IllegalArgumentException.class, () -> helloService.greet(name));
        }

        @Test
        @DisplayName("이름이 50자를 초과하면 IllegalArgumentException을 던진다")
        void it_throws_exception_when_name_is_too_long() {
            // given
            String longName = "a".repeat(51);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> helloService.greet(longName));
        }
    }

    @Nested
    @DisplayName("calculateAge 메서드는")
    class Describe_calculateAge {

        @ParameterizedTest
        @CsvSource({
            "1990, 2023, 33",
            "2000, 2023, 23",
            "2023, 2023, 0"
        })
        @DisplayName("출생연도와 현재연도를 기반으로 나이를 계산한다")
        void it_calculates_age_correctly(int birthYear, int currentYear, int expected) {
            // given
            // when
            int result = helloService.calculateAge(birthYear, currentYear);

            // then
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("출생연도가 미래라면 IllegalArgumentException을 던진다")
        void it_throws_exception_when_birthyear_is_future() {
            // given
            int birthYear = 2025;
            int currentYear = 2023;

            // when & then
            assertThrows(IllegalArgumentException.class, () -> helloService.calculateAge(birthYear, currentYear));
        }
    }
}