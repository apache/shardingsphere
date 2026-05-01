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

import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryWorkflowSessionContextTest {
    
    @Test
    void assertGetOrCreateCreatesNewSnapshot() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot actualSnapshot = workflowSessionContext.getOrCreate("session-1", "");
        assertThat(actualSnapshot.getSessionId(), is("session-1"));
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertTrue(actualSnapshot.getPlanId().startsWith("plan-"));
    }
    
    @Test
    void assertGetOrCreateReturnsStoredSnapshot() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        workflowSessionContext.save(snapshot);
        WorkflowContextSnapshot actualSnapshot = workflowSessionContext.getOrCreate("session-1", "plan-1");
        assertThat(actualSnapshot.getPlanId(), is("plan-1"));
        assertThat(actualSnapshot.getStatus(), is("planned"));
    }
    
    @Test
    void assertFindPurgesExpiredSnapshot() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-25T00:00:00Z"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext(clock, Duration.ofHours(24), 10);
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        workflowSessionContext.save(snapshot);
        clock.setInstant(clock.instant().plus(Duration.ofDays(2)));
        Optional<WorkflowContextSnapshot> actualSnapshot = workflowSessionContext.find("plan-1");
        assertFalse(actualSnapshot.isPresent());
    }
    
    @Test
    void assertGetRequiredReturnsSavedSnapshot() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        workflowSessionContext.save(snapshot);
        WorkflowContextSnapshot actualSnapshot = workflowSessionContext.getRequired("plan-1");
        assertThat(actualSnapshot.getPlanId(), is("plan-1"));
        assertThat(actualSnapshot.getStatus(), is("planned"));
    }
    
    @Test
    void assertGetRequiredThrowsForUnknownPlanId() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        Exception actualException = assertThrows(MCPInvalidRequestException.class, () -> workflowSessionContext.getRequired("missing"));
        assertThat(actualException.getMessage(), is("Unknown plan_id `missing`."));
    }
    
    @Test
    void assertRemoveDeletesSnapshot() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        workflowSessionContext.save(snapshot);
        workflowSessionContext.remove("plan-1");
        assertFalse(workflowSessionContext.find("plan-1").isPresent());
    }
    
    @Test
    void assertSaveStoresDetachedSnapshotCopy() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        workflowSessionContext.save(snapshot);
        snapshot.setStatus("failed");
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("planned"));
    }
    
    @Test
    void assertGetRequiredReturnsDetachedSnapshotCopy() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        workflowSessionContext.save(snapshot);
        WorkflowContextSnapshot actualSnapshot = workflowSessionContext.getRequired("plan-1");
        actualSnapshot.setStatus("failed");
        assertThat(workflowSessionContext.getRequired("plan-1").getStatus(), is("planned"));
    }
    
    @Test
    void assertPersistUpdatesCurrentStepAndStatus() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("intaking");
        snapshot.setInteractionPlan(interactionPlan);
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.persist(snapshot, "review", "planned");
        WorkflowContextSnapshot actualSnapshot = workflowSessionContext.getRequired("plan-1");
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getInteractionPlan().getCurrentStep(), is("review"));
    }
    
    @Test
    void assertSaveEvictsOldestSnapshotWhenCapacityExceeded() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-25T00:00:00Z"));
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext(clock, Duration.ofHours(24), 2);
        WorkflowContextSnapshot firstSnapshot = createSnapshot("plan-1");
        workflowSessionContext.save(firstSnapshot);
        clock.setInstant(clock.instant().plusSeconds(1L));
        WorkflowContextSnapshot secondSnapshot = createSnapshot("plan-2");
        workflowSessionContext.save(secondSnapshot);
        clock.setInstant(clock.instant().plusSeconds(1L));
        WorkflowContextSnapshot thirdSnapshot = createSnapshot("plan-3");
        workflowSessionContext.save(thirdSnapshot);
        assertFalse(workflowSessionContext.find("plan-1").isPresent());
        assertTrue(workflowSessionContext.find("plan-2").isPresent());
        assertTrue(workflowSessionContext.find("plan-3").isPresent());
    }
    
    @Test
    void assertConstructWithNonPositiveTtlThrowsException() {
        Exception actual = assertThrows(IllegalArgumentException.class, () -> new InMemoryWorkflowSessionContext(Clock.systemUTC(), Duration.ZERO, 1));
        assertThat(actual.getMessage(), is("Context TTL must be positive."));
    }
    
    @Test
    void assertConstructWithNonPositiveCapacityThrowsException() {
        Exception actual = assertThrows(IllegalArgumentException.class, () -> new InMemoryWorkflowSessionContext(Clock.systemUTC(), Duration.ofHours(1L), 0));
        assertThat(actual.getMessage(), is("Max entries must be positive."));
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        return result;
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
