package com.example.demo.client.impl;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailClientImplTest {

    private EmailClientImpl emailClientImpl;

    @BeforeEach
    public void setUp() {
        emailClientImpl = new EmailClientImpl();
    }

    @Test
    public void testSendEmail_validInput_returnsTrue() {
        boolean result = emailClientImpl.sendEmail("test@example.com", "Subject", "Body");
        assertTrue(result);
    }

    @Test
    public void testSendEmail_nullEmail_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailClientImpl.sendEmail(null, "Subject", "Body");
        });
        assertThat(exception.getMessage()).isEqualTo("Email cannot be null");
    }

    @Test
    public void testSendEmail_emptyEmail_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailClientImpl.sendEmail("", "Subject", "Body");
        });
        assertThat(exception.getMessage()).isEqualTo("Email cannot be empty");
    }

    @Test
    public void testSendEmail_nullTitle_returnsTrue() {
        boolean result = emailClientImpl.sendEmail("test@example.com", null, "Body");
        assertTrue(result);
    }

    @Test
    public void testSendEmail_emptyTitle_returnsTrue() {
        boolean result = emailClientImpl.sendEmail("test@example.com", "", "Body");
        assertTrue(result);
    }

    @Test
    public void testSendEmail_nullBody_returnsTrue() {
        boolean result = emailClientImpl.sendEmail("test@example.com", "Subject", null);
        assertTrue(result);
    }

    @Test
    public void testSendEmail_emptyBody_returnsTrue() {
        boolean result = emailClientImpl.sendEmail("test@example.com", "Subject", "");
        assertTrue(result);
    }
}