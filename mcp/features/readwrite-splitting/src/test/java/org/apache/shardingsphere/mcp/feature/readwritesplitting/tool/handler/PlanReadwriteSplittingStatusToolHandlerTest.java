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
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingStatusWorkflowPlanningService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanReadwriteSplittingStatusToolHandlerTest {
    
    @Test
    void assertHandle() {
        try (
                MockedConstruction<ReadwriteSplittingStatusWorkflowPlanningService> mocked = mockConstruction(
                        ReadwriteSplittingStatusWorkflowPlanningService.class, (mock, context) -> when(mock.plan(any(), any(), any())).thenReturn(createSnapshot()))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPSuccessPayload actual = new PlanReadwriteSplittingStatusToolHandler().handle(fixture.requestContext, Map.of(
                    "database", "logic_db",
                    "rule", "readwrite_ds",
                    "storage_unit", "read_ds_0",
                    "target_status", "disable",
                    "natural_language_intent", "opaque request text"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actualPayload.get("workflow_kind"), is("readwrite.status"));
            List<?> actualResourcesToRead = (List<?>) actualPayload.get("resources_to_read");
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/readwrite-splitting/databases/logic_db/status"), is("rule"));
            Map<?, ?> actualIntentInference = (Map<?, ?>) actualPayload.get("intent_inference");
            assertFalse(actualIntentInference.containsKey("operation_type"));
            assertThat(actualIntentInference.get("target_status"), is("disable"));
            Map<?, ?> actualArgumentProvenance = (Map<?, ?>) actualPayload.get("argument_provenance");
            assertFalse(actualArgumentProvenance.containsKey("operation_type"));
            assertThat(actualArgumentProvenance.get("target_status"), is("user_provided"));
            Map<?, ?> actualDistSQLArtifact = (Map<?, ?>) ((List<?>) actualPayload.get("distsql_artifacts")).getFirst();
            assertFalse(actualDistSQLArtifact.containsKey("operation_type"));
            assertThat(actualDistSQLArtifact.get("target_status"), is("disable"));
            ArgumentCaptor<ReadwriteSplittingStatusWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ReadwriteSplittingStatusWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).plan(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getStorageUnit(), is("read_ds_0"));
            assertThat(requestCaptor.getValue().getTargetStatus(), is("disable"));
        }
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        ReadwriteSplittingStatusWorkflowRequest request = new ReadwriteSplittingStatusWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setStorageUnit("read_ds_0");
        request.setTargetStatus("disable");
        request.setOperationType("disable");
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(WorkflowKind.valueOf(ReadwriteSplittingFeatureDefinition.STATUS_WORKFLOW_KIND.getValue()));
        result.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        result.setClarifiedIntent(clarifiedIntent);
        result.setInteractionPlan(InteractionPlan.create("plan-1", request, "Readwrite workflow plan.", List.of("review"), List.of("rules")));
        result.getRuleArtifacts().add(new RuleArtifact("disable", "ALTER READWRITE_SPLITTING RULE `readwrite_ds` DISABLE `read_ds_0` FROM `logic_db`"));
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
