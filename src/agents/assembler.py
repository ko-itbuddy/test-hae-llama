from .base import BaseAgent

class AssemblerAgent(BaseAgent):
    """조립창: 각 팀의 결과물을 가져와 완벽한 Java 코드로 조립합니다."""
    def __init__(self, llm):
        super().__init__(llm, role="Master Assembler")

    async def assemble_test_method(self, scenario_name, data_part, mock_part, exec_part, verify_part):
        prompt = f"""[SCENARIO] {scenario_name}
[DATA] {data_part}
[MOCK] {mock_part}
[EXEC] {exec_part}
[VERIFY] {verify_part}

[TASK] Assemble these into ONE JUnit 5 @Test method.
-section given: contains mocks.
-section when: contains execution.
-section then: contains assertions.
-Return ONLY valid Java code. No markdown. No backticks.
"""
        return await self._call_llm(prompt, "Master Assembler")
