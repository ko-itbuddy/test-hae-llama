import pytest
from unittest.mock import MagicMock, patch, AsyncMock
from src.rag_engine import run_context7_agent

@pytest.mark.asyncio
@patch('src.rag_engine.ChatOllama')
@patch('src.rag_engine.MCPBridge')
@patch('src.rag_engine.get_strategy')
@patch('src.rag_engine._call_chain')
async def test_run_context7_agent_workflow(mock_call_chain, mock_strategy, mock_mcp, mock_ollama, tmp_path):
    """
    CI 환경 검증용 모킹 테스트. 
    Ollama 직접 호출과 체인 호출을 모두 정확히 모킹라마!
    """
    # 1. Setup Mocks
    mock_file = tmp_path / "DummyService.java"
    mock_file.write_text("public class DummyService {}")
    
    strategy_inst = MagicMock()
    strategy_inst.get_architect_prompt.return_value = MagicMock()
    strategy_inst.get_coder_prompt.return_value = MagicMock()
    strategy_inst.get_quality_engineer_prompt.return_value = MagicMock()
    strategy_inst.assemble_final_class.return_value = "package com.example; class DummyTest {}"
    mock_strategy.return_value = strategy_inst
    
    # 💡 0.7.9: _call_chain 결과 모킹
    mock_call_chain.side_effect = [
        "SCENARIO: Test Success", # Analyst Plan
        "<CODE> @Test void test() {} </CODE>", # Coder
        "<CODE> @Test void test() {} </CODE>"  # QA
    ]
    
    # 💡 0.7.9: LLM 직접 호출(.content) 결과 모킹
    llm_inst = MagicMock()
    llm_inst.invoke.return_value = MagicMock(content="SKIP") # Researcher Decision
    mock_ollama.return_value = llm_inst
    
    # Mock MCP
    mcp_inst = AsyncMock()
    mcp_inst.connect = AsyncMock()
    mcp_inst.get_tools = AsyncMock(return_value=[])
    mcp_inst.disconnect = AsyncMock()
    mock_mcp.return_value = mcp_inst

    # 2. Execute
    result = await run_context7_agent(
        target_file_path=str(mock_file),
        target_code="class Dummy {}",
        initial_context="context",
        llm_model="glm4",
        project_path=str(tmp_path),
        strategy=strategy_inst,
        custom_rules=""
    )

    # 3. Assertions
    assert "DummyTest" in result
    assert mock_call_chain.called
    print("✅ All mocks (Chain & Direct) verified!")
