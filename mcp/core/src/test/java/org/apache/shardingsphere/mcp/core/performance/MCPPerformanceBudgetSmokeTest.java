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

package org.apache.shardingsphere.mcp.core.performance;

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.completion.provider.WorkflowPlanIdCompletionProvider;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.RequestContextFixture;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerCapabilitiesHandler;
import org.apache.shardingsphere.mcp.core.tool.handler.metadata.SearchMetadataToolHandler;
import org.apache.shardingsphere.mcp.core.workflow.InMemoryWorkflowSessionStore;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPPerformanceBudgetSmokeTest {
    
    private static final long DESCRIPTOR_BUDGET_MILLIS = 5000L;
    
    private static final long REQUEST_CONTEXT_BUDGET_MILLIS = 5000L;
    
    private static final long METADATA_SEARCH_BUDGET_MILLIS = 5000L;
    
    private static final long WORKFLOW_PLAN_PAYLOAD_BUDGET_MILLIS = 5000L;
    
    private static final long COMPLETION_BUDGET_MILLIS = 5000L;
    
    private static final int DESCRIPTOR_ITERATIONS = 100;
    
    private static final int REQUEST_CONTEXT_ITERATIONS = 200;
    
    private static final int METADATA_SEARCH_ITERATIONS = 100;
    
    private static final int WORKFLOW_PLAN_PAYLOAD_ITERATIONS = 1000;
    
    private static final int COMPLETION_ITERATIONS = 1000;
    
    @Test
    void assertDescriptorGenerationBudget() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        MCPFeatureRuntimeRequestContext requestContext = new MCPFeatureRuntimeRequestContext(runtimeContext, new MCPSessionIdentity("session-1", "", "", Map.of()));
        ServerCapabilitiesHandler handler = new ServerCapabilitiesHandler();
        Map<String, Object> actual = handler.handle(requestContext, new MCPUriVariables(Map.of())).toPayload();
        assertFalse(actual.containsKey("fingerprints"));
        long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < DESCRIPTOR_ITERATIONS; i++) {
                handler.handle(requestContext, new MCPUriVariables(Map.of())).toPayload();
            }
        });
        assertWithinBudget("descriptor generation", elapsedMillis, DESCRIPTOR_BUDGET_MILLIS);
    }
    
    @Test
    void assertRequestContextCreationBudget() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < REQUEST_CONTEXT_ITERATIONS; i++) {
                new MCPFeatureRuntimeRequestContext(runtimeContext, new MCPSessionIdentity("session-1", "", "", Map.of())).getMetadataQueryFacade();
            }
        });
        assertWithinBudget("request context creation", elapsedMillis, REQUEST_CONTEXT_BUDGET_MILLIS);
    }
    
    @Test
    void assertMetadataSearchBudget() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        try (RequestContextFixture requestContextFixture = ResourceTestDataFactory.createRequestContextFixture(runtimeContext, ResourceTestDataFactory.createDatabaseMetadata())) {
            MCPFeatureRuntimeRequestContext requestContext = requestContextFixture.getRequestContext();
            SearchMetadataToolHandler handler = new SearchMetadataToolHandler();
            Map<String, Object> arguments = Map.of("query", "order", "object_types", List.of("table"));
            assertDoesNotThrow(() -> handler.handle(requestContext, arguments));
            long elapsedMillis = measureElapsedMillis(() -> {
                for (int i = 0; i < METADATA_SEARCH_ITERATIONS; i++) {
                    handler.handle(requestContext, arguments).toPayload();
                }
            });
            assertWithinBudget("metadata search", elapsedMillis, METADATA_SEARCH_BUDGET_MILLIS);
        }
    }
    
    @Test
    void assertWorkflowPlanPayloadBudget() {
        WorkflowContextSnapshot snapshot = createWorkflowSnapshot("plan-1");
        assertTrue(WorkflowPlanPayloadBuilder.build(snapshot).containsKey("next_actions"));
        long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < WORKFLOW_PLAN_PAYLOAD_ITERATIONS; i++) {
                WorkflowPlanPayloadBuilder.build(snapshot);
            }
        });
        assertWithinBudget("workflow plan payload", elapsedMillis, WORKFLOW_PLAN_PAYLOAD_BUDGET_MILLIS);
    }
    
    @Test
    void assertWorkflowPlanIdCompletionBudget() {
        WorkflowSessionContext workflowSessionContext = new InMemoryWorkflowSessionStore().getSessionContext("session-1");
        workflowSessionContext.save(createWorkflowSnapshot("plan-1"));
        MCPFeatureRequestContext handlerContext = mock(MCPFeatureRequestContext.class);
        when(handlerContext.getSessionIdentity()).thenReturn(new MCPSessionIdentity("session-1", "", "", Map.of()));
        when(handlerContext.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        WorkflowPlanIdCompletionProvider provider = new WorkflowPlanIdCompletionProvider();
        MCPCompletionRequest request = new MCPCompletionRequest(
                new MCPCompletionTargetDescriptor("prompt", "recover_workflow", List.of("plan_id"), 50, Map.of()), "plan_id", Map.of());
        assertFalse(provider.complete(handlerContext, request).getCandidates().isEmpty());
        long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < COMPLETION_ITERATIONS; i++) {
                provider.complete(handlerContext, request);
            }
        });
        assertWithinBudget("workflow plan id completion", elapsedMillis, COMPLETION_BUDGET_MILLIS);
    }
    
    private WorkflowContextSnapshot createWorkflowSnapshot(final String planId) {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId("session-1");
        result.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        result.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        result.setRequest(request);
        result.setClarifiedIntent(new ClarifiedIntent());
        result.setInteractionPlan(InteractionPlan.create(planId, request, "Encrypt workflow plan.", List.of("review"), List.of("rules")));
        return result;
    }
    
    private long measureElapsedMillis(final Runnable action) {
        long startNanos = System.nanoTime();
        action.run();
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
    
    private void assertWithinBudget(final String name, final long elapsedMillis, final long budgetMillis) {
        assertTrue(elapsedMillis <= budgetMillis, () -> String.format("%s elapsedMillis=%d exceeded budgetMillis=%d", name, elapsedMillis, budgetMillis));
    }
}
