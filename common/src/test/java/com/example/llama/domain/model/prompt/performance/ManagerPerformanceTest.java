package com.example.llama.domain.model.prompt.performance;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ManagerPerformanceTest extends PromptTestSupport {

    @Autowired
    private AgentFactory agentFactory;

    @Test
    @DisplayName("DATA_MANAGER: Should review code via Unified XML Protocol")
    void testDataManager() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.DATA_MANAGER, Intelligence.ComponentType.SERVICE);

        // Case 1: Perfect Code (Satisfies all Artisan Policies)
        String task = "Audit this test for 100% compliance with Artisan BDD standards.";
        String context = """
                @ExtendWith(MockitoExtension.class)
                @DisplayName("결제 서비스 테스트")
                class PaymentServiceTest {

                    @Mock
                    private PaymentRepository paymentRepository;

                    @InjectMocks
                    private PaymentService paymentService;

                    @Nested
                    @DisplayName("Describe_processPayment")
                    class Describe_processPayment {

                        @ParameterizedTest
                        @CsvSource({
                            "1000, SUCCESS, 결제 성공",
                            "0, INVALID_AMOUNT, 유효하지 않은 금액"
                        })
                        @DisplayName("결제 요청 시나리오 검증")
                        void testProcessPayment(int amount, String expectedStatus, String desc) {
                            // given
                            PaymentRequest request = new PaymentRequest(amount);
                            if ("SUCCESS".equals(expectedStatus)) {
                                BDDMockito.given(paymentRepository.save(any())).willReturn(new Payment(amount, "SUCCESS"));
                            }

                            // when
                            PaymentResponse response = paymentService.processPayment(request);

                            // then
                            assertThat(response.getStatus()).isEqualTo(expectedStatus);
                        }
                    }
                }
                """;
        String response1 = actAndLog(agent, task, context);

        // Assert: Response must follow the unified schema
        assertThat(response1).contains("<response>");
        assertThat(response1).contains("<status>APPROVED</status>");

        // Case 2: Bad Code (Incomplete statement)
        String contextBad = "@Test void test() { int a = ";
        String response2 = actAndLog(agent, task, contextBad);
        assertThat(response2).contains("<response>");
        // The gatekeeper should reject incomplete code
        assertThat(response2).contains("<status>REJECTED</status>");
    }
}