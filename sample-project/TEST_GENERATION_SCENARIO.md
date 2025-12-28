# 🧪 Test Generation Verification Scenario
**Target Project:** `sample-project` (High-Complexity Spring Boot App)
**Goal:** Verify that the LLM Engine generates compilable, meaningful, and passing tests for all architectural patterns.

---

## 1. 🏗️ Infrastructure Analysis (Ingestion Phase)
The engine must correctly identify:
- **Build System:** Gradle 8.5 (Wrapper), Spring Boot 3.3.0.
- **Key Libraries:** 
  - `QueryDSL 5.1.0` (needs generated Q-Classes).
  - `Lombok`.
  - `Spring Event Publisher`.
  - `Spring Async`.
- **Test Frameworks:** JUnit 5, Mockito, AssertJ.

---

## 2. 🎯 Class-Specific Verification Points

The engine should generate `*Test.java` files covering the following specific scenarios.

### A. Core Business Logic & Concurrency
**Target:** `com.example.demo.service.OrderService`
- [ ] **Dependencies:** Mocks for `ProductRepository`, `UserRepository`, `ApplicationEventPublisher`.
- [ ] **Scenario 1 (Success):** `placeOrder` successfully calculates `finalPrice` (scale 2), saves user/product implicitly, and **publishes** `OrderPlacedEvent`.
    - *Verify:* `verify(eventPublisher).publishEvent(any(OrderPlacedEvent.class))`
- [ ] **Scenario 2 (Concurrency):** Simulate `ObjectOptimisticLockingFailureException` from repository.
    - *Expect:* Service catches exception and rethrows `RuntimeException` (Fail-fast).
- [ ] **Scenario 3 (Edge Case):** Coupon Logic (`FIXED_1000`, `PERCENT_10`).

### B. Transactional & Event Integrity
**Target:** `com.example.demo.service.UserService`
- [ ] **Dependencies:** Mocks for `UserRepository`, `ApplicationEventPublisher`.
- [ ] **Scenario 1 (Transaction):** Verify `createUser` is executed. (Difficult to test `@Transactional` with unit tests, but ensure no compile errors with imports).
- [ ] **Scenario 2 (Event):** Verify `UserCreatedEvent` is published after save.
- [ ] **Scenario 3 (Exception):** `existsByEmail` returns true -> throws `IllegalArgumentException`.

### C. QueryDSL & Async Integration
**Target:** `com.example.demo.service.ProductService`
- [ ] **Dependencies:** Mocks for `ProductRepository` (must mock the interface that extends Custom), `AiService`, `ExchangeRateClient`.
- [ ] **Scenario 1 (Async Mocking):** Call `getDiscountedPriceInUsd`.
    - *Challenge:* `AiService.analyzeProductTrend` returns `CompletableFuture`.
    - *Expect:* `given(aiService.analyzeProductTrend(any())).willReturn(CompletableFuture.completedFuture(...))`.
- [ ] **Scenario 2 (QueryDSL Wrapper):** Call `getExpensiveProducts`.
    - *Expect:* `verify(productRepository).findProductsExpensiveThan(any())`.

### D. Async Event Listeners
**Target:** `com.example.demo.listener.OrderEventListener`
- [ ] **Challenge:** Testing `@TransactionalEventListener` and `@Async` in unit tests is tricky.
- [ ] **Expectation:** Simple unit test verifying that *if* the method is called, it triggers the `NotificationService`.
    - *Note:* Integration tests are better here, but if unit test generated, it should just check logic delegation.

### E. Legacy & Strategy Patterns
**Target:** `com.example.demo.service.NotificationService`
- [ ] **Scenario:** User Grade 'VIP' -> Calls both `SmsClient` and `EmailClient`.
- [ ] **Scenario:** User Grade 'General' -> Calls only `EmailClient`.

---

## 3. 🚀 Execution & Success Metrics

### Step 1: Generation
Run the engine against `sample-project`:
```bash
# Example Command
python src/main.py generate-all --project-path ./sample-project
```

### Step 2: Validation
1.  **Compilation:**
    ```bash
    ./gradlew clean testClasses
    ```
    - *Must Pass:* No missing symbols (e.g., `UserCreatedEvent`, `QProduct`).
2.  **Execution:**
    ```bash
    ./gradlew test
    ```
    - *Must Pass:* All generated tests are Green.
3.  **Logical Check:**
    - Are assertions meaningful? (e.g., `assertThat(id).isNotNull()` vs `assertThat(true).isTrue()`).
    - Are Mocks correctly reset or isolated?

---

## 4. 🚩 Red Flags (Fail Conditions)
- ❌ **Hallucinated Methods:** Calling methods that don't exist in `ProductRepository` (e.g., `findByName` when not defined).
- ❌ **Async Blockage:** Tests hanging because `CompletableFuture` wasn't mocked properly.
- ❌ **Event Type Mismatch:** Capturing `Object` instead of `OrderPlacedEvent`.
- ❌ **QueryDSL Compilation Error:** Trying to instantiate `QProduct` directly in a Unit Test without proper context (should verify repository method call instead).
