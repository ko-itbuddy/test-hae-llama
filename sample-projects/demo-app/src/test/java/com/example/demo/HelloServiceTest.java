package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HelloService에 대한 단위 테스트 클래스입니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HelloService 명세")
class HelloServiceTest {

    @InjectMocks
    private HelloService helloService;

    @BeforeEach
    void setUp() {
        // 초기화 로직이 필요한 경우 여기에 작성합니다.
    }

    @Nested
    @DisplayName("Describe_greet")
    class Describe_greet {

        @Nested
        @DisplayName("Context_with_valid_name")
        class Context_with_valid_name {

            @Test
            @DisplayName("It returns greeting message")
            void it_returns_greeting_message() {
                // given
                String name = "World";
                // when
                String result = helloService.greet(name);
                // then
                assertThat(result).isEqualTo("Hello, World!");
            }
        }

        @Nested
        @DisplayName("Context_with_null_or_empty_name")
        class Context_with_null_or_empty_name {

            @Test
            @DisplayName("It throws IllegalArgumentException")
            void it_throws_IllegalArgumentException() {
                // given
                String nullName = null;
                String emptyName = "";
                String blankName = "   ";
                // when & then
                assertThatThrownBy(() -> helloService.greet(nullName)).isInstanceOf(IllegalArgumentException.class).hasMessage("Name cannot be empty");
                assertThatThrownBy(() -> helloService.greet(emptyName)).isInstanceOf(IllegalArgumentException.class).hasMessage("Name cannot be empty");
                assertThatThrownBy(() -> helloService.greet(blankName)).isInstanceOf(IllegalArgumentException.class).hasMessage("Name cannot be empty");
            }
        }

        @Nested
        @DisplayName("Context_with_long_name")
        class Context_with_long_name {

            @Test
            @DisplayName("It throws IllegalArgumentException")
            void it_throws_IllegalArgumentException() {
                // given
                String longName = "a".repeat(51);
                // when & then
                assertThatThrownBy(() -> helloService.greet(longName)).isInstanceOf(IllegalArgumentException.class).hasMessage("Name is too long");
            }
        }
    }

    @Nested
    @DisplayName("Describe_calculateAge")
    class Describe_calculateAge {

        @Nested
        @DisplayName("Context_with_valid_years")
        class Context_with_valid_years {

            @Test
            @DisplayName("It returns correct age")
            void it_returns_correct_age() {
                // given
                int birthYear = 2000;
                int currentYear = 2023;
                // when
                int age = helloService.calculateAge(birthYear, currentYear);
                // then
                assertThat(age).isEqualTo(23);
            }
        }

        @Nested
        @DisplayName("Context_with_future_birth_year")
        class Context_with_future_birth_year {

            @Test
            @DisplayName("It throws IllegalArgumentException")
            void it_throws_IllegalArgumentException() {
                // given
                int birthYear = 2024;
                int currentYear = 2023;
                // when & then
                assertThatThrownBy(() -> helloService.calculateAge(birthYear, currentYear)).isInstanceOf(IllegalArgumentException.class).hasMessage("Birth year cannot be in the future");
            }
        }
    }
}