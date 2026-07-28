/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mcp.core.workflow;

import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class InMemoryWorkflowSessionStoreTest {
    
    @Test
    void assertGetOrCreateNewSnapshot() {
        WorkflowContextSnapshot actual = new InMemoryWorkflowSessionStore().getSessionContext("session-1").getOrCreate("");
        assertThat(actual.getSessionId(), is("session-1"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertTrue(actual.getPlanId().startsWith("plan-"));
    }
    
    @Test
    void assertGetOrCreateStoredSnapshot() {
        WorkflowSessionContext context = new InMemoryWorkflowSessionStore().getSessionContext("session-1");
        context.save(createSnapshot("plan-1", "planned"));
        assertThat(context.getOrCreate("plan-1").getStatus(), is("planned"));
    }
    
    @Test
    void assertSessionContextsAreIsolated() {
        InMemoryWorkflowSessionStore store = new InMemoryWorkflowSessionStore();
        store.getSessionContext("session-1").save(createSnapshot("plan-1", "planned"));
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class,
                () -> store.getSessionContext("session-2").getRequired("plan-1"));
        assertThat(actual.getMessage(), is("Unknown or unavailable plan_id `plan-1`. Call the planning tool again in the current MCP session."));
    }
    
    @Test
    void assertSaveBindsSnapshotToSession() {
        WorkflowSessionContext context = new InMemoryWorkflowSessionStore().getSessionContext("session-1");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "planned");
        snapshot.setSessionId("session-2");
        context.save(snapshot);
        assertThat(context.getRequired("plan-1").getSessionId(), is("session-1"));
    }
    
    @Test
    void assertSaveStoresDetachedSnapshot() {
        WorkflowSessionContext context = new InMemoryWorkflowSessionStore().getSessionContext("session-1");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "planned");
        context.save(snapshot);
        snapshot.setStatus("failed");
        assertThat(context.getRequired("plan-1").getStatus(), is("planned"));
    }
    
    @Test
    void assertGetRequiredReturnsDetachedSnapshot() {
        WorkflowSessionContext context = new InMemoryWorkflowSessionStore().getSessionContext("session-1");
        context.save(createSnapshot("plan-1", "planned"));
        WorkflowContextSnapshot actual = context.getRequired("plan-1");
        actual.setStatus("failed");
        assertThat(context.getRequired("plan-1").getStatus(), is("planned"));
    }
    
    @Test
    void assertPersistUpdatesLifecycle() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "clarifying");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("intaking");
        snapshot.setInteractionPlan(interactionPlan);
        WorkflowSessionContext context = new InMemoryWorkflowSessionStore().getSessionContext("session-1");
        context.persist(snapshot, "review", "planned");
        WorkflowContextSnapshot actual = context.getRequired("plan-1");
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is("review"));
    }
    
    @Test
    void assertListReturnsCurrentSessionSnapshotsInUpdateOrder() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-25T00:00:00Z"));
        InMemoryWorkflowSessionStore store = createStore(clock);
        WorkflowSessionContext firstContext = store.getSessionContext("session-1");
        firstContext.save(createSnapshot("plan-1", "planned"));
        store.getSessionContext("session-2").save(createSnapshot("plan-2", "planned"));
        clock.setInstant(clock.instant().plusSeconds(1L));
        firstContext.save(createSnapshot("plan-3", "planned"));
        List<WorkflowContextSnapshot> actual = firstContext.list();
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getPlanId(), is("plan-1"));
        assertThat(actual.get(1).getPlanId(), is("plan-3"));
    }
    
    @Test
    void assertRemoveSessionKeepsOtherSessions() {
        InMemoryWorkflowSessionStore store = new InMemoryWorkflowSessionStore();
        store.getSessionContext("session-1").save(createSnapshot("plan-1", "planned"));
        store.getSessionContext("session-2").save(createSnapshot("plan-2", "planned"));
        store.removeSession("session-1");
        assertThrows(MCPInvalidRequestException.class, () -> store.getSessionContext("session-1").getRequired("plan-1"));
        assertThat(store.getSessionContext("session-2").getRequired("plan-2").getPlanId(), is("plan-2"));
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String status) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setStatus(status);
        return result;
    }
    
    private InMemoryWorkflowSessionStore createStore(final Clock clock) {
        try (MockedStatic<Clock> mockedClock = mockStatic(Clock.class)) {
            mockedClock.when(Clock::systemUTC).thenReturn(clock);
            return new InMemoryWorkflowSessionStore();
        }
    }
    
    private static final class MutableClock extends Clock {
        
        private Instant currentInstant;
        
        private MutableClock(final Instant currentInstant) {
            this.currentInstant = currentInstant;
        }
        
        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }
        
        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }
        
        @Override
        public Instant instant() {
            return currentInstant;
        }
        
        private void setInstant(final Instant currentInstant) {
            this.currentInstant = currentInstant;
        }
    }
}
