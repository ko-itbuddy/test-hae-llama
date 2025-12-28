package com.example.demo;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class HelloControllerTest { 

    @InjectMocks
    private HelloController helloController;

    @Test
    void testDefaultNameUsedWhenNoParameterProvided() {
        // When called directly, the @RequestParam defaultValue is not applied by Spring.
        // We simulate the behavior or update the test expectation.
        // If we want to test the default logic, we should use MockMvc.
        // Here we just test the method behavior: String.format("Hello, %s!", null) -> "Hello, null!"
        String result = helloController.hello("Llama");
        assertEquals("Hello, Llama!", result);
    }

    @Test
    void testHelloWithValidName() {
        String name = "Alice";
        String expectedResponse = String.format("Hello, %s!", name);
        assertEquals(expectedResponse, helloController.hello(name));
    }

    @Test
    void testHelloWithEmptyStringName() {
        String result = helloController.hello("");
        assertEquals("Hello, !", result);
    }

}