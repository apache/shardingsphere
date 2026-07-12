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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.feature.broadcast.BroadcastFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.broadcast.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.model.BroadcastWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.service.BroadcastWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanBroadcastRuleToolHandlerTest {
    
    @Test
    void assertHandlePlanBroadcastRule() {
        try (
                MockedConstruction<BroadcastWorkflowPlanningService> mocked = mockConstruction(BroadcastWorkflowPlanningService.class,
                        (mock, context) -> when(mock.plan(any(), any(), any(), any())).thenReturn(createSnapshot("planned")))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPResponse actual = new PlanBroadcastRuleToolHandler().handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of(
                    "database", "logic_db",
                    "tables", "t_order",
                    "structured_intent_evidence", Map.of("tables", "t_order_item"))));
            assertThat(actual.toPayload().get("plan_id"), is("plan-1"));
            ArgumentCaptor<BroadcastWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(BroadcastWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).plan(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getTargetTables(), is(List.of("t_order")));
        }
    }
    
    @Test
    void assertHandlePlanBroadcastRuleWithArtifacts() {
        try (
                MockedConstruction<BroadcastWorkflowPlanningService> ignored = mockConstruction(BroadcastWorkflowPlanningService.class,
                        (mock, context) -> when(mock.plan(any(), any(), any(), any())).thenReturn(createSnapshot("planned")))) {
            MCPResponse actual = new PlanBroadcastRuleToolHandler().handle(createWorkflowContextFixture().workflowContext,
                    new MCPToolCall("session-1", Map.of("database", "logic_db", "table", "t_order")));
            Map<String, Object> actualPayload = actual.toPayload();
            assertFalse(actualPayload.containsKey("ddl_artifacts"));
            assertFalse(actualPayload.containsKey("index_plan"));
            assertTrue(String.valueOf(((Map<?, ?>) ((List<?>) actualPayload.get("distsql_artifacts")).getFirst()).get("sql")).contains("CREATE BROADCAST TABLE RULE"));
            List<?> actualResourcesToRead = (List<?>) actualPayload.get("resources_to_read");
            List<String> actualResourceUris = extractResourceUris(actualResourcesToRead);
            assertTrue(actualResourceUris.contains("shardingsphere://features/broadcast/databases/logic_db/rules"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/broadcast/databases/logic_db/rules"), is("rule"));
            assertFalse(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/t_order/columns"));
            assertThat(((Map<?, ?>) actualPayload.get("proxy_topology_hint")).get("expected_runtime_view"), is("proxy_rule_distsql"));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).getFirst();
            assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
            assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("preview"));
        }
    }
    
    private WorkflowContextSnapshot createSnapshot(final String status) {
        BroadcastWorkflowRequest request = new BroadcastWorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("t_order");
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(BroadcastFeatureDefinition.WORKFLOW_KIND);
        result.setStatus(status);
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        result.setClarifiedIntent(clarifiedIntent);
        result.setInteractionPlan(InteractionPlan.create("plan-1", request, "Broadcast workflow plan.", List.of("review"), List.of("rules")));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE BROADCAST TABLE RULE `t_order`"));
        return result;
    }
    
    private List<String> extractResourceUris(final List<?> resources) {
        return resources.stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
    }
    
    private String findResourceKind(final List<?> resources, final String uri) {
        for (Object each : resources) {
            Map<?, ?> resource = (Map<?, ?>) each;
            if (uri.equals(resource.get("uri"))) {
                return (String) resource.get("resource_kind");
            }
        }
        return "";
    }
    
    private WorkflowContextFixture createWorkflowContextFixture() {
        MCPWorkflowHandlerContext result = mock(MCPWorkflowHandlerContext.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(result.getDatabaseContext()).thenReturn(databaseContext);
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        return new WorkflowContextFixture(result, workflowSessionContext, queryFacade, executionFacade);
    }
    
    private record WorkflowContextFixture(MCPWorkflowHandlerContext workflowContext, WorkflowSessionContext workflowSessionContext,
                                          MCPFeatureQueryFacade queryFacade, MCPFeatureExecutionFacade executionFacade) {
    }
}
