import asyncio
import os
import sys

# Add src to path
sys.path.append(os.path.abspath("."))

from src.rag_engine import validate_and_fix

# Fake main
async def main():
    target_file = "sample-project/src/test/java/com/example/demo/service/OrderServiceTest.java"
    project_path = "sample-project"
    
    print("--- Starting Self-Healing Demo ---")
    await validate_and_fix(target_file, project_path, llm_model="qwen2.5-coder:14b")
    print("--- Demo Finished ---")

if __name__ == "__main__":
    asyncio.run(main())
