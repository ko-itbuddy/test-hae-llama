# Project Overview

The project `sample-project` (Demo) is a Spring Boot application designed to demonstrate a typical Service-Repository architecture.

## Structure
- Standard Maven/Gradle directory layout:
  - `src/main/java`: Source code.
  - `src/test/java`: Test code.
  - `src/main/resources`: Configuration files (`application.yml` etc.).

## Key Components
- **Domain/Model**: JPA Entities (e.g., `User`, `Product`) using Lombok.
- **Repository**: Spring Data JPA repositories.
- **Service**: Business logic.
- **Controller**: REST endpoints (e.g., `HelloController`).
- **Clients**: External service simulations (e.g., `SmsClient`, `BankClient`).

## Entry Point
- `com.example.demo.DemoApplication` annotated with `@SpringBootApplication`.
