from .base import BaseAgent

class AssemblerAgent(BaseAgent):
    """조립창: 각 팀의 결과물을 가져와 완벽한 Java 코드로 조립합니다."""
    def __init__(self, llm):
        super().__init__(llm, role="Master Assembler")

    async def assemble_test_method(self, scenario_name, data_part, mock_part, exec_part, verify_part):
        prompt = f"""[COMPONENTS]
Scenario: {scenario_name}
Mocks: {mock_part}
Exec: {exec_part}
Verify: {verify_part}

[TASK]
STITCH these components together into a single @Test method.
STRICT RULES:
1. Use ONLY the code provided in [COMPONENTS].
2. DO NOT invent new classes or logic.
3. Organize using // given, // when, // then sections.
4. Output ONLY the method code. No class wrapper.
"""
        return await self._call_llm(prompt, "Mechanical Assembler")
