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
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowContextStoreTest {
    
    @Test
    void assertFindPurgesExpiredSnapshot() throws Exception {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        contextStore.save(snapshot);
        @SuppressWarnings("unchecked")
        Map<String, WorkflowContextSnapshot> contexts = (Map<String, WorkflowContextSnapshot>) Plugins.getMemberAccessor()
                .get(WorkflowContextStore.class.getDeclaredField("contexts"), contextStore);
        contexts.get("plan-1").setUpdateTime(Instant.now().minus(Duration.ofDays(2)));
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
        Exception actual = assertThrows(MCPInvalidRequestException.class, () -> contextStore.getRequired("missing"));
        assertThat(actual.getMessage(), is("Unknown plan_id `missing`."));
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
}
