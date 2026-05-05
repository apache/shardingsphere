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

package org.apache.shardingsphere.mcp.bootstrap.transport.completion;

import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.workflow.InMemoryWorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPCompletionSpecificationFactoryTest {
    
    @Test
    void assertCompleteDatabaseValues() {
        SyncCompletionSpecification completionSpecification = findCompletion(createFactory(createRuntimeContext(new InMemoryWorkflowSessionContext())).createCompletionSpecifications(),
                new McpSchema.PromptReference("inspect_metadata"));
        McpSchema.CompleteResult actual = completionSpecification.completionHandler().apply(createExchange("session-1"),
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("inspect_metadata"), new McpSchema.CompleteRequest.CompleteArgument("database", "logic")));
        assertThat(actual.completion().values(), is(List.of("logic_db")));
        assertFalse(actual.completion().hasMore());
        assertThat(actual.meta().get("diagnostic"), is("ok"));
        assertThat(actual.meta().get("matchStrategy"), is("prefix"));
        assertThat(actual.meta().get("matchedCandidateCount"), is(1));
        assertThat(((List<?>) actual.meta().get("valueDetails")).size(), is(1));
    }
    
    @Test
    void assertCompleteDatabaseValuesWithContainsFallback() {
        SyncCompletionSpecification completionSpecification = findCompletion(createFactory(createRuntimeContext(new InMemoryWorkflowSessionContext())).createCompletionSpecifications(),
                new McpSchema.PromptReference("inspect_metadata"));
        McpSchema.CompleteResult actual = completionSpecification.completionHandler().apply(createExchange("session-1"),
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("inspect_metadata"), new McpSchema.CompleteRequest.CompleteArgument("database", "house")));
        assertThat(actual.completion().values(), is(List.of("warehouse")));
        assertThat(actual.meta().get("matchStrategy"), is("contains_fallback"));
        assertTrue(((List<?>) actual.meta().get("rankingPolicy")).contains("contains-fallback-when-prefix-has-no-match"));
    }
    
    @Test
    void assertCompleteTableValuesWithMissingContextDiagnostic() {
        SyncCompletionSpecification completionSpecification = findCompletion(createFactory(createRuntimeContext(new InMemoryWorkflowSessionContext())).createCompletionSpecifications(),
                new McpSchema.PromptReference("inspect_metadata"));
        McpSchema.CompleteResult actual = completionSpecification.completionHandler().apply(createExchange("session-1"),
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("inspect_metadata"), new McpSchema.CompleteRequest.CompleteArgument("table", "ord")));
        assertThat(actual.completion().values(), is(List.of()));
        assertThat(actual.meta().get("diagnostic"), is("missing_context"));
        assertThat(actual.meta().get("missingContextArguments"), is(List.of("database", "schema")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.meta().get("next_actions")).get(0);
        assertThat(actualNextAction.get("action_kind"), is("complete_argument"));
        assertThat(actualNextAction.get("argument_name"), is("database"));
    }
    
    @Test
    void assertCompleteDatabaseValuesWithPrefixFilteredDiagnostic() {
        SyncCompletionSpecification completionSpecification = findCompletion(createFactory(createRuntimeContext(new InMemoryWorkflowSessionContext())).createCompletionSpecifications(),
                new McpSchema.PromptReference("inspect_metadata"));
        McpSchema.CompleteResult actual = completionSpecification.completionHandler().apply(createExchange("session-1"),
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("inspect_metadata"), new McpSchema.CompleteRequest.CompleteArgument("database", "zzz")));
        assertThat(actual.completion().values(), is(List.of()));
        assertThat(actual.meta().get("diagnostic"), is("prefix_filtered_all_candidates"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.meta().get("next_actions")).get(0);
        assertThat(actualNextAction.get("action_kind"), is("complete_argument"));
        assertThat(actualNextAction.get("argument_name"), is("database"));
    }
    
    @Test
    void assertCompletePlanIdsWithNoCandidatesDiagnostic() {
        SyncCompletionSpecification completionSpecification = findCompletion(createFactory(createRuntimeContext(new InMemoryWorkflowSessionContext())).createCompletionSpecifications(),
                new McpSchema.PromptReference("recover_workflow"));
        McpSchema.CompleteResult actual = completionSpecification.completionHandler().apply(createExchange("session-1"),
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("recover_workflow"), new McpSchema.CompleteRequest.CompleteArgument("plan_id", "")));
        assertThat(actual.completion().values(), is(List.of()));
        assertThat(actual.meta().get("diagnostic"), is("no_candidates"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.meta().get("next_actions")).get(0);
        assertThat(actualNextAction.get("action_kind"), is("read_resource"));
        assertThat(actualNextAction.get("target_resource"), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertCompleteCurrentSessionPlanIds() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot("plan-1", "session-1", WorkflowLifecycle.STATUS_PLANNED));
        workflowSessionContext.save(createSnapshot("plan-2", "session-2", WorkflowLifecycle.STATUS_PLANNED));
        workflowSessionContext.save(createSnapshot("plan-3", "session-1", WorkflowLifecycle.STATUS_CLARIFYING));
        SyncCompletionSpecification completionSpecification = findCompletion(createFactory(createRuntimeContext(workflowSessionContext)).createCompletionSpecifications(),
                new McpSchema.PromptReference("recover_workflow"));
        McpSchema.CompleteResult actual = completionSpecification.completionHandler().apply(createExchange("session-1"),
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("recover_workflow"), new McpSchema.CompleteRequest.CompleteArgument("plan_id", "plan-")));
        assertThat(actual.completion().values(), is(List.of("plan-1")));
        assertThat(actual.completion().total(), is(1));
        assertThat(((Map<?, ?>) ((List<?>) actual.meta().get("valueDetails")).get(0)).get("rankingReason"), is("recent-plan-first-for-plan_id"));
    }
    
    @Test
    void assertCompletePlanIdsWithExactMatchFirst() {
        WorkflowSessionContext workflowSessionContext = mock(WorkflowSessionContext.class);
        when(workflowSessionContext.list("session-1")).thenReturn(List.of(
                createSnapshot("plan-a1", "session-1", WorkflowLifecycle.STATUS_PLANNED, Instant.parse("2026-05-04T12:00:00Z")),
                createSnapshot("plan-a", "session-1", WorkflowLifecycle.STATUS_PLANNED, Instant.parse("2026-05-04T11:00:00Z"))));
        SyncCompletionSpecification completionSpecification = findCompletion(createFactory(createRuntimeContext(workflowSessionContext)).createCompletionSpecifications(),
                new McpSchema.PromptReference("recover_workflow"));
        McpSchema.CompleteResult actual = completionSpecification.completionHandler().apply(createExchange("session-1"),
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("recover_workflow"), new McpSchema.CompleteRequest.CompleteArgument("plan_id", "plan-a")));
        assertThat(actual.completion().values(), is(List.of("plan-a", "plan-a1")));
    }
    
    @Test
    void assertCompletePlanIdsWithRecentPlanFirst() {
        WorkflowSessionContext workflowSessionContext = mock(WorkflowSessionContext.class);
        when(workflowSessionContext.list("session-1")).thenReturn(List.of(
                createSnapshot("plan-old", "session-1", WorkflowLifecycle.STATUS_PLANNED, Instant.parse("2026-05-04T11:00:00Z")),
                createSnapshot("plan-new", "session-1", WorkflowLifecycle.STATUS_PLANNED, Instant.parse("2026-05-04T12:00:00Z"))));
        SyncCompletionSpecification completionSpecification = findCompletion(createFactory(createRuntimeContext(workflowSessionContext)).createCompletionSpecifications(),
                new McpSchema.PromptReference("recover_workflow"));
        McpSchema.CompleteResult actual = completionSpecification.completionHandler().apply(createExchange("session-1"),
                new McpSchema.CompleteRequest(new McpSchema.PromptReference("recover_workflow"), new McpSchema.CompleteRequest.CompleteArgument("plan_id", "plan-")));
        assertThat(actual.completion().values(), is(List.of("plan-new", "plan-old")));
    }
    
    private MCPCompletionSpecificationFactory createFactory(final MCPRuntimeContext runtimeContext) {
        return new MCPCompletionSpecificationFactory(runtimeContext);
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
    
    private McpSyncServerExchange createExchange(final String sessionId) {
        McpSyncServerExchange result = mock(McpSyncServerExchange.class);
        when(result.sessionId()).thenReturn(sessionId);
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status) {
        return createSnapshot(planId, sessionId, status, null);
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status, final Instant updateTime) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        result.setUpdateTime(updateTime);
        return result;
    }
    
    private SyncCompletionSpecification findCompletion(final List<SyncCompletionSpecification> specifications, final McpSchema.CompleteReference reference) {
        return specifications.stream().filter(each -> reference.equals(each.referenceKey())).findFirst().orElseThrow();
    }
}
