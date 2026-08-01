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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.handler;

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingRuleWorkflowPlanningService;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanReadwriteSplittingRuleToolHandlerTest {
    
    @Test
    void assertHandle() {
        try (
                MockedConstruction<ReadwriteSplittingRuleWorkflowPlanningService> mocked = mockConstruction(
                        ReadwriteSplittingRuleWorkflowPlanningService.class, (mock, context) -> when(mock.plan(any(), any(), any())).thenReturn(createSnapshot()))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPSuccessPayload actual = new PlanReadwriteSplittingRuleToolHandler().handle(fixture.requestContext, Map.of(
                    "database", "logic_db",
                    "rule", "readwrite_ds",
                    "structured_intent_evidence", Map.of("rule", "inferred_rule", "read_storage_units", "read_ds_1")));
            assertThat(actual.toPayload().get("plan_id"), is("plan-1"));
            ArgumentCaptor<ReadwriteSplittingRuleWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ReadwriteSplittingRuleWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).plan(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getRuleName(), is("readwrite_ds"));
            assertThat(requestCaptor.getValue().getReadStorageUnits(), is(List.of("read_ds_1")));
        }
    }
    
    @Test
    void assertHandleWithTopLevelLoadBalancerProperties() {
        ReadwriteSplittingRuleWorkflowRequest actual = handle(Map.of("database", "logic_db",
                "load_balancer_properties", Map.of("read_ds_0", "2")));
        assertThat(actual.getLoadBalancerProperties(), is(Map.of("read_ds_0", "2")));
    }
    
    @Test
    void assertHandleWithStructuredIntentLoadBalancerProperties() {
        ReadwriteSplittingRuleWorkflowRequest actual = handle(Map.of("database", "logic_db",
                "structured_intent_evidence", Map.of("load_balancer_properties", Map.of("read_ds_0", "2"))));
        assertThat(actual.getLoadBalancerProperties(), is(Map.of("read_ds_0", "2")));
    }
    
    @Test
    void assertHandleWithArtifacts() {
        try (
                MockedConstruction<ReadwriteSplittingRuleWorkflowPlanningService> ignored = mockConstruction(
                        ReadwriteSplittingRuleWorkflowPlanningService.class, (mock, context) -> when(mock.plan(any(), any(), any())).thenReturn(createSnapshot()))) {
            MCPSuccessPayload actual = new PlanReadwriteSplittingRuleToolHandler().handle(
                    createWorkflowContextFixture().requestContext, Map.of("database", "logic_db", "rule", "readwrite_ds"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertTrue(String.valueOf(((Map<?, ?>) ((List<?>) actualPayload.get("distsql_artifacts")).getFirst()).get("sql")).contains("CREATE READWRITE_SPLITTING RULE"));
            List<?> actualResourcesToRead = (List<?>) actualPayload.get("resources_to_read");
            assertTrue(extractResourceUris(actualResourcesToRead).contains("shardingsphere://features/readwrite-splitting/databases/logic_db/rules"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/readwrite-splitting/databases/logic_db/rules"), is("rule"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/readwrite-splitting/load-balance-algorithm-plugins"), is("algorithm"));
            assertThat(((Map<?, ?>) actualPayload.get("proxy_topology_hint")).get("expected_runtime_view"), is("proxy_rule_distsql"));
        }
    }
    
    private ReadwriteSplittingRuleWorkflowRequest handle(final Map<String, Object> arguments) {
        try (
                MockedConstruction<ReadwriteSplittingRuleWorkflowPlanningService> mocked = mockConstruction(
                        ReadwriteSplittingRuleWorkflowPlanningService.class, (mock, context) -> when(mock.plan(any(), any(), any())).thenReturn(createSnapshot()))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            new PlanReadwriteSplittingRuleToolHandler().handle(fixture.requestContext, arguments);
            ArgumentCaptor<ReadwriteSplittingRuleWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ReadwriteSplittingRuleWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).plan(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            return requestCaptor.getValue();
        }
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(WorkflowKind.valueOf(ReadwriteSplittingFeatureDefinition.RULE_WORKFLOW_KIND.getValue()));
        result.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        result.setClarifiedIntent(clarifiedIntent);
        result.setInteractionPlan(InteractionPlan.create("plan-1", request, "Readwrite workflow plan.", List.of("review"), List.of("rules")));
        result.getRuleArtifacts().add(new RuleArtifact("create",
                "CREATE READWRITE_SPLITTING RULE `readwrite_ds` (WRITE_STORAGE_UNIT=`write_ds`, READ_STORAGE_UNITS(`read_ds_0`), TRANSACTIONAL_READ_QUERY_STRATEGY='DYNAMIC')"));
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
