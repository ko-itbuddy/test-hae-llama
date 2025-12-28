from .base import BaseAgent
from .architect import ArchitectAgent
from .critic import CriticAgent
from .librarian import LibrarianAgent
from .assembler import AssemblerAgent
from .specialists import ScoutAgent, DataDeptTeam, MockDeptTeam, ExecDeptTeam, VerifyDeptTeam

class ScenarioSquad:
    """초정밀 프로젝트 팀: 팀원 간 정보를 공유하고 피드백을 주고받습니다."""
    def __init__(self, llm, scenario_name, target_intel, librarian, extra_guide=""):
        self.name = scenario_name
        self.brief = f"Scenario: {scenario_name}\nTarget Intel: {target_intel}"
        self.librarian = librarian # 💡 Now correctly receiving the librarian agent
        
        if extra_guide:
            self.brief += f"\n[SPECIAL_RESEARCH_DATA]\n{extra_guide}"
            
        self.data_team = DataDeptTeam(llm)
        self.mock_team = MockDeptTeam(llm)
        self.exec_team = ExecDeptTeam(llm)
        self.verify_team = VerifyDeptTeam(llm)
        self.assembler = AssemblerAgent(llm) # Re-using guide from Director for efficiency

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
    def __init__(self, llm, mcp_configs=None):
        super().__init__(llm, role="Global Project Director")
        self.llm = llm
        self.architect = ArchitectAgent(llm)
        self.scout = ScoutAgent(llm)
        # Initialize Librarian with MCP config if provided
        mcp_conf = mcp_configs[0] if mcp_configs else "context7|npx|@upstash/mcp-context7"
        self.librarian = LibrarianAgent(llm, mcp_conf)

    async def orchestrate_test_generation(self, target_code, dependencies, context_mgr, strategy):
        # 💡 Step 0: Get Real Classpath (The Lawbook)
        from src.dependency import get_project_classpath
        print("[Director] 🕵️ Scouting for Project Classpath...")
        classpath = get_project_classpath(".") # Current project path
        
        # 1. 시나리오 기획
        scenarios = await self.architect.plan_scenarios(target_code)
        
        # 2. 정밀 첩보 수집
        target_intel = await self.scout.analyze_target("primary methods", target_code)
        
        results = []
        # 3. 프로젝트 팀(Squad) 파견
        for scenario in scenarios:
            # 💡 Now squads have the Classpath!
            squad = ScenarioSquad(self.llm, scenario, target_intel, self.librarian)
            outcome = await squad.execute_project(target_intel, classpath)
            results.append(outcome)

        return results