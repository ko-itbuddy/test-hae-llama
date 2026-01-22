package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import static org.mockito.BDDMockito.given;

/**
 * {@link HelloService}의 비즈니스 로직을 검증하기 위한 단위 테스트 클래스입니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HelloService 테스트")
class HelloServiceTest {

    @InjectMocks
    private HelloService helloService;

    @BeforeEach
    void setUp() {
        // 각 테스트 케이스 실행 전 필요한 공통 설정을 수행합니다.
    }


@Nested
@DisplayName("greet 메서드는")
class Describe_greet {

    @Test
    @DisplayName("유효한 이름이 주어지면 인사말을 반환한다")
    void it_returns_greeting_when_valid_name_is_provided() {
        // given
        String name = "World";
        // when
        String result = helloService.greet(name);
        // then
        assertThat(result).isEqualTo("Hello, World!");
    }

    @ParameterizedTest
    @NullSource
    @CsvSource({ "''", "' '", "'   '" })
    @DisplayName("이름이 null이거나 비어있거나 공백이면 IllegalArgumentException을 던진다")
    void it_throws_exception_when_name_is_empty_or_null(String invalidName) {
        // given & when & then
        assertThatThrownBy(() -> helloService.greet(invalidName)).isInstanceOf(IllegalArgumentException.class).hasMessage("Name cannot be empty");
    }

    @Test
    @DisplayName("이름이 50자를 초과하면 IllegalArgumentException을 던진다")
    void it_throws_exception_when_name_is_too_long() {
        // given
        String longName = "a".repeat(51);
        // when & then
        assertThatThrownBy(() -> helloService.greet(longName)).isInstanceOf(IllegalArgumentException.class).hasMessage("Name is too long");
    }
}

}