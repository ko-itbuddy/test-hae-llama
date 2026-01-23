package com.example.demo.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * 보안 민감 정보 및 기밀 로직이 포함된 테스트용 서비스
 */
@Slf4j
@Service
public class SecuritySensitiveService {

    // 가짜 API 키 (외부 노출 금지)
    private static final String EXTERNAL_API_KEY = "sk-live-592e9ab14d4970ebe7619e56d03274cd"; // SEC:VAL

    // 가짜 SSL 프라이빗 키
    private static final String SSL_PRIVATE_KEY = """
            -----BEGIN PRIVATE KEY-----
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDJ9f9Z...
            ... (생략) ...
            -----END PRIVATE KEY-----
            """; // SEC:VAL

    /**
     * 회사의 핵심 수익을 결정하는 기밀 알고리즘
     */
    public double calculateDynamicPricing(double basePrice, int demandLevel) {
        log.info("Calculating pricing for base: {}", basePrice);
        
        // SEC:BODY - START (이 아래 로직은 절대 LLM으로 전송되면 안 됨)
        double multiplier = 1.0;
        if (demandLevel > 10) {
            multiplier = 1.5 + (Math.random() * 0.5);
        } else if (demandLevel > 5) {
            multiplier = 1.2;
        }
        double finalPrice = basePrice * multiplier * 0.987654321; // 회사의 비밀 계수
        return finalPrice;
        // SEC:BODY - END
    }

    public String callExternalProvider(String data) {
        log.info("Calling provider with key: {}", EXTERNAL_API_KEY);
        return "Success response for " + data;
    }
}
