from .base import BaseAgent

class AssemblerAgent(BaseAgent):
    """조립창: 각 팀의 결과물을 가져와 완벽한 Java 코드로 조립합니다."""
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Master Assembler", target_file=target_file)

    async def assemble_test_method(self, scenario_name, data_part, mock_part, exec_part, verify_part):
        prompt = f"""[COMPONENTS]
Scenario: {scenario_name}
Data Row: {data_part}
Mocks: {mock_part}
Execution: {exec_part}
Verification: {verify_part}

[TASK]
You are a purely mechanical code stitcher.
STITCH the components above into a standard JUnit 5 @Test method.

[STRICT RULES]
1. DO NOT invent classes, instances, or new logic.
2. Use ONLY the code provided in [COMPONENTS].
3. If a component says 'Bureaucracy Failure', comment it out.
4. If @CsvSource is provided, write the method signature accordingly.
5. NO markdown. NO explanation. NO class wrapper.

[OUTPUT FORMAT]
@Test
void testName() {{
    // given
    ...
    // when
    ...
    // then
    ...
}}
"""
        return await self._call_llm(prompt, "Mechanical Stitching Tool")
