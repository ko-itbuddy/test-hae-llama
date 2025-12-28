from .base import BaseAgent
from .architect import ArchitectAgent
from .critic import CriticAgent
from .librarian import LibrarianAgent
from .assembler import AssemblerAgent
from .specialists import ScoutAgent, DataDeptTeam, MockDeptTeam, ExecDeptTeam, VerifyDeptTeam

class ScenarioSquad:
    """초정밀 프로젝트 팀: 팀원 간 정보를 공유하고 피드백을 주고받습니다."""
    def __init__(self, llm, scenario_name, target_intel):
        self.name = scenario_name
        # 💡 팀원들이 공유할 '작전 지침서' (Brief)
        self.brief = f"Scenario: {scenario_name}\nTarget Intel: {target_intel}"
        
        # 전문 팀 편성
        self.data_team = DataDeptTeam(llm)
        self.mock_team = MockDeptTeam(llm)
        self.exec_team = ExecDeptTeam(llm)
        self.verify_team = VerifyDeptTeam(llm)
        self.assembler = AssemblerAgent(llm)

    async def execute_project(self, deep_intel): # 💡 Now receiving deep_intel
        print(f"   🚀 [Project-Squad] Launching informed TF for: {self.name}")
        
        # 1. 모든 공정에 Deep Intel을 전수하며 작업 수행
        data_row = await self.data_team.execute_mission(self.name, deep_intel, self.brief)
        self.brief += f"\nData: {data_row}"
        
        mock_code = await self.mock_team.execute_mission(self.name, deep_intel, self.brief)
        self.brief += f"\nMocks: {mock_code}"
        
        exec_code = await self.exec_team.execute_mission(self.name, deep_intel, self.brief)
        self.brief += f"\nExec: {exec_code}"
        
        verify_code = await self.verify_team.execute_mission(self.name, deep_intel, self.brief)

        # 2. 최종 조립
        return await self.assembler.assemble_test_method(
            self.name, data_row, mock_code, exec_code, verify_code
        )

class DirectorAgent(BaseAgent):
    """글로벌 본부: 시나리오별 TF(ScenarioSquad)를 창설하고 최종 성과를 검수합니다."""
    def __init__(self, llm, mcp_configs=None):
        super().__init__(llm, role="Global Project Director")
        self.architect = ArchitectAgent(llm)
        self.llm = llm

    async def orchestrate_test_generation(self, target_code, dependencies, context_mgr, strategy):
        # 1. 시나리오 기획
        print("[Director] 📜 Phase 1: High-Level Project Planning...")
        scenarios = await self.architect.plan_scenarios(target_code)
        
        # 2. 정밀 첩보 수집 (Deep Intel)
        scout = ScoutAgent(self.llm)
        print("[Director] 🕵️ Requesting Deep Intel for primary methods...")
        target_intel = await scout.analyze_target("the target service methods", target_code)
        
        results = []
        # 3. 프로젝트 팀(Squad) 파견
        for scenario in scenarios:
            squad = ScenarioSquad(self.llm, scenario, target_intel)
            # 💡 Fixed: Passing target_intel to execute_project
            outcome = await squad.execute_project(target_intel)
            results.append(outcome)

        return results