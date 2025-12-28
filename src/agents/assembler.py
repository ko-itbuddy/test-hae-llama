from .base import BaseAgent

class AssemblerAgent(BaseAgent):
    """조립창: 각 팀의 결과물을 가져와 완벽한 Java 코드로 조립합니다."""
    def __init__(self, llm):
        super().__init__(llm, role="Master Assembler")

    async def assemble_test_method(self, scenario_name, data_part, mock_part, exec_part, verify_part):
        prompt = f"""[SCENARIO] {scenario_name}
[PARTS]
Mock: {mock_part}
Exec: {exec_part}
Verify: {verify_part}

[TASK]
Assemble these into ONE Java JUnit 5 @Test method.
STRICT RULES:
1. Output ONLY the method code.
2. DO NOT include class declaration, imports, or markdown.
3. Start with @Test.
4. Ensure sections are separated by // given, // when, // then comments.
"""
        return await self._call_llm(prompt, "Master Assembler")
