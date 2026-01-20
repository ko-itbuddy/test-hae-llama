package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import com.example.demo.client.BankClient;
import com.example.demo.repository.PaymentRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PayrollService {
    private final BankClient bankClient;
    private final PaymentRepository paymentRepository;

    public PayrollService(BankClient bankClient, PaymentRepository paymentRepository) {
        this.bankClient = bankClient;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public boolean processPayroll(Long employeeId, BigDecimal baseSalary) {
        // 1. 유효성 검사 (Edge Cases)
        if (employeeId == null || baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid input data");
        }

        // 2. 세금 계산 (10% 정액)
        BigDecimal tax = baseSalary.multiply(new BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal netSalary = baseSalary.subtract(tax);

        // 3. 외부 은행 송금 호출 (Mocking 대상)
        boolean transferSuccess = bankClient.transfer(employeeId, netSalary);

        if (transferSuccess) {
            // 4. 결과 저장
            paymentRepository.save(employeeId, netSalary);
            return true;
        }
        
        return false;
    }
}
