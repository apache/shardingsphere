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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanShadowRuleToolHandlerTest {
    
    @Test
    void assertHandle() {
        try (
                MockedConstruction<ShadowWorkflowPlanningService> mocked = mockConstruction(ShadowWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planRule(any(), any(), any())).thenReturn(createSnapshot()))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPSuccessPayload actual = new PlanShadowRuleToolHandler().handle(fixture.requestContext, Map.of(
                    "database", "logic_db",
                    "algorithm_type", "SQL_HINT",
                    "structured_intent_evidence", Map.of("rule", "shadow_rule", "table", "t_order")));
            Map<String, Object> actualPayload = actual.toPayload();
            List<?> actualResourcesToRead = (List<?>) actualPayload.get("resources_to_read");
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/shadow/algorithm-plugins"), is("algorithm"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/shadow/databases/logic_db/rules"), is("rule"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/shadow/databases/logic_db/table-rules"), is("rule"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/shadow/databases/logic_db/tables/t_order/rules"), is("rule"));
            ArgumentCaptor<ShadowRuleWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShadowRuleWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planRule(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getTableName(), is("t_order"));
            assertThat(requestCaptor.getValue().getAlgorithmType(), is("SQL_HINT"));
        }
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        ShadowRuleWorkflowRequest request = new ShadowRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("shadow_rule");
        request.setTableName("t_order");
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(WorkflowKind.valueOf(ShadowFeatureDefinition.RULE_WORKFLOW_KIND.getValue()));
        result.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        result.setClarifiedIntent(clarifiedIntent);
        result.setInteractionPlan(InteractionPlan.create("plan-1", request, "Shadow workflow plan.", List.of("review"), List.of("rules")));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE SHADOW RULE `shadow_rule`(SOURCE=`demo_ds`, SHADOW=`demo_ds_shadow`, `t_order`(TYPE(NAME='sql_hint')))"));
        return result;
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
        MCPFeatureRequestContext result = mock(MCPFeatureRequestContext.class);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(result.getSessionIdentity()).thenReturn(new MCPSessionIdentity("session-1", "", "", Map.of()));
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        when(result.getExecutionFacade()).thenReturn(mock(MCPFeatureExecutionFacade.class));
        return new WorkflowContextFixture(result, workflowSessionContext, queryFacade);
    }
    
    private record WorkflowContextFixture(MCPFeatureRequestContext requestContext, WorkflowSessionContext workflowSessionContext,
                                          MCPFeatureQueryFacade queryFacade) {
    }
}
