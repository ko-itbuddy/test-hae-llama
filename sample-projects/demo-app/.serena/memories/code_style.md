# Code Style and Conventions

- **Language**: Java
- **Indentation**: 4 spaces.
- **Naming**:
  - Classes: PascalCase (e.g., `UserService`)
  - Methods/Variables: camelCase (e.g., `createUser`)
  - Constants: UPPER_SNAKE_CASE
- **Annotations**:
  - Extensive use of Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) for models.
  - Standard Spring annotations (`@Service`, `@Repository`, `@RestController`, `@Autowired`/Constructor Injection).
- **Architecture**:
  - Layered architecture: Controller -> Service -> Repository.
  - Interfaces used for Clients (`SmsClient`) and Repositories (`UserRepository`).
- **Testing**:
  - JUnit 5 (`@Test`, `@ExtendWith(MockitoExtension.class)`).
  - Mockito for mocking dependencies.
  - AssertJ for fluent assertions.
