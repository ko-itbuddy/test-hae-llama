package com.example.llama.domain.port.outbound;

/**
 * Outbound Port for security and data masking.
 */
public interface SecurityPort {
    
    /**
     * Masks sensitive data in code before sending to external services.
     */
    String maskSensitiveData(String sourceCode);
    
    /**
     * Restores masked data after receiving response.
     */
    String unmaskSensitiveData(String maskedCode);
    
    /**
     * Validates if code contains security risks.
     */
    SecurityCheckResult validateSecurity(String code);
    
    /**
     * Result of security validation.
     */
    record SecurityCheckResult(boolean passed, String[] issues) {}
}
