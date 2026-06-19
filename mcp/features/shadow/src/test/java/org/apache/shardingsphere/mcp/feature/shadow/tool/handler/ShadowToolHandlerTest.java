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

package org.apache.shardingsphere.mcp.feature.shadow.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowAlgorithmCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowDefaultAlgorithmWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShadowToolHandlerTest {
    
    @Test
    void assertHandlePlanRule() {
        ShadowWorkflowPlanningService planningService = mock(ShadowWorkflowPlanningService.class);
        when(planningService.planRule(any(), any(), any(), any())).thenReturn(createSnapshot(createRuleRequest(), ShadowFeatureDefinition.RULE_WORKFLOW_KIND.getValue()));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        MCPResponse actual = new PlanShadowRuleToolHandler(planningService).handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of(
                "database", "logic_db",
                "structured_intent_evidence", Map.of("rule", "shadow_rule", "table", "t_order"),
                "user_overrides", Map.of("algorithm_type", "SQL_HINT"))));
        assertFalse(actual.toPayload().containsKey("ddl_artifacts"));
        ArgumentCaptor<ShadowRuleWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShadowRuleWorkflowRequest.class);
        verify(planningService).planRule(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getTableName(), is("t_order"));
        assertThat(requestCaptor.getValue().getAlgorithmType(), is("SQL_HINT"));
    }
    
    @Test
    void assertHandlePlanDefaultAlgorithm() {
        ShadowWorkflowPlanningService planningService = mock(ShadowWorkflowPlanningService.class);
        when(planningService.planDefaultAlgorithm(any(), any(), any(), any())).thenReturn(createSnapshot(new ShadowDefaultAlgorithmWorkflowRequest(),
                ShadowFeatureDefinition.DEFAULT_ALGORITHM_WORKFLOW_KIND.getValue()));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        MCPResponse actual = new PlanDefaultShadowAlgorithmToolHandler(planningService)
                .handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of("database", "logic_db", "algorithm_type", "SQL_HINT")));
        ArgumentCaptor<ShadowDefaultAlgorithmWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShadowDefaultAlgorithmWorkflowRequest.class);
        verify(planningService).planDefaultAlgorithm(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getAlgorithmType(), is("SQL_HINT"));
        assertThat(actual.toPayload().get("workflow_kind"), is("shadow.default"));
    }
    
    @Test
    void assertHandlePlanCleanup() {
        ShadowWorkflowPlanningService planningService = mock(ShadowWorkflowPlanningService.class);
        when(planningService.planAlgorithmCleanup(any(), any(), any(), any())).thenReturn(createSnapshot(new ShadowAlgorithmCleanupWorkflowRequest(),
                ShadowFeatureDefinition.ALGORITHM_CLEANUP_WORKFLOW_KIND.getValue()));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        MCPResponse actual = new PlanShadowAlgorithmCleanupToolHandler(planningService)
                .handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of("database", "logic_db", "algorithm_name", "unused_algorithm")));
        ArgumentCaptor<ShadowAlgorithmCleanupWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShadowAlgorithmCleanupWorkflowRequest.class);
        verify(planningService).planAlgorithmCleanup(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getAlgorithmName(), is("unused_algorithm"));
        assertThat(actual.toPayload().get("workflow_kind"), is("shadow.cleanup"));
    }
    
    private ShadowRuleWorkflowRequest createRuleRequest() {
        ShadowRuleWorkflowRequest result = new ShadowRuleWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("shadow_rule");
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final WorkflowRequest request, final String workflowKind) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(WorkflowKind.valueOf(workflowKind));
        result.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        result.setClarifiedIntent(clarifiedIntent);
        result.setInteractionPlan(InteractionPlan.create("plan-1", request, "Shadow workflow plan.", List.of("review"), List.of("rules")));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE SHADOW RULE shadow_rule(SOURCE=demo_ds, SHADOW=demo_ds_shadow, t_order(TYPE(NAME='sql_hint')))"));
        return result;
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
