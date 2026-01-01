package com.example.llama.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Collaboration Team Test")
class CollaborationTeamTest {

    @Mock private Agent worker;
    @Mock private Agent reviewer;
    @Mock private Agent arbitrator;

    @Test
    @DisplayName("should return output immediately if approved first time")
    void approveFirstTime() {
        CollaborationTeam squad = new CollaborationTeam(worker, reviewer, arbitrator);
        given(worker.getRole()).willReturn("Clerk");
        given(reviewer.getRole()).willReturn("Manager");
        
        given(worker.act(anyString(), anyString())).willReturn("draft 1");
        given(reviewer.act(anyString(), anyString())).willReturn("[APPROVED]");

        String result = squad.execute("mission", "context");

        assertThat(result).isEqualTo("draft 1");
    }

    @Test
    @DisplayName("should retry upon rejection and eventually succeed")
    void retryAndSucceed() {
        CollaborationTeam squad = new CollaborationTeam(worker, reviewer, arbitrator);
        given(worker.getRole()).willReturn("Clerk");
        given(reviewer.getRole()).willReturn("Manager");

        given(worker.act(anyString(), anyString())).willReturn("draft 1", "draft 2");
        given(reviewer.act(contains("draft 1"), anyString())).willReturn("[REJECTED] error");
        given(reviewer.act(contains("draft 2"), anyString())).willReturn("[APPROVED]");

        String result = squad.execute("mission", "context");

        assertThat(result).isEqualTo("draft 2");
    }

    @Test
    @DisplayName("should summon arbitrator after failed attempts")
    void summonArbitrator() {
        CollaborationTeam squad = new CollaborationTeam(worker, reviewer, arbitrator);
        given(worker.getRole()).willReturn("Clerk");
        given(reviewer.getRole()).willReturn("Manager");

        given(worker.act(anyString(), anyString())).willReturn("draft 1", "draft 2");
        given(reviewer.act(anyString(), anyString())).willReturn("[REJECTED] error");
        given(arbitrator.act(anyString(), anyString())).willReturn("Final Verdict");

        String result = squad.execute("mission", "context");

        assertThat(result).isEqualTo("Final Verdict");
        verify(arbitrator).act(contains("ARBITRATION"), anyString());
    }
}
