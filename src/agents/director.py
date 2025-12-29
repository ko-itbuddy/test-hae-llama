import asyncio
from .base import BaseAgent
from .architect import ArchitectAgent
from .critic import CriticAgent
from .librarian import LibrarianAgent
from .assembler import AssemblerAgent
from .specialists.utils import ScoutAgent
from .specialists.data import DataDeptTeam
from .specialists.mock import MockDeptTeam
from .specialists.exec import ExecDeptTeam
from .specialists.verify import VerifyDeptTeam

class ScenarioSquad:
    """초정밀 프로젝트 팀: 팀원 간 정보를 공유하고 피드백을 주고받습니다."""
    def __init__(self, llm, scenario_name, target_intel, librarian, extra_guide="", target_file="unknown"):
        self.name = scenario_name
        self.brief = f"Scenario: {scenario_name}\nTarget Intel: {target_intel}"
        self.librarian = librarian
        
        if extra_guide:
            self.brief += f"\n[SPECIAL_RESEARCH_DATA]\n{extra_guide}"
            
        # 💡 Dispatching teams with Librarian for JIT learning
        self.data_team = DataDeptTeam(llm, target_file=target_file, librarian=librarian)
        self.mock_team = MockDeptTeam(llm, target_file=target_file, librarian=librarian)
        self.exec_team = ExecDeptTeam(llm, target_file=target_file, librarian=librarian)
        self.verify_team = VerifyDeptTeam(llm, target_file=target_file, librarian=librarian)
        self.assembler = AssemblerAgent(llm, target_file=target_file) # Re-using guide from Director for efficiency

    async def execute_project(self, deep_intel, classpath="."): # 💡 Receiving classpath
        print(f"   🚀 [Squad] TF Mission with Classpath Support: {self.name}")
        
        # 1. 모든 공정에 Classpath와 Intel 전수
        data_row = await self.data_team.execute_mission(self.name, deep_intel, self.brief, classpath)
        self.brief += f"\n[INPUT_DATA] {data_row}"
        
        mock_code = await self.mock_team.execute_mission(self.name, deep_intel, self.brief, classpath)
        self.brief += f"\n[STUBBING] {mock_code}"
        
        exec_code = await self.exec_team.execute_mission(self.name, deep_intel, self.brief, classpath)
        self.brief += f"\n[EXECUTION] {exec_code}"
        
        verify_code = await self.verify_team.execute_mission(self.name, deep_intel, self.brief, classpath)

        return await self.assembler.assemble_test_method(
            self.name, data_row, mock_code, exec_code, verify_code
        )

class DirectorAgent(BaseAgent):
    """글로벌 본부: 시나리오별 TF(ScenarioSquad)를 창설하고 최종 성과를 검수합니다."""
    def __init__(self, llm, mcp_configs=None, target_file="unknown"):
        from src.utils.config_loader import config
        super().__init__(llm, role="Global Project Director", target_file=target_file)
        self.llm = llm
        self.target_file = target_file
        self.architect = ArchitectAgent(llm, target_file=target_file)
        self.scout = ScoutAgent(llm, target_file=target_file)
        
        # 💡 [v6.3] Use MCP Config from Global Config
        mcp_conf = mcp_configs[0] if mcp_configs else config.get("mcp.context7", "context7|npx|@upstash/mcp-context7")
        self.librarian = LibrarianAgent(llm, mcp_conf, target_file=target_file)

    async def orchestrate_test_generation(self, target_code, dependencies, context_mgr, strategy):
        # 💡 [v8.0 PRECISION] High-Efficiency Data Orchestration
        from src.dependency import get_project_classpath
        classpath = get_project_classpath(".")
        
        # 1. Blueprint Phase
        scenarios = await self.architect.plan_scenarios(target_code)
        
        # 2. Extract Dependency Skeletons
        dep_classes = [dep[0] for dep in dependencies if dep[0] not in ["String", "int", "Long", "boolean"]]
        print(f"[Director] 🕵️ Extracting skeletal specs for: {dep_classes}")
        skeletal_manual = await self.librarian.fetch_class_intel(dep_classes)
        
        # Get target class summary
        target_intel = await self.scout.analyze_target("target class", target_code, strategy)
        
        results = []
        semaphore = asyncio.Semaphore(1)

        async def run_squad(scenario):
            async with semaphore:
                print(f"\n{'='*10} 🚀 Launching Elite Team for: {scenario} {'='*10}")
                
                # 💡 [PRECISION] Get ONLY the method body under test
                method_name = scenario.split(" - ")[0].strip()
                method_body = context_mgr.get_method_context(method_name, target_code, strategy)
                
                # 💡 ENRICHED MINIMAL CONTEXT
                final_intel = f"""[TARGET_METHOD_CODE]
{method_body or target_code[:1000]}

[DEPENDENCY_SKELETONS]
{skeletal_manual}

[OVERALL_SPEC]
{target_intel}
"""
                squad = ScenarioSquad(self.llm, scenario, final_intel, self.librarian, "", target_file=self.target_file)
                outcome = await squad.execute_project(final_intel, classpath)
                return outcome

        tasks = [run_squad(s) for s in scenarios]
        results = await asyncio.gather(*tasks)
        return results