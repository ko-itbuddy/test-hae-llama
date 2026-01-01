package com.example.demo.client;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateClientTest {

    @InjectMocks
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private ExternalExchangeService externalExchangeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Assuming getExchangeRate is a method on ExternalExchangeService that returns a double
    @Test
    public void testGetExchangeRate_SuccessfulRetrieval() {
        double expectedRate = 1.23;
        when(externalExchangeService.getExchangeRate()).thenReturn(expectedRate);
        double actualRate = exchangeRateClient.getExchangeRate();
        assertEquals(expectedRate, actualRate);
    }

    @Test
    public void testGetExchangeRate_ReturnsNull() {
        // given: No specific setup needed as getExchangeRate does not depend on any external services or fields
        // when: Calling the method that might return null
        Double result = exchangeRateClient.getExchangeRate();
        // then: Verify that the result is null
        assertNull(result, "Expected getExchangeRate() to return null");
    }

    @ParameterizedTest
    @CsvSource({ "Double.MIN_VALUE, Double.MIN_VALUE", "Double.MAX_VALUE, Double.MAX_VALUE", "0.0, 0.0", "-1.0, -1.0" })
    public void testGetExchangeRate(double input, double expected) {
        // given
        when(externalExchangeService.getExchangeRate()).thenReturn(input);
        // when
        double result = exchangeRateClient.getExchangeRate();
        // then
        assertEquals(expected, result);
    }
}