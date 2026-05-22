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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.IndexPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptToolHandlerTest {
    
    @Test
    void assertHandlePlanEncryptRule() throws ReflectiveOperationException {
        PlanEncryptRuleToolHandler handler = new PlanEncryptRuleToolHandler();
        EncryptWorkflowPlanningService planningService = mock(EncryptWorkflowPlanningService.class);
        when(planningService.plan(any(), any(), any(), any(), any())).thenReturn(createSnapshot("plan-1", "planned"));
        setField(handler, "planningService", planningService);
        setField(handler, "propertyTemplateService", new EncryptAlgorithmPropertyTemplateService());
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        MCPResponse actual = handler.handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of(
                "database", "logic_db",
                "table", "orders",
                "column", "phone",
                "allow_index_ddl", false,
                "algorithm_type", "AES",
                "structured_intent_evidence", Map.of("field_semantics", "phone", "requires_decrypt", true),
                "user_overrides", Map.of("cipher_column_name", "phone_cipher"))));
        assertThat(actual.toPayload().get("plan_id"), is("plan-1"));
        ArgumentCaptor<EncryptWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(EncryptWorkflowRequest.class);
        verify(planningService).plan(eq(fixture.workflowSessionContext), eq(fixture.metadataQueryFacade), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        EncryptWorkflowRequest actualRequest = requestCaptor.getValue();
        assertFalse(actualRequest.getOptions().getAllowIndexDDL());
        assertThat(actualRequest.getAlgorithmType(), is("AES"));
        assertThat(actualRequest.getFieldSemantics(), is("phone"));
        assertThat(actualRequest.getOptions().getCipherColumnName(), is("phone_cipher"));
    }
    
    @Test
    void assertHandlePlanEncryptRuleWithMaskedArtifacts() throws ReflectiveOperationException {
        PlanEncryptRuleToolHandler handler = new PlanEncryptRuleToolHandler();
        EncryptWorkflowPlanningService planningService = mock(EncryptWorkflowPlanningService.class);
        when(planningService.plan(any(), any(), any(), any(), any())).thenReturn(createDetailedSnapshot());
        setField(handler, "planningService", planningService);
        setField(handler, "propertyTemplateService", new EncryptAlgorithmPropertyTemplateService());
        MCPResponse actual = handler.handle(createWorkflowContextFixture().workflowContext, new MCPToolCall("session-1", Map.of(
                "database", "logic_db",
                "table", "orders",
                "column", "phone")));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(((Map<?, ?>) ((Map<?, ?>) actualPayload.get("masked_property_preview")).get("primary")).get("aes-key-value"), is("******"));
        assertThat(((Map<?, ?>) actualPayload.get("derived_column_plan")).get("cipher_column_name"), is("phone_cipher"));
        assertThat(((List<?>) actualPayload.get("ddl_artifacts")).size(), is(1));
        assertThat(((List<?>) actualPayload.get("index_plan")).size(), is(1));
        assertTrue(String.valueOf(((Map<?, ?>) ((List<?>) actualPayload.get("distsql_artifacts")).get(0)).get("sql")).contains("******"));
        List<String> actualResourceUris = extractResourceUris((List<?>) actualPayload.get("resources_to_read"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/encrypt/algorithms"));
        assertTrue(actualResourceUris.contains("shardingsphere://features/encrypt/databases/logic_db/rules"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns"));
        assertTrue(actualResourceUris.contains("shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualPayload.get("next_actions")).get(0);
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
        assertFalse(actualNextAction.containsKey("requires_user_approval"));
        assertFalse(actualNextAction.containsKey("required_arguments"));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("plan_id"), is("plan-1"));
        assertThat(((Map<?, ?>) actualNextAction.get("arguments")).get("execution_mode"), is("preview"));
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
    
    private WorkflowContextSnapshot createDetailedSnapshot() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        WorkflowContextSnapshot result = createSnapshot("plan-1", "planned");
        result.setRequest(request);
        EncryptWorkflowState workflowState = new EncryptWorkflowState();
        workflowState.setDerivedColumnPlan(createDerivedColumnPlan());
        result.setFeatureData(workflowState);
        result.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", ""));
        result.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN phone_cipher VARCHAR(32)", 10));
        result.getIndexPlans().add(new IndexPlan("idx_phone_assisted", "phone_assisted_query", "assist", "CREATE INDEX idx_phone_assisted ON orders(phone_assisted_query)"));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE orders (PROPERTIES('aes-key-value'='123456'))"));
        result.setInteractionPlan(createInteractionPlan());
        return result;
    }
    
    private DerivedColumnPlan createDerivedColumnPlan() {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setLogicalColumn("phone");
        result.setCipherColumnName("phone_cipher");
        result.setCipherColumnRequired(true);
        return result;
    }
    
    private InteractionPlan createInteractionPlan() {
        InteractionPlan result = new InteractionPlan();
        result.setCurrentStep("review");
        result.setDeliveryMode("interactive");
        result.setExecutionMode("review-then-execute");
        return result;
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private List<String> extractResourceUris(final List<?> resources) {
        return resources.stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
    }
    
    private WorkflowContextFixture createWorkflowContextFixture() {
        MCPWorkflowHandlerContext result = mock(MCPWorkflowHandlerContext.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(result.getDatabaseContext()).thenReturn(databaseContext);
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(databaseContext.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        return new WorkflowContextFixture(result, workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade);
    }
    
    private record WorkflowContextFixture(MCPWorkflowHandlerContext workflowContext, WorkflowSessionContext workflowSessionContext,
                                          MCPMetadataQueryFacade metadataQueryFacade, MCPFeatureQueryFacade queryFacade,
                                          MCPFeatureExecutionFacade executionFacade) {
    }
}
