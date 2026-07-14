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

import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.completion.provider.WorkflowPlanIdCompletionProvider;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.RequestScopeFixture;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerCapabilitiesHandler;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.core.tool.handler.metadata.SearchMetadataToolHandler;
import org.apache.shardingsphere.mcp.core.workflow.InMemoryWorkflowSessionStore;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowRequestContext;
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
    
    private static final long REQUEST_SCOPE_BUDGET_MILLIS = 5000L;
    
    private static final long METADATA_SEARCH_BUDGET_MILLIS = 5000L;
    
    private static final long WORKFLOW_PLAN_PAYLOAD_BUDGET_MILLIS = 5000L;
    
    private static final long COMPLETION_BUDGET_MILLIS = 5000L;
    
    private static final long SQL_CLASSIFIER_BUDGET_MILLIS = 5000L;
    
    private static final int DESCRIPTOR_ITERATIONS = 100;
    
    private static final int REQUEST_SCOPE_ITERATIONS = 200;
    
    private static final int METADATA_SEARCH_ITERATIONS = 100;
    
    private static final int WORKFLOW_PLAN_PAYLOAD_ITERATIONS = 1000;
    
    private static final int COMPLETION_ITERATIONS = 1000;
    
    private static final int SQL_CLASSIFIER_ITERATIONS = 1000;
    
    @Test
    void assertDescriptorGenerationBudget() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        MCPRequestScope requestScope = new MCPRequestScope(runtimeContext, "session-1");
        ServerCapabilitiesHandler handler = new ServerCapabilitiesHandler();
        Map<String, Object> actual = handler.handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
        assertFalse(actual.containsKey("fingerprints"));
        long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < DESCRIPTOR_ITERATIONS; i++) {
                handler.handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            }
        });
        assertWithinBudget("descriptor generation", elapsedMillis, DESCRIPTOR_BUDGET_MILLIS);
    }
    
    @Test
    void assertRequestScopeCreationBudget() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < REQUEST_SCOPE_ITERATIONS; i++) {
                new MCPRequestScope(runtimeContext, "session-1").getMetadataQueryFacade();
            }
        });
        assertWithinBudget("request scope creation", elapsedMillis, REQUEST_SCOPE_BUDGET_MILLIS);
    }
    
    @Test
    void assertMetadataSearchBudget() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        try (RequestScopeFixture requestScopeFixture = ResourceTestDataFactory.createRequestScopeFixture(runtimeContext, ResourceTestDataFactory.createDatabaseMetadata())) {
            MCPRequestScope requestScope = requestScopeFixture.getRequestScope();
            SearchMetadataToolHandler handler = new SearchMetadataToolHandler();
            Map<String, Object> arguments = Map.of("query", "order", "object_types", List.of("table"));
            assertDoesNotThrow(() -> handler.handle(requestScope, arguments));
            long elapsedMillis = measureElapsedMillis(() -> {
                for (int i = 0; i < METADATA_SEARCH_ITERATIONS; i++) {
                    handler.handle(requestScope, arguments).toPayload();
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
        MCPWorkflowRequestContext handlerContext = mock(MCPWorkflowRequestContext.class);
        when(handlerContext.getSessionId()).thenReturn("session-1");
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
    
    @Test
    void assertSQLClassifierBudget() {
        StatementClassifier classifier = new StatementClassifier();
        assertDoesNotThrow(() -> classifier.classify("SELECT * FROM orders WHERE order_id = 1"));
        long elapsedMillis = measureElapsedMillis(() -> {
            for (int i = 0; i < SQL_CLASSIFIER_ITERATIONS; i++) {
                classifier.classify("SELECT * FROM orders WHERE order_id = 1");
            }
        });
        assertWithinBudget("SQL classifier", elapsedMillis, SQL_CLASSIFIER_BUDGET_MILLIS);
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
