# Coding Conventions & Protocols

## 🚨 Serena-MCP Protocol (Mandatory)
All agents MUST adhere to these rules:
1. **LSP-First**: Use `find_symbol`, `get_symbols_overview` instead of grep.
2. **Structural Editing**: Use `replace_symbol_body`, `insert_after_symbol` instead of text replacement.
3. **Analyze-Plan-Act**: Always analyze (`codebase_investigator`) and plan (`architect`) before coding.

## Java Test Generation Style
- **Frameworks**: JUnit 5 (`@Test`, `@DisplayName`), Mockito (`@ExtendWith(MockitoExtension.class)`), AssertJ (`assertThat`).
- **Structure**:
    - `// given`: Setup mocks and data.
    - `// when`: Execute the method under test.
    - `// then`: Verify results and interactions.
- **Constraints**: No `public` modifier on test classes/methods (JUnit 5 default). Use `@InjectMocks` and `@Mock`.

## Python Code Style
- **Type Hints**: Encourage typing.
- **Async/Await**: The engine is async-heavy (`asyncio`).
- **Docstrings**: Clear explanation of agent roles.
