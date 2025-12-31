package com.example.demo.repository;



package com.example.demo.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PaymentRepositoryTest {

    @Autowired()
    private PaymentRepository paymentRepository;

    @org.junit.jupiter.api.AfterEach()
    public void tearDown() {
        paymentRepository.deleteAll();
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    void setUp() {
        // Setup any necessary data for testing using TestEntityManager
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setName("TestPayment");
        payment.setAmount(new BigDecimal("100.00"));
        entityManager.persist(payment);
    }

    @Test
    void testFindByName_success() {
        // Given
        String name = "TestPayment";
        // When
        Payment foundPayment = paymentRepository.findByName(name);
        // Then
        assertNotNull(foundPayment);
        assertEquals(name, foundPayment.getName());
        assertEquals(new BigDecimal("100.00"), foundPayment.getAmount());
    }

    @Test
    void testSaveWithValidData() {
        // Arrange
        Long id = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        // Act
        paymentRepository.save(id, amount);
        // Assert
        Payment savedPayment = entityManager.find(Payment.class, id);
        assertNotNull(savedPayment);
        assertEquals(amount, savedPayment.getAmount());
    }

    @BeforeEach
    public void setUp() {
        // Example setup for testing custom query methods and JPQL correctness
        Payment payment1 = new Payment();
        payment1.setId(1L);
        payment1.setAmount(new BigDecimal("100.00"));
        entityManager.persist(payment1);
        Payment payment2 = new Payment();
        payment2.setId(2L);
        payment2.setAmount(new BigDecimal("200.00"));
        entityManager.persist(payment2);
        // Flush the changes to ensure they are available in the database
        entityManager.flush();
    }

    @Test
    public void testFindByName() {
        // Assuming there is a custom query method findByName in PaymentRepository
        String name = "TestPayment";
        Payment payment = new Payment();
        payment.setName(name);
        payment.setAmount(new BigDecimal("150.00"));
        entityManager.persist(payment);
        entityManager.flush();
        Optional<Payment> foundPayment = paymentRepository.findByName(name);
        assertTrue(foundPayment.isPresent());
        assertEquals(name, foundPayment.get().getName());
    }

    @Test
    public void testJPQLCorrectness() {
        // Assuming there is a custom JPQL query in PaymentRepository
        BigDecimal expectedTotal = new BigDecimal("300.00");
        Optional<BigDecimal> totalAmount = paymentRepository.getTotalAmount();
        assertTrue(totalAmount.isPresent());
        assertEquals(expectedTotal, totalAmount.get());
    }

    @Test
    public void testDatabaseConstraints() {
        // Test for invalid data
        Payment invalidPayment = new Payment();
        // Assuming amount cannot be negative
        invalidPayment.setAmount(new BigDecimal("-100.00"));
        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.save(invalidPayment);
        });
    }

    @Test
    void testFindByName_success() {
        // Given
        String paymentName = "Test Payment";
        BigDecimal amount = new BigDecimal("100.00");
        Payment payment = new Payment(paymentName, amount);
        entityManager.persist(payment);
        // When
        Optional<Payment> result = paymentRepository.findByName(paymentName);
        // Then
        assertTrue(result.isPresent());
        assertEquals(paymentName, result.get().getName());
        assertEquals(amount, result.get().getAmount());
    }

    @Test
    void testSave_UpdatesExistingRecord() {
        // Given
        Long existingId = 1L;
        BigDecimal newAmount = new BigDecimal("200.00");
        Payment existingPayment = new Payment(existingId, new BigDecimal("100.00"));
        entityManager.persistAndFlush(existingPayment);
        // When
        paymentRepository.save(existingId, newAmount);
        // Then
        Optional<Payment> updatedPayment = paymentRepository.findById(existingId);
        assertTrue(updatedPayment.isPresent());
        assertEquals(newAmount, updatedPayment.get().getAmount());
    }

    @BeforeEach
    public void setUp() {
        // Example setup using TestEntityManager
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setName("TestPayment");
        entityManager.persist(payment);
        entityManager.flush();
    }

    @Test
    public void testFindByName() {
        // Given
        String name = "TestPayment";
        // When
        Payment result = paymentRepository.findByName(name);
        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
    }

    @Test
    void testFindByName_SuccessScenario3() {
        // Given
        String name = "TestPayment";
        BigDecimal amount = new BigDecimal("100.00");
        // Assuming there is a method to create and persist an entity
        Payment payment = new Payment();
        payment.setName(name);
        payment.setAmount(amount);
        testEntityManager.persist(payment);
        // When
        Optional<Payment> result = paymentRepository.findByName(name);
        // Then
        assertTrue(result.isPresent());
        assertEquals(name, result.get().getName());
        assertEquals(amount, result.get().getAmount());
    }

    @Test
    void testSaveWithNullAmount() {
        Long paymentId = 1L;
        BigDecimal nullAmount = null;
        // Expecting a DataIntegrityViolationException due to not-null constraint on amount
        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.save(paymentId, nullAmount);
        });
    }

    @BeforeEach
    void setUp() {
        // Example setup using TestEntityManager to insert a test record
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        entityManager.persist(payment);
        // Ensure the data is flushed to the database
        entityManager.flush();
    }

    @Test
    void testFindByName() {
        // Assuming there's a method findByName in PaymentRepository
        String name = "TestPayment";
        Payment payment = new Payment();
        payment.setName(name);
        payment.setAmount(new BigDecimal("200.00"));
        entityManager.persist(payment);
        entityManager.flush();
        Optional<Payment> result = paymentRepository.findByName(name);
        assertTrue(result.isPresent());
        assertEquals(name, result.get().getName());
    }

    @Test
    void testJPQLCorrectness() {
        // Assuming there's a JPQL query in PaymentRepository
        String jpqlQuery = "SELECT p FROM Payment p WHERE p.amount > :amount";
        BigDecimal amountThreshold = new BigDecimal("50.00");
        List<Payment> result = paymentRepository.findByCustomQuery(amountThreshold);
        assertTrue(result.stream().allMatch(p -> p.getAmount().compareTo(amountThreshold) > 0));
    }

    @Test
    void testDatabaseConstraints() {
        // Assuming there's a constraint on the amount field (e.g., not null)
        Payment payment = new Payment();
        payment.setId(2L);
        // Intentionally setting amount to null to violate the constraint
        payment.setAmount(null);
        assertThrows(DataIntegrityViolationException.class, () -> {
            entityManager.persist(payment);
            entityManager.flush();
        });
    }

    @BeforeEach
    public void setUp() {
        // Setup logic for custom query methods and JPQL correctness can be added here
        // Example: Preparing data in the database using testEntityManager
    }
}
