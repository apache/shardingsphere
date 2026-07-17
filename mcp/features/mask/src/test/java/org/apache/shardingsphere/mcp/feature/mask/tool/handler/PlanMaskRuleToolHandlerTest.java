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

package org.apache.shardingsphere.mcp.feature.mask.tool.handler;

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
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

class PlanMaskRuleToolHandlerTest {
    
    @Test
    void assertHandlePlanMaskRule() {
        try (MockedConstruction<MaskWorkflowPlanningService> mockedConstruction = mockConstruction(MaskWorkflowPlanningService.class)) {
            PlanMaskRuleToolHandler handler = new PlanMaskRuleToolHandler();
            MaskWorkflowPlanningService planningService = mockedConstruction.constructed().getFirst();
            when(planningService.plan(any(), any(), any(), any())).thenReturn(createSnapshot("plan-1", "planned"));
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPSuccessPayload actual = handler.handle(fixture.requestContext, Map.of(
                    "database", "logic_db",
                    "table", "orders",
                    "column", "phone",
                    "algorithm_type", "KEEP_FIRST_N_LAST_M",
                    "structured_intent_evidence", Map.of("field_semantics", "phone")));
            assertThat(actual.toPayload().get("plan_id"), is("plan-1"));
            ArgumentCaptor<WorkflowRequest> requestCaptor = ArgumentCaptor.forClass(WorkflowRequest.class);
            verify(planningService).plan(eq(fixture.workflowSessionContext), eq(fixture.metadataQueryFacade), eq(fixture.queryFacade), requestCaptor.capture());
            WorkflowRequest actualRequest = requestCaptor.getValue();
            assertThat(actualRequest.getAlgorithmType(), is("KEEP_FIRST_N_LAST_M"));
            assertThat(actualRequest.getFieldSemantics(), is("phone"));
        }
    }
    
    @Test
    void assertHandlePlanMaskRuleWithMaskedArtifacts() {
        try (MockedConstruction<MaskWorkflowPlanningService> mockedConstruction = mockConstruction(MaskWorkflowPlanningService.class)) {
            PlanMaskRuleToolHandler handler = new PlanMaskRuleToolHandler();
            when(mockedConstruction.constructed().getFirst().plan(any(), any(), any(), any())).thenReturn(createDetailedSnapshot());
            MCPSuccessPayload actual = handler.handle(createWorkflowContextFixture().requestContext, Map.of(
                    "database", "logic_db",
                    "table", "orders",
                    "column", "phone"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(((Map<?, ?>) ((Map<?, ?>) actualPayload.get("masked_property_preview")).get("primary")).get("first-n"), is("3"));
            assertTrue(String.valueOf(((Map<?, ?>) ((List<?>) actualPayload.get("distsql_artifacts")).getFirst()).get("sql")).contains("keep_first_n_last_m"));
            List<?> actualResourcesToRead = (List<?>) actualPayload.get("resources_to_read");
            List<String> actualResourceUris = extractResourceUris(actualResourcesToRead);
            assertTrue(actualResourceUris.contains("shardingsphere://features/mask/algorithms"));
            assertTrue(actualResourceUris.contains("shardingsphere://features/mask/databases/logic_db/rules"));
            assertTrue(actualResourceUris.contains("shardingsphere://features/mask/databases/logic_db/tables/orders/rules"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/mask/algorithms"), is("algorithm"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/mask/databases/logic_db/rules"), is("rule"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/mask/databases/logic_db/tables/orders/rules"), is("rule"));
            assertFalse(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns"));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).getFirst();
            assertThat(actualNextAction.get("type"), is("tool_call"));
            assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
            assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("plan_id"), is("plan-1"));
            assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("preview"));
        }
    }
    
    @Test
    void assertHandlePlanMaskRuleWithSecretReference() {
        try (MockedConstruction<MaskWorkflowPlanningService> mockedConstruction = mockConstruction(MaskWorkflowPlanningService.class)) {
            PlanMaskRuleToolHandler handler = new PlanMaskRuleToolHandler();
            MaskWorkflowPlanningService planningService = mockedConstruction.constructed().getFirst();
            when(planningService.plan(any(), any(), any(), any())).thenReturn(createSnapshot("plan-1", "planned"));
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            handler.handle(fixture.requestContext, Map.of(
                    "database", "logic_db",
                    "primary_algorithm_properties", Map.of("replace-char", Map.of("secret_ref", "placeholder://secret-value-1"))));
            ArgumentCaptor<WorkflowRequest> requestCaptor = ArgumentCaptor.forClass(WorkflowRequest.class);
            verify(planningService).plan(eq(fixture.workflowSessionContext), eq(fixture.metadataQueryFacade), eq(fixture.queryFacade), requestCaptor.capture());
            WorkflowRequest actualRequest = requestCaptor.getValue();
            assertThat(actualRequest.getPrimaryAlgorithmProperties().get("replace-char"), is("secret_reference:primary.replace-char"));
            assertFalse(actualRequest.getSecretReferences("primary").get("replace-char").isMalformed());
        }
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String status) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setWorkflowKind(MaskFeatureDefinition.WORKFLOW_KIND);
        result.setStatus(status);
        result.setRequest(new WorkflowRequest());
        result.setClarifiedIntent(new ClarifiedIntent());
        result.setInteractionPlan(createInteractionPlan());
        return result;
    }
    
    private WorkflowContextSnapshot createDetailedSnapshot() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.getPrimaryAlgorithmProperties().put("first-n", "3");
        request.getPrimaryAlgorithmProperties().put("last-m", "2");
        request.getPrimaryAlgorithmProperties().put("replace-char", "*");
        WorkflowContextSnapshot result = createSnapshot("plan-1", "planned");
        result.setRequest(request);
        result.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "first-n", true, false, "from", ""));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE `orders` (TYPE(NAME='keep_first_n_last_m'))"));
        result.setInteractionPlan(createInteractionPlan());
        return result;
    }
    
    private InteractionPlan createInteractionPlan() {
        InteractionPlan result = new InteractionPlan();
        result.setCurrentStep("review");
        result.setDeliveryMode("interactive");
        result.setExecutionMode("review-then-execute");
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
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(result.getSessionId()).thenReturn("session-1");
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        when(result.getExecutionFacade()).thenReturn(executionFacade);
        return new WorkflowContextFixture(result, workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade);
    }
    
    private record WorkflowContextFixture(MCPFeatureRequestContext requestContext, WorkflowSessionContext workflowSessionContext,
                                          MCPMetadataQueryFacade metadataQueryFacade, MCPFeatureQueryFacade queryFacade, MCPFeatureExecutionFacade executionFacade) {
    }
}
