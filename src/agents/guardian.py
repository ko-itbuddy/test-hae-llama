import re
from .base import BaseAgent

class GuardianAgent(BaseAgent):
    def __init__(self, llm):
        super().__init__(llm, role="Security & Privacy Guardian")
        # Basic PII patterns
        self.patterns = {
            "email": r'[\w\.-]+@[\w\.-]+\.\w+',
            "phone": r'\d{2,3}-\d{3,4}-\d{4}',
            "password": r'(?i)(password|passwd|secret|token|api_key|apikey)\s*[:=]\s*["\']([^"\\]+)["\\]'
        }

    def mask_code(self, code):
        """Masks sensitive information in the code before sending to LLM."""
        masked_code = code
        # Mask Emails
        masked_code = re.sub(self.patterns["email"], "[EMAIL_MASKED]", masked_code)
        # Mask Phone Numbers
        masked_code = re.sub(self.patterns["phone"], "[PHONE_MASKED]", masked_code)
        # Mask Secrets/Passwords (masking only the value group)
        def secret_replacer(match):
            return f'{match.group(1)}: "[SECRET_MASKED]"'
        masked_code = re.sub(self.patterns["password"], secret_replacer, masked_code)
        
        return masked_code

    async def scan_for_vulnerabilities(self, code):
        """Optional: Use LLM to scan for deeper security issues."""
        prompt = f"""Scan this Java code for potential security vulnerabilities (SQLi, XSS, etc.).
[CODE]
{code}
Return ONLY a summary of issues or 'CLEAN'.
"""
        return await self._call_llm(prompt)
