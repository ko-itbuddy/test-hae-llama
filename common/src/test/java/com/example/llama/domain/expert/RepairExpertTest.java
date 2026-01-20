package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Repair Specialist Expert Test")
class RepairExpertTest {

    private final RepairExpert expert = new RepairExpert();

    @Test
    @DisplayName("Should provided specialized mission for REPAIR_AGENT")
    void shouldProvideMissionResultingInXmlTemplate() {
        String mission = expert.getDomainMission(AgentType.REPAIR_AGENT);

        assertThat(mission)
                .contains("Senior Debugger")
                .contains("COMPLETE, FIXED JAVA CLASS")
                .contains("java_class")
                .contains("<response>")
                .contains("<![CDATA[");
    }

    @Test
    @DisplayName("Should provide default mission for other roles")
    void shouldProvideDefaultMission() {
        String mission = expert.getDomainMission(AgentType.SERVICE_CODER);
        assertThat(mission).contains("Perform code repair duties");
    }

    @Test
    @DisplayName("Should provide verification directive covering assertions")
    void shouldProvideVerificationDirective() {
        assertThat(expert.getVerificationDirective())
                .contains("Adjust assertions only if they are incorrect");
    }
}
