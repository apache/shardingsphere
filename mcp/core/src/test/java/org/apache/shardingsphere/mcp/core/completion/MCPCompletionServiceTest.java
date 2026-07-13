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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.core.completion.provider.MetadataCompletionProvider;
import org.apache.shardingsphere.mcp.core.completion.provider.WorkflowPlanIdCompletionProvider;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.workflow.InMemoryWorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPCompletionServiceTest {
    
    @Test
    void assertCompleteDatabaseValues() {
        MCPCompletionResult actual = complete(new InMemoryWorkflowSessionContext(), createDescriptor("inspect_metadata", "database", 1), "database", "", new LinkedHashMap<>());
        assertThat(actual.getValues(), is(List.of("logic_db")));
        assertThat(actual.getTotal(), is(2));
        assertTrue(actual.isHasMore());
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.DIAGNOSTIC), is("ok"));
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.RETURNED_CANDIDATE_COUNT), is(1));
    }
    
    @Test
    void assertCompleteTableValuesWithMissingContextDiagnostic() {
        MCPCompletionResult actual = complete(new InMemoryWorkflowSessionContext(), createDescriptor("inspect_metadata", "table", 50), "table", "order", new LinkedHashMap<>());
        assertThat(actual.getValues(), is(List.of()));
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.DIAGNOSTIC), is("missing_context"));
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.MISSING_CONTEXT_ARGUMENTS), is(List.of("database", "schema")));
        assertThat(((Map<?, ?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.RECOVERY)).get("recovery_category"), is("missing_context"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.NEXT_ACTIONS)).getFirst();
        assertThat(actualNextAction.get("resource_uri"), is("shardingsphere://databases"));
    }
    
    @Test
    void assertCompleteMissingContextUsesProtocolReferenceType() {
        MCPCompletionResult actual = complete(new InMemoryWorkflowSessionContext(), createDescriptor("inspect_metadata", "table", 50), "table", "order", new LinkedHashMap<>(),
                List.of(new MissingContextCompletionProvider()));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.NEXT_ACTIONS)).getFirst();
        assertThat(actualNextAction.get("ref"), is(Map.of("type", "ref/prompt", "name", "inspect_metadata")));
        assertThat(actualNextAction.get("resume_ref"), is(Map.of("type", "ref/prompt", "name", "inspect_metadata")));
    }
    
    @Test
    void assertCompletePlanIdsWithRecentPlanFirst() {
        WorkflowSessionContext workflowSessionContext = mock(WorkflowSessionContext.class);
        when(workflowSessionContext.list("session-1")).thenReturn(List.of(
                createSnapshot("plan-old", WorkflowLifecycle.STATUS_PLANNED, Instant.parse("2026-05-04T11:00:00Z")),
                createSnapshot("plan-new", WorkflowLifecycle.STATUS_PLANNED, Instant.parse("2026-05-04T12:00:00Z")),
                createSnapshot("plan-clarifying", WorkflowLifecycle.STATUS_CLARIFYING, Instant.parse("2026-05-04T13:00:00Z"))));
        MCPCompletionResult actual = complete(workflowSessionContext, createDescriptor("recover_workflow", "plan_id", 50), "plan_id", "plan-", new LinkedHashMap<>());
        assertThat(actual.getValues(), is(List.of("plan-new", "plan-old")));
        assertThat(((Map<?, ?>) ((List<?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.VALUE_DETAILS)).getFirst()).get("rankingReason"), is("recent-plan-first-for-plan_id"));
    }
    
    @Test
    void assertCompleteCapsMaxValues() {
        MCPCompletionResult actual = complete(new InMemoryWorkflowSessionContext(), createDescriptor("foo_prompt", "value", 101), "value", "value-", new LinkedHashMap<>(),
                List.of(new SizedCompletionProvider()));
        assertThat(actual.getValues().size(), is(100));
        assertThat(actual.getTotal(), is(101));
        assertTrue(actual.isHasMore());
    }
    
    @Test
    void assertCompleteReplacesEmptyContextWithInferredContext() {
        MCPCompletionResult actual = complete(new InMemoryWorkflowSessionContext(), createDescriptor("inspect_metadata", "table", 50), "table", "t_",
                Map.of("database", "logic_db", "schema", ""), List.of(new InferredContextCompletionProvider()));
        assertThat(actual.getValues(), is(List.of("t_order")));
        assertThat(((Map<?, ?>) actual.getMeta().get(MCPShardingSphereMetadataKeys.CONTEXT_ARGUMENTS)).get("schema"), is("public"));
        assertThat(actual.getMeta().get(MCPShardingSphereMetadataKeys.DIAGNOSTIC), is("ok"));
    }
    
    @Test
    void assertCompleteRejectsUndeclaredArgumentBeforeProviderInvocation() {
        MCPInvalidRequestException actual = assertThrows(MCPInvalidRequestException.class, () -> complete(new InMemoryWorkflowSessionContext(),
                createDescriptor("inspect_metadata", "database", 50), "table", "t_", new LinkedHashMap<>(), List.of(new UnexpectedCompletionProvider())));
        assertThat(actual.getMessage(), is("Completion argument `table` is not declared for prompt `inspect_metadata`."));
    }
    
    private MCPCompletionResult complete(final WorkflowSessionContext workflowSessionContext, final MCPCompletionTargetDescriptor descriptor, final String argumentName, final String prefix,
                                         final Map<String, String> contextArguments) {
        return complete(workflowSessionContext, descriptor, argumentName, prefix, contextArguments, List.of(new MetadataCompletionProvider(), new WorkflowPlanIdCompletionProvider()));
    }
    
    private MCPCompletionResult complete(final WorkflowSessionContext workflowSessionContext, final MCPCompletionTargetDescriptor descriptor, final String argumentName, final String prefix,
                                         final Map<String, String> contextArguments, final Collection<? extends MCPCompletionProvider<?>> completionProviders) {
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPCompletionProvider.class)).thenReturn(completionProviders);
            return new MCPCompletionService(createRuntimeContext(workflowSessionContext)).complete("session-1", descriptor, argumentName, prefix, contextArguments);
        }
    }
    
    private MCPCompletionTargetDescriptor createDescriptor(final String reference, final String argument, final int maxValues) {
        return new MCPCompletionTargetDescriptor("prompt", reference, List.of(argument), maxValues, Map.of());
    }
    
    private MCPRuntimeContext createRuntimeContext(final WorkflowSessionContext workflowSessionContext) {
        MCPDatabaseCapabilityProvider databaseCapabilityProvider = mock(MCPDatabaseCapabilityProvider.class);
        when(databaseCapabilityProvider.getDatabaseProfiles()).thenReturn(List.of(
                new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", true, true, IdentifierCasePolicyFactory.newInsensitivePolicySet()),
                new RuntimeDatabaseProfile("warehouse", "FixtureWarehouseDB", "2.0", true, true, IdentifierCasePolicyFactory.newInsensitivePolicySet())));
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
    
    private static final class SizedCompletionProvider implements MCPCompletionProvider<MCPWorkflowHandlerContext> {
        
        @Override
        public Class<MCPWorkflowHandlerContext> getContextType() {
            return MCPWorkflowHandlerContext.class;
        }
        
        @Override
        public boolean supports(final MCPCompletionRequestContext requestContext) {
            return "value".equals(requestContext.getArgumentName());
        }
        
        @Override
        public MCPCompletionProviderResult complete(final MCPWorkflowHandlerContext handlerContext, final MCPCompletionRequestContext requestContext) {
            return new MCPCompletionProviderResult(createCandidates());
        }
        
        private List<MCPCompletionCandidate> createCandidates() {
            return IntStream.rangeClosed(1, 101)
                    .mapToObj(each -> new MCPCompletionCandidate(String.format("value-%03d", each), "value", "test-provider")).toList();
        }
    }
    
    private static final class InferredContextCompletionProvider implements MCPCompletionProvider<MCPWorkflowHandlerContext> {
        
        @Override
        public Class<MCPWorkflowHandlerContext> getContextType() {
            return MCPWorkflowHandlerContext.class;
        }
        
        @Override
        public boolean supports(final MCPCompletionRequestContext requestContext) {
            return "table".equals(requestContext.getArgumentName());
        }
        
        @Override
        public MCPCompletionProviderResult complete(final MCPWorkflowHandlerContext handlerContext, final MCPCompletionRequestContext requestContext) {
            return new MCPCompletionProviderResult(List.of(new MCPCompletionCandidate("t_order", "logical table", "test-provider")), Map.of("schema", "public"), List.of(), "");
        }
    }
    
    private static final class MissingContextCompletionProvider implements MCPCompletionProvider<MCPWorkflowHandlerContext> {
        
        @Override
        public Class<MCPWorkflowHandlerContext> getContextType() {
            return MCPWorkflowHandlerContext.class;
        }
        
        @Override
        public boolean supports(final MCPCompletionRequestContext requestContext) {
            return "table".equals(requestContext.getArgumentName());
        }
        
        @Override
        public MCPCompletionProviderResult complete(final MCPWorkflowHandlerContext handlerContext, final MCPCompletionRequestContext requestContext) {
            return new MCPCompletionProviderResult(List.of(), Map.of(), List.of("database"), "");
        }
    }
    
    private static final class UnexpectedCompletionProvider implements MCPCompletionProvider<MCPWorkflowHandlerContext> {
        
        @Override
        public Class<MCPWorkflowHandlerContext> getContextType() {
            return MCPWorkflowHandlerContext.class;
        }
        
        @Override
        public boolean supports(final MCPCompletionRequestContext requestContext) {
            throw new AssertionError("Provider should not be invoked for undeclared completion arguments.");
        }
        
        @Override
        public MCPCompletionProviderResult complete(final MCPWorkflowHandlerContext handlerContext, final MCPCompletionRequestContext requestContext) {
            throw new AssertionError("Provider should not be invoked for undeclared completion arguments.");
        }
    }
}
