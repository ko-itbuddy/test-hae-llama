package com.example.demo.client.impl;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import com.example.demo.client.SmsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SmsClientImplTest {

    private SmsClientImpl smsClient;

    @BeforeEach
    public void setUp() {
        smsClient = new SmsClientImpl();
    }

    @ParameterizedTest
    @CsvSource({ "1234567890, Hello World", "9876543210, Test Message" })
    public void testSendSms_Success(String phone, String msg) {
        // given
        boolean expectedResult = true;
        // when
        boolean result = smsClient.sendSms(phone, msg);
        // then
        assertTrue(result == expectedResult);
    }

    @ParameterizedTest
    @CsvSource({ "null, 'Hello'", "'1234567890', null", "null, null", "'', 'Hello'", "'1234567890', ''" })
    public void testSendSmsFailure(String phone, String msg) {
        // given
        // when
        boolean result = smsClient.sendSms(phone, msg);
        // then
        assertFalse(result);
    }

    @ParameterizedTest
    @CsvSource({ 
        "1234567890, Hello", // Valid phone number
        "abc123456789, Hello", // Invalid characters
        "12345678901234567890, Hello", // Exceeding maximum length
        "123, Hello" }) // Below minimum length
    void testSendSmsFailure_InvalidPhoneNumberFormat(String phone, String msg) {
        // given
        boolean expected = false;
        // when
        boolean result = smsClient.sendSms(phone, msg);
        // then
        assertEquals(expected, result);
    }
}