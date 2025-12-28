# 📉 Project Improvement & Deficiencies Report

During the test generation and verification process according to `TEST_GENERATION_SCENARIO.md`, several architectural and testability deficiencies were identified.

## 1. Testability Issues

### A. Missing Accessors for Verification
- **Issue:** The `Product` domain class used Lombok's `@Getter` but `stockQuantity` was added later manually without explicit checks. While Lombok usually handles this, in some test environments (or if the IDE/engine isn't lombok-aware during generation), explicit getters might be assumed.
- **Impact:** Generated tests referencing `product.getStockQuantity()` might fail if Lombok annotation processing isn't fully recognized by the static analysis tool or if the field is private without a getter.
- **Fix Applied:** Manually added `getStockQuantity()` to `Product.java`.

### B. Event Listener Unit Testing
- **Issue:** `OrderEventListener` and `UserEventListener` use `@TransactionalEventListener`.
- **Deficiency:** Unit testing these classes (`@InjectMocks` style) does **not** verify the transactional phase behavior (i.e., that it *waits* for commit). It only tests the logic inside the method.
- **Improvement:** Need **Integration Tests** (`@SpringBootTest`) to verify the event wiring and transactional boundaries. Unit tests are insufficient for EDD architecture.

### C. Async Mocking Complexity
- **Issue:** `ProductService` depends on `AiService` which returns `CompletableFuture`.
- **Deficiency:** Mocking `CompletableFuture` requires boilerplate (`CompletableFuture.completedFuture(...)`). If the service logic didn't return a Future but just fired an async void task, it would be extremely hard to verify completion or exception handling in a unit test.
- **Improvement:** Encourage returning `CompletableFuture` or `Future` from async methods (as done in `AiService`) to improve testability, rather than `void`.

## 2. Architectural Deficiencies

### A. Tight Coupling of Notifications
- **Issue:** `NotificationService` has logic for "VIP" vs "General" hardcoded.
- **Improvement:** Use the **Strategy Pattern**. Create a `NotificationStrategy` interface with implementations `VipNotificationStrategy` and `GeneralNotificationStrategy`. This would make testing easier and the code more Open-Closed Principle (OCP) compliant.

### B. Hardcoded Business Logic (Discount)
- **Issue:** `OrderService` has hardcoded "FIXED_1000" and "PERCENT_10" logic.
- **Improvement:** Move discount logic to a **Policy** or **Strategy** pattern (e.g., `DiscountPolicy`). This allows adding new coupon types without modifying the core `OrderService` (OCP).

### C. Anemic Domain Model vs Rich Domain Model
- **Issue:** `OrderService` handles calculation logic (`basePrice`, `discount`, `finalPrice`).
- **Improvement:** Move this calculation logic into the domain objects (e.g., `Order` object or `Product` object methods) to promote a **Rich Domain Model**. `OrderService` should only orchestrate the process.

## 3. Recommended Actions for "Engine"
1.  **Generate Integration Tests:** The engine should effectively distinguish when to use Unit Tests vs Integration Tests. For Listeners and Repositories (QueryDSL), integration tests are mandatory.
2.  **Lombok Awareness:** Ensure the engine parses Lombok annotations correctly to avoid generating getters/setters that already exist (or assuming them when they don't).
3.  **Pattern Refactoring:** The engine should suggest refactoring hardcoded `if-else` business logic into Strategies before generating tests, or at least flag it as "Low Maintainability".
