package com.example.llama.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Collaboration Team Test")
class CollaborationTeamTest {

    @Mock Agent worker;
    @Mock Agent reviewer;
    @Mock Agent arbitrator;

    @Test
    @DisplayName("should return output immediately if approved first time")
    void successOnFirstTry() {
        // given
        CollaborationTeam team = new CollaborationTeam(worker, reviewer, arbitrator);
        given(worker.act(anyString(), anyString())).willReturn("Good Code");
        // Reviewer receives formatted prompt, use contains to be safe
        given(reviewer.act(contains("Audit this code"), anyString())).willReturn("APPROVED");
        given(worker.getRole()).willReturn("Worker");
        given(reviewer.getRole()).willReturn("Reviewer");

        // when
        String result = team.execute("Write Code", "Context");

        // then
        assertThat(result).isEqualTo("Good Code");
        verify(worker, times(1)).act(anyString(), anyString());
    }

    @Test
    @DisplayName("should retry upon rejection and eventually succeed")
    void retryAndSucceed() {
        // given
        CollaborationTeam team = new CollaborationTeam(worker, reviewer, arbitrator);
        
        // 1st attempt
        given(worker.act(contains("Write Code"), anyString())).willReturn("Bad Code");
        given(reviewer.act(contains("Bad Code"), anyString())).willReturn("Fix bugs");
        
        // 2nd attempt
        given(worker.act(contains("Fix bugs"), anyString())).willReturn("Fixed Code");
        given(reviewer.act(contains("Fixed Code"), anyString())).willReturn("APPROVED");

        given(worker.getRole()).willReturn("Worker");
        given(reviewer.getRole()).willReturn("Reviewer");

        // when
        String result = team.execute("Write Code", "Context");

        // then
        assertThat(result).isEqualTo("Fixed Code");
        verify(worker, times(2)).act(anyString(), anyString());
    }

    @Test
    @DisplayName("should summon arbitrator after 3 failed attempts")
    void summonArbitrator() {
        // given
        CollaborationTeam team = new CollaborationTeam(worker, reviewer, arbitrator);
        given(worker.act(anyString(), anyString())).willReturn("Persistent Bad Code");
        given(reviewer.act(anyString(), anyString())).willReturn("Reject again");
        given(arbitrator.act(anyString(), anyString())).willReturn("Arbitrated Final Code");
        
        given(worker.getRole()).willReturn("Worker");
        given(reviewer.getRole()).willReturn("Reviewer");

        // when
        String result = team.execute("Write Code", "Context");

        // then
        assertThat(result).isEqualTo("Arbitrated Final Code");
        verify(worker, times(3)).act(anyString(), anyString());
        verify(reviewer, times(3)).act(anyString(), anyString());
        verify(arbitrator, times(1)).act(anyString(), anyString());
    }
}
