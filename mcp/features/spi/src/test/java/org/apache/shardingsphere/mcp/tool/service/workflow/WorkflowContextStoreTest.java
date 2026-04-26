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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowContextStoreTest {
    
    @Test
    void assertGetOrCreateCreatesNewSnapshot() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot actualSnapshot = contextStore.getOrCreate("session-1", "");
        assertThat(actualSnapshot.getSessionId(), is("session-1"));
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertTrue(actualSnapshot.getPlanId().startsWith("plan-"));
    }
    
    @Test
    void assertGetOrCreateReturnsStoredSnapshot() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        contextStore.save(snapshot);
        WorkflowContextSnapshot actualSnapshot = contextStore.getOrCreate("session-1", "plan-1");
        assertThat(actualSnapshot.getPlanId(), is("plan-1"));
        assertThat(actualSnapshot.getStatus(), is("planned"));
    }
    
    @Test
    void assertFindPurgesExpiredSnapshot() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-25T00:00:00Z"));
        WorkflowContextStore contextStore = new WorkflowContextStore(clock, Duration.ofHours(24), 10);
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        contextStore.save(snapshot);
        clock.setInstant(clock.instant().plus(Duration.ofDays(2)));
        Optional<WorkflowContextSnapshot> actualSnapshot = contextStore.find("plan-1");
        assertFalse(actualSnapshot.isPresent());
    }
    
    @Test
    void assertGetRequiredReturnsSavedSnapshot() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        contextStore.save(snapshot);
        WorkflowContextSnapshot actualSnapshot = contextStore.getRequired("plan-1");
        assertThat(actualSnapshot.getPlanId(), is("plan-1"));
        assertThat(actualSnapshot.getStatus(), is("planned"));
    }
    
    @Test
    void assertGetRequiredThrowsForUnknownPlanId() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        Exception actualException = assertThrows(MCPInvalidRequestException.class, () -> contextStore.getRequired("missing"));
        assertThat(actualException.getMessage(), is("Unknown plan_id `missing`."));
    }
    
    @Test
    void assertRemoveDeletesSnapshot() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        contextStore.save(snapshot);
        contextStore.remove("plan-1");
        assertFalse(contextStore.find("plan-1").isPresent());
    }
    
    @Test
    void assertSaveStoresDetachedSnapshotCopy() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        contextStore.save(snapshot);
        snapshot.setStatus("failed");
        assertThat(contextStore.getRequired("plan-1").getStatus(), is("planned"));
    }
    
    @Test
    void assertGetRequiredReturnsDetachedSnapshotCopy() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        contextStore.save(snapshot);
        WorkflowContextSnapshot actualSnapshot = contextStore.getRequired("plan-1");
        actualSnapshot.setStatus("failed");
        assertThat(contextStore.getRequired("plan-1").getStatus(), is("planned"));
    }
    
    @Test
    void assertPersistUpdatesCurrentStepAndStatus() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("intaking");
        snapshot.setInteractionPlan(interactionPlan);
        WorkflowContextStore contextStore = new WorkflowContextStore();
        contextStore.persist(snapshot, "review", "planned");
        WorkflowContextSnapshot actualSnapshot = contextStore.getRequired("plan-1");
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getInteractionPlan().getCurrentStep(), is("review"));
    }
    
    @Test
    void assertSaveEvictsOldestSnapshotWhenCapacityExceeded() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-25T00:00:00Z"));
        WorkflowContextStore contextStore = new WorkflowContextStore(clock, Duration.ofHours(24), 2);
        WorkflowContextSnapshot firstSnapshot = createSnapshot("plan-1");
        contextStore.save(firstSnapshot);
        clock.setInstant(clock.instant().plusSeconds(1L));
        WorkflowContextSnapshot secondSnapshot = createSnapshot("plan-2");
        contextStore.save(secondSnapshot);
        clock.setInstant(clock.instant().plusSeconds(1L));
        WorkflowContextSnapshot thirdSnapshot = createSnapshot("plan-3");
        contextStore.save(thirdSnapshot);
        assertFalse(contextStore.find("plan-1").isPresent());
        assertTrue(contextStore.find("plan-2").isPresent());
        assertTrue(contextStore.find("plan-3").isPresent());
    }
    
    @Test
    void assertConstructWithNonPositiveTtlThrowsException() {
        Exception actual = assertThrows(IllegalArgumentException.class, () -> new WorkflowContextStore(Clock.systemUTC(), Duration.ZERO, 1));
        assertThat(actual.getMessage(), is("Context TTL must be positive."));
    }
    
    @Test
    void assertConstructWithNonPositiveCapacityThrowsException() {
        Exception actual = assertThrows(IllegalArgumentException.class, () -> new WorkflowContextStore(Clock.systemUTC(), Duration.ofHours(1L), 0));
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
