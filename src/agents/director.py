from .base import BaseAgent
from .specialists import DataClerk, MockSpecialist, AssertionSpecialist, Assembler
from .architect import ArchitectAgent

from .architect import ArchitectAgent
from .critic import CriticAgent
from .specialists import ScoutAgent, MockerClerk, ExecutionClerk, AssertionClerk, DataClerk

from .specialists import ScoutAgent, MockerClerk, MockManager, ExecutionClerk, ExecutionManager, AssertionClerk, AssertionManager

from .librarian import LibrarianAgent

class ScenarioSquad:
    """프로젝트 전담 팀: 하나의 시나리오를 완성하기 위해 전문가들이 뭉친 TF팀입니다."""
    def __init__(self, llm, scenario_name, context_mgr, strategy):
        self.name = scenario_name
        self.clerk = ImplementerAgent(llm) # 다능인 실무자
        self.reviewer = CriticAgent(llm) # QA 담당
        self.context_mgr = context_mgr
        self.strategy = strategy

    async def execute_project(self, target_code, mock_info, instance_name):
        print(f"   🚀 [Project-TF] Starting Scenario: {self.name}")
        
        # 1. 태스크 수행 (Code Generation)
        code_fragment = await self.clerk.implement_test_method(
            self.name, "primaryMethod", target_code, mock_info, instance_name
        )
        
        # 2. 품질 검수 (QA)
        issues = self.reviewer.quick_review(code_fragment, self.name, "unknown")
        if issues:
            print(f"      ⚠️ QA Rejected: {issues}")
            # 자가 수정 로직 (Self-Healing)
            
        return code_fragment

class ScenarioSquad:
    """초정밀 프로젝트 팀: 단계별 [실무자-매니저-QA] 결재 라인을 수행합니다."""
    def __init__(self, llm, scenario_name):
        self.name = scenario_name
        # Roles for each stage
        self.data_dept = {"clerk": DataClerk(llm), "mgr": DataManager(llm), "qa": DataQA(llm)}
        self.mock_dept = {"clerk": MockerClerk(llm), "mgr": MockManager(llm), "qa": MockQA(llm)}
        self.exec_dept = {"clerk": ExecClerk(llm), "mgr": ExecManager(llm), "qa": ExecQA(llm)}
        self.verify_dept = {"clerk": AssertClerk(llm), "mgr": AssertManager(llm), "qa": AssertQA(llm)}

    async def execute_project(self):
        print(f"   🏭 [Project-Squad] Manufacturing Scenario: {self.name}")
        
        # 1. Data Stage
        data_row = await self._run_stage("DATA", self.data_dept, self.name)
        # 2. Mock Stage
        mock_code = await self._run_stage("MOCK", self.mock_dept, "dependency info")
        # 3. Exec Stage
        exec_code = await self._run_stage("EXEC", self.exec_dept, "method signature")
        # 4. Verify Stage
        verify_code = await self._run_stage("VERIFY", self.verify_dept, "test goal")

        # Result assembly
        return f"// {self.name}\n{mock_code}\n{exec_code}\n{verify_code}"

    async def _run_stage(self, stage_name, dept, context):
        """3중 결재 공정: 실무자 작업 -> 매니저 승인 -> QA 검증"""
        for attempt in range(2):
            # A. Task by Clerk
            work = await dept["clerk"].task(context) if hasattr(dept["clerk"], "task") else ""
            
            # B. Approval by Manager
            approval = await dept["mgr"].approve(work)
            if "APPROVED" not in approval.upper():
                print(f"      ⚠️ [{stage_name}] Manager Rejected: {approval}")
                continue
                
            # C. Verification by QA
            v_result = await dept["qa"].verify(work)
            if "PASSED" not in v_result.upper() and "VALID" not in v_result.upper():
                print(f"      ⚠️ [{stage_name}] QA Flagged: {v_result}")
                continue
                
            return work
        return f"// {stage_name} FAILED AFTER RETRIES"

from .specialists import (
    DataClerk, DataManager, DataQA,
    MockerClerk, MockManager, MockQA,
    ExecClerk, ExecManager, ExecQA,
    AssertClerk, AssertManager, AssertQA
)

from .specialists import DataDeptTeam, MockDeptTeam, ExecDeptTeam, VerifyDeptTeam

from .assembler import AssemblerAgent

class ScenarioSquad:
    """초정밀 프로젝트 팀: 팀원 간 정보를 공유하고 피드백을 주고받습니다."""
    def __init__(self, llm, scenario_name, target_intel):
        self.name = scenario_name
        self.brief = f"Scenario: {scenario_name}\nTarget Method Intel: {target_intel}"
        self.data_team = DataDeptTeam(llm)
        self.mock_team = MockDeptTeam(llm)
        self.exec_team = ExecDeptTeam(llm)
        self.verify_team = VerifyDeptTeam(llm)
        self.assembler = AssemblerAgent(llm)

    async def execute_project(self):
        print(f"   🚀 [Project-Squad] Executing TF Mission: {self.name}")
        
        # 1. 각 팀에게 브리핑 전달 및 수행
        data_row = await self.data_team.execute_mission(self.name, self.brief)
        # 이전 단계의 결과물을 다음 팀의 브리핑에 추가 (Chain of Responsibility)
        self.brief += f"\nGenerated Data: {data_row}"
        
        mock_code = await self.mock_team.execute_mission(self.name, self.brief)
        self.brief += f"\nMocks Configured: {mock_code}"
        
        exec_code = await self.exec_team.execute_mission(self.name, self.brief)
        self.brief += f"\nExecution Call: {exec_code}"
        
        verify_code = await self.verify_team.execute_mission(self.name, self.brief)

        # 2. 최종 조립
        return await self.assembler.assemble_test_method(
            self.name, data_row, mock_code, exec_code, verify_code
        )

class DirectorAgent(BaseAgent):
    """글로벌 본부: 독립 팀(Squad)을 편성하여 테스트를 완성합니다."""
    def __init__(self, llm, mcp_configs=None):
        super().__init__(llm, role="Global Project Director")
        self.architect = ArchitectAgent(llm)
        self.llm = llm

    async def orchestrate_test_generation(self, target_code, dependencies, context_mgr, strategy):
        print("[Director] 📜 Phase 1: High-Level Project Planning...")
        scenarios = await self.architect.plan_scenarios(target_code)
        
        results = []
        for scenario in scenarios:
            squad = ScenarioSquad(self.llm, scenario)
            outcome = await squad.execute_project()
            results.append(outcome)

        return results
