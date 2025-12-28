# 🦙 Test-Hae-Llama: Engine Stress Test Scenarios

This document outlines the high-difficulty scenarios used to stress-test and improve the multi-agent generation engine.

## 🎯 Stage 1: Dependency Hell (NotificationService)
- **Goal**: Verify complex multi-mock orchestration and conditional logic branching.
- **Key Metrics**:
    - Are all 3 mocks correctly initialized and used?
    - Does it catch the "VIP Grade" branch correctly?
    - Is the `verify()` call accurate for side-effects?

## 🎯 Stage 2: Data Complexity (OrderService)
- **Goal**: Test parameterized generation capabilities and logic accuracy.
- **Key Metrics**:
    - Does it use `@CsvSource` or `@MethodSource` correctly?
    - Are edge cases (null IDs, zero quantity) covered?
    - Is the discount calculation logic accurately stubbed?

## 🎯 Stage 3: Domain Depth (PayrollService / Custom Lists)
- **Goal**: Verify advanced AssertJ chaining and object state validation.
- **Key Metrics**:
    - Does it use `.extracting()` and `.tuple()` for list results?
    - Is `BigDecimal` rounding handled without precision errors?
    - Does the Master Assembler combine parts without duplication?

---
## 🛠️ Improvement Log
*(To be filled during execution)*
- [ ] Bug 1: ...
- [ ] Enhancement 1: ...