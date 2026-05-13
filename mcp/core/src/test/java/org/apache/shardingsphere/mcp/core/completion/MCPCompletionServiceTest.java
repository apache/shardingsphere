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

package org.apache.shardingsphere.mcp.core.completion;

import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.workflow.InMemoryWorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPCompletionServiceTest {
    
    @Test
    void assertCompleteDatabaseValues() {
        MCPCompletionResult actual = new MCPCompletionService(createRuntimeContext(new InMemoryWorkflowSessionContext())).complete("session-1",
                createDescriptor("prompt", "inspect_metadata", "database", 1), "database", "", new LinkedHashMap<>());
        assertThat(actual.getValues(), is(List.of("logic_db")));
        assertThat(actual.getTotal(), is(2));
        assertTrue(actual.isHasMore());
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.DIAGNOSTIC), is("ok"));
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.RETURNED_CANDIDATE_COUNT), is(1));
    }
    
    @Test
    void assertCompleteTableValuesWithMissingContextDiagnostic() {
        MCPCompletionResult actual = new MCPCompletionService(createRuntimeContext(new InMemoryWorkflowSessionContext())).complete("session-1",
                createDescriptor("prompt", "inspect_metadata", "table", 50), "table", "order", new LinkedHashMap<>());
        assertThat(actual.getValues(), is(List.of()));
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.DIAGNOSTIC), is("missing_context"));
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.MISSING_CONTEXT_ARGUMENTS), is(List.of("database", "schema")));
        assertThat(((Map<?, ?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.RECOVERY)).get("recovery_category"), is("missing_context"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.NEXT_ACTIONS)).get(0);
        assertThat(actualNextAction.get("resource_uri"), is("shardingsphere://databases"));
    }
    
    @Test
    void assertCompletePlanIdsWithRecentPlanFirst() {
        WorkflowSessionContext workflowSessionContext = mock(WorkflowSessionContext.class);
        when(workflowSessionContext.list("session-1")).thenReturn(List.of(
                createSnapshot("plan-old", WorkflowLifecycle.STATUS_PLANNED, Instant.parse("2026-05-04T11:00:00Z")),
                createSnapshot("plan-new", WorkflowLifecycle.STATUS_PLANNED, Instant.parse("2026-05-04T12:00:00Z")),
                createSnapshot("plan-clarifying", WorkflowLifecycle.STATUS_CLARIFYING, Instant.parse("2026-05-04T13:00:00Z"))));
        MCPCompletionResult actual = new MCPCompletionService(createRuntimeContext(workflowSessionContext)).complete("session-1",
                createDescriptor("prompt", "recover_workflow", "plan_id", 50), "plan_id", "plan-", new LinkedHashMap<>());
        assertThat(actual.getValues(), is(List.of("plan-new", "plan-old")));
        assertThat(((Map<?, ?>) ((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.VALUE_DETAILS)).get(0)).get("rankingReason"), is("recent-plan-first-for-plan_id"));
    }
    
    private MCPCompletionTargetDescriptor createDescriptor(final String referenceType, final String reference, final String argument, final int maxValues) {
        return new MCPCompletionTargetDescriptor(referenceType, reference, List.of(argument), maxValues, Map.of());
    }
    
    private MCPRuntimeContext createRuntimeContext(final WorkflowSessionContext workflowSessionContext) {
        MCPDatabaseCapabilityProvider databaseCapabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        when(databaseCapabilityProvider.getDatabaseProfiles()).thenReturn(List.of(
                new RuntimeDatabaseProfile("logic_db", "MySQL", "8.0"),
                new RuntimeDatabaseProfile("warehouse", "PostgreSQL", "16")));
        MCPRuntimeContext result = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
        when(result.getDatabaseCapabilityProvider()).thenReturn(databaseCapabilityProvider);
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(result.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String status, final Instant updateTime) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setStatus(status);
        result.setUpdateTime(updateTime);
        return result;
    }
}
