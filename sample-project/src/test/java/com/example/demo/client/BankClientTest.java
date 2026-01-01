package com.example.demo.client;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MockitoExtension.class)
public class BankClientTest {

    @InjectMocks
    private BankClient bankClient;

    @Test
    public void testTransferWithValidPositiveAmount() {
        // given
        Long clientId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        // when
        boolean result = bankClient.transfer(clientId, amount);
        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource({ "null, 100.0", "0, 100.0", "-1, 100.0", "1, null", "1, 0.0", "1, -100.0" })
    public void testTransfer_InvalidClientIdOrAmount(Long id, BigDecimal amount) {
        // given
        // when
        boolean result = bankClient.transfer(id, amount);
        // then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource({ "1000, 50.0, true", "2000, 300.0, false" })
    public void testTransfer(Long id, BigDecimal amount, boolean expected) {
        // given
        // when
        boolean result = bankClient.transfer(id, amount);
        // then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(bd = { null, BigDecimal.ZERO, new BigDecimal("-100") })
    public void testTransfer_invalidAmount(BigDecimal amount) {
        // given
        Long id = 1000L;
        // when
        boolean result = bankClient.transfer(id, amount);
        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testTransfer_minimumBigDecimalValue() {
        // given
        Long id = 1000L;
        BigDecimal minAmount = new BigDecimal("0.01");
        // when
        boolean result = bankClient.transfer(id, minAmount);
        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testTransfer_maximumBigDecimalValue() {
        // given
        Long id = 1000L;
        BigDecimal maxAmount = new BigDecimal("999999999999999999.99");
        // when
        boolean result = bankClient.transfer(id, maxAmount);
        // then
        assertThat(result).isFalse();
    }
}