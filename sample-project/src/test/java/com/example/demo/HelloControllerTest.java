package com.example.demo;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HelloControllerTest {

    @Mock
    private SomeDependency someDependency; // Assuming there's a dependency to mock

    @InjectMocks
    private HelloController helloController;

    @Test
    public void testHello() {
        // given
        when(someDependency.someMethod("Llama")).thenReturn("Hello, Llama!");

        // when
        String result = helloController.hello("Llama");

        // then
        assertThat(result).isEqualTo("Hello, Llama!");
    }

    @Test
    public void testHelloCustomName() {
        // given
        when(someDependency.someMethod("CustomName")).thenReturn("Hello, CustomName!");

        // when
        String result = helloController.hello("CustomName");

        // then
        assertThat(result).isEqualTo("Hello, CustomName!");
    }

    @Test
    public void testHelloWithInvalidName() {
        // given
        doThrow(new InvalidParameterException()).when(someDependency).someMethod("InvalidName");

        // when
        Throwable thrown = catchThrowable(() -> helloController.hello("InvalidName"));

        // then
        assertThat(thrown).isInstanceOf(InvalidParameterException.class);
    }
}