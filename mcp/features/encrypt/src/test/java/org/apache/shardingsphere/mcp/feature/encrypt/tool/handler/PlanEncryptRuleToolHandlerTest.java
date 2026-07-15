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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
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

class PlanEncryptRuleToolHandlerTest {
    
    @Test
    void assertHandlePlanEncryptRule() {
        try (MockedConstruction<EncryptWorkflowPlanningService> mockedConstruction = mockConstruction(EncryptWorkflowPlanningService.class)) {
            PlanEncryptRuleToolHandler handler = new PlanEncryptRuleToolHandler();
            EncryptWorkflowPlanningService planningService = mockedConstruction.constructed().getFirst();
            when(planningService.plan(any(), any(), any(), any())).thenReturn(createSnapshot("plan-1", "planned"));
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPSuccessPayload actual = handler.handle(fixture.workflowContext, Map.of(
                    "database", "logic_db",
                    "table", "orders",
                    "column", "phone",
                    "algorithm_type", "AES",
                    "cipher_column_name", "phone_cipher",
                    "structured_intent_evidence", Map.of("field_semantics", "phone", "requires_decrypt", true)));
            assertThat(actual.toPayload().get("plan_id"), is("plan-1"));
            ArgumentCaptor<EncryptWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(EncryptWorkflowRequest.class);
            verify(planningService).plan(eq(fixture.workflowSessionContext), eq(fixture.metadataQueryFacade), eq(fixture.queryFacade), requestCaptor.capture());
            EncryptWorkflowRequest actualRequest = requestCaptor.getValue();
            assertThat(actualRequest.getAlgorithmType(), is("AES"));
            assertThat(actualRequest.getFieldSemantics(), is("phone"));
            assertThat(actualRequest.getOptions().getCipherColumnName(), is("phone_cipher"));
        }
    }
    
    @Test
    void assertHandlePlanEncryptRuleWithMaskedArtifacts() {
        try (MockedConstruction<EncryptWorkflowPlanningService> mockedConstruction = mockConstruction(EncryptWorkflowPlanningService.class)) {
            PlanEncryptRuleToolHandler handler = new PlanEncryptRuleToolHandler();
            when(mockedConstruction.constructed().getFirst().plan(any(), any(), any(), any())).thenReturn(createDetailedSnapshot());
            MCPSuccessPayload actual = handler.handle(createWorkflowContextFixture().workflowContext, Map.of(
                    "database", "logic_db",
                    "table", "orders",
                    "column", "phone"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(((Map<?, ?>) ((Map<?, ?>) actualPayload.get("masked_property_preview")).get("primary")).get("aes-key-value"), is("******"));
            assertFalse(actualPayload.containsKey("derived_column_plan"));
            assertFalse(actualPayload.containsKey("ddl_artifacts"));
            assertFalse(actualPayload.containsKey("index_plan"));
            assertTrue(String.valueOf(((Map<?, ?>) ((List<?>) actualPayload.get("distsql_artifacts")).getFirst()).get("sql")).contains("******"));
            List<?> actualResourcesToRead = (List<?>) actualPayload.get("resources_to_read");
            List<String> actualResourceUris = extractResourceUris(actualResourcesToRead);
            assertTrue(actualResourceUris.contains("shardingsphere://features/encrypt/algorithms"));
            assertTrue(actualResourceUris.contains("shardingsphere://features/encrypt/databases/logic_db/rules"));
            assertTrue(actualResourceUris.contains("shardingsphere://features/encrypt/databases/logic_db/tables/orders/rules"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/encrypt/algorithms"), is("algorithm"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/encrypt/databases/logic_db/rules"), is("rule"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/encrypt/databases/logic_db/tables/orders/rules"), is("rule"));
            Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).getFirst();
            assertThat(actualNextAction.get("type"), is("tool_call"));
            assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
            assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("plan_id"), is("plan-1"));
            assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("preview"));
        }
    }
    
    @Test
    void assertHandlePlanEncryptRuleWithSecretReferences() {
        try (MockedConstruction<EncryptWorkflowPlanningService> mockedConstruction = mockConstruction(EncryptWorkflowPlanningService.class)) {
            PlanEncryptRuleToolHandler handler = new PlanEncryptRuleToolHandler();
            EncryptWorkflowPlanningService planningService = mockedConstruction.constructed().getFirst();
            when(planningService.plan(any(), any(), any(), any())).thenReturn(createSnapshot("plan-1", "planned"));
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            handler.handle(fixture.workflowContext, Map.of(
                    "database", "logic_db",
                    "primary_algorithm_properties", Map.of("aes-key-value", Map.of("secret_ref", "placeholder://secret-value-1")),
                    "assisted_query_algorithm_properties", Map.of("salt", Map.of("secret_ref", "placeholder://secret-value-2")),
                    "like_query_algorithm_properties", Map.of("token", Map.of("secret_ref", "placeholder://secret-value-3"))));
            ArgumentCaptor<EncryptWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(EncryptWorkflowRequest.class);
            verify(planningService).plan(eq(fixture.workflowSessionContext), eq(fixture.metadataQueryFacade), eq(fixture.queryFacade), requestCaptor.capture());
            EncryptWorkflowRequest actualRequest = requestCaptor.getValue();
            assertThat(actualRequest.getPrimaryAlgorithmProperties().get("aes-key-value"), is("secret_reference:primary.aes-key-value"));
            assertFalse(actualRequest.getSecretReferences("primary").get("aes-key-value").isMalformed());
            assertThat(actualRequest.getOptions().getAssistedQueryAlgorithmProperties().get("salt"), is("secret_reference:assisted_query.salt"));
            assertFalse(actualRequest.getSecretReferences("assisted_query").get("salt").isMalformed());
            assertThat(actualRequest.getOptions().getLikeQueryAlgorithmProperties().get("token"), is("secret_reference:like_query.token"));
            assertFalse(actualRequest.getSecretReferences("like_query").get("token").isMalformed());
        }
    }
    
    @Test
    void assertHandlePlanEncryptRuleMasksPropertiesBeforeRequirementsCollected() {
        try (MockedConstruction<EncryptWorkflowPlanningService> mockedConstruction = mockConstruction(EncryptWorkflowPlanningService.class)) {
            PlanEncryptRuleToolHandler handler = new PlanEncryptRuleToolHandler();
            when(mockedConstruction.constructed().getFirst().plan(any(), any(), any(), any())).thenReturn(createClarifyingSnapshot());
            MCPSuccessPayload actual = handler.handle(createWorkflowContextFixture().workflowContext, Map.of(
                    "database", "logic_db",
                    "table", "orders",
                    "column", "phone"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(((Map<?, ?>) ((Map<?, ?>) actualPayload.get("masked_property_preview")).get("primary")).get("aes-key-value"), is("******"));
            assertFalse(String.valueOf(actualPayload).contains("recovery-secret-value"));
        }
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String status) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setWorkflowKind(EncryptFeatureDefinition.WORKFLOW_KIND);
        result.setStatus(status);
        result.setRequest(new EncryptWorkflowRequest());
        result.setClarifiedIntent(new ClarifiedIntent());
        result.setFeatureData(new EncryptWorkflowState());
        result.setInteractionPlan(createInteractionPlan());
        return result;
    }
    
    private WorkflowContextSnapshot createClarifyingSnapshot() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setAlgorithmType("AES");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "recovery-secret-value");
        WorkflowContextSnapshot result = createSnapshot("plan-1", "clarifying");
        result.setRequest(request);
        return result;
    }
    
    private WorkflowContextSnapshot createDetailedSnapshot() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("phone");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        WorkflowContextSnapshot result = createSnapshot("plan-1", "planned");
        result.setRequest(request);
        result.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", ""));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE `orders` (PROPERTIES('aes-key-value'='123456'))"));
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
        MCPWorkflowRequestContext result = mock(MCPWorkflowRequestContext.class);
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
    
    private record WorkflowContextFixture(MCPWorkflowRequestContext workflowContext, WorkflowSessionContext workflowSessionContext,
                                          MCPMetadataQueryFacade metadataQueryFacade, MCPFeatureQueryFacade queryFacade, MCPFeatureExecutionFacade executionFacade) {
    }
}
