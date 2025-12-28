from .base import BaseAgent
import re

class ImplementerAgent(BaseAgent):
    def __init__(self, llm):
        super().__init__(llm, role="JUnit 5 Expert")

    async def implement_test_method(self, scenario_obj, target_method, context, mock_info, instance_name):
        clean_context = re.sub(r'//.*', '', context)
        clean_context = "\n".join([l.strip() for l in clean_context.split("\n") if l.strip()])
        
        test_type = scenario_obj.get("type", "TEST")
        
        if test_type == "PARAMETERIZED":
            return await self._implement_parameterized(scenario_obj, target_method, clean_context, mock_info, instance_name)
        else:
            return await self._implement_normal(scenario_obj, target_method, clean_context, mock_info, instance_name)

    async def _implement_normal(self, scenario_obj, target_method, context, mock_info, instance_name):
        target_name = target_method.split("(")[0].split(" ")[-1]
        
        prompt = "[TASK] Write a standard JUnit 5 @Test method.\n"
        prompt += "[SCENARIO] " + scenario_obj['description'] + "\n"
        prompt += "[TARGET] " + target_method + "\n"
        prompt += "[CONTEXT]\n" + context + "\n"
        prompt += "[MOCKS]\n" + mock_info + "\n"
        prompt += "[RULES]\n1. Use @Test.\n2. Use AssertJ 'assertThat'.\n3. Output ONLY the method code.\n"

        return await self._get_clean_code(prompt)

    async def _implement_parameterized(self, scenario_obj, target_method, context, mock_info, instance_name):
        csv_data = scenario_obj.get("csv_source", [])
        
        if not csv_data:
            data_prompt = "[TASK] Generate 3-5 rows of CSV test data for this scenario.\n"
            data_prompt += "[SCENARIO] " + scenario_obj['description'] + "\n"
            data_prompt += "[METHOD] " + target_method + "\n"
            data_prompt += "[OUTPUT FORMAT]\nComma-separated values, one set per line.\n[DATA ONLY]\n"
            
            data_response = await self._call_llm(data_prompt)
            csv_data = [line.strip() for line in data_response.split("\n") if line.strip() and "," in line]
        
        if not csv_data:
            csv_data = ["null", "empty"]

        # Safe construction of annotation
        csv_lines = '"",\n    "'.join(csv_data)
        annotation_block = "@ParameterizedTest\n    @CsvSource({\n        \"" + csv_lines + "\"\n    })"

        target_name = target_method.split("(")[0].split(" ")[-1]
        
        code_prompt = "[TASK] Write a JUnit 5 @ParameterizedTest method using the provided data.\n"
        code_prompt += "[SCENARIO] " + scenario_obj['description'] + "\n"
        code_prompt += "[TARGET] " + target_method + "\n"
        code_prompt += "[ANNOTATION]\n" + annotation_block + "\n"
        code_prompt += "[CONTEXT]\n" + context + "\n"
        code_prompt += "[MOCKS]\n" + mock_info + "\n"
        code_prompt += "[RULES]\n1. Method arguments MUST match the CSV data columns.\n2. Use the provided annotation EXACTLY.\n3. Call '" + target_name + "' on '" + instance_name + "'.\n4. Output ONLY the method code.\n"

        return await self._get_clean_code(code_prompt)

    async def _get_clean_code(self, prompt):
        response = await self._call_llm(prompt)
        clean = response.replace("```java", "").replace("```", "").strip()
        
        if "@" in clean:
            idx = clean.find("@")
            clean = clean[idx:]
            
        return clean
