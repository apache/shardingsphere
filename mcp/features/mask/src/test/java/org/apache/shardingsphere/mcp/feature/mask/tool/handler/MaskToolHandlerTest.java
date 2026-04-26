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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowPlanningService;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowValidationService;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowExecutionService;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.apache.shardingsphere.mcp.tool.handler.workflow.WorkflowExecutionToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.workflow.WorkflowValidationToolHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaskToolHandlerTest {
    
    @Test
    void assertGetPlanMaskRuleToolDescriptor() {
        MCPToolDescriptor actual = new PlanMaskRuleToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("plan_mask_rule"));
    }
    
    @Test
    void assertHandlePlanMaskRule() throws ReflectiveOperationException {
        PlanMaskRuleToolHandler handler = new PlanMaskRuleToolHandler();
        MaskWorkflowPlanningService planningService = mock(MaskWorkflowPlanningService.class);
        when(planningService.plan(any(), any(), any(), any(), any())).thenReturn(createSnapshot("plan-1", "planned"));
        setField(handler, "planningService", planningService);
        setField(handler, "propertyTemplateService", new MaskAlgorithmPropertyTemplateService());
        RequestContextFixture fixture = createRequestContextFixture();
        MCPResponse actual = handler.handle(fixture.requestContext, "session-1", Map.of(
                "database", "logic_db",
                "table", "orders",
                "column", "phone",
                "algorithm_type", "MASK_FROM_X_TO_Y",
                "structured_intent_evidence", Map.of("field_semantics", "phone"),
                "user_overrides", Map.of("algorithm_type", "KEEP_FIRST_N_LAST_M")));
        assertThat(actual.toPayload().get("plan_id"), is("plan-1"));
        ArgumentCaptor<WorkflowRequest> requestCaptor = ArgumentCaptor.forClass(WorkflowRequest.class);
        verify(planningService).plan(eq(fixture.contextStore), eq(fixture.metadataQueryFacade), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        WorkflowRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.getAlgorithmType(), is("KEEP_FIRST_N_LAST_M"));
        assertThat(actualRequest.getFieldSemantics(), is("phone"));
    }
    
    @Test
    void assertHandlePlanMaskRuleWithMaskedArtifacts() throws ReflectiveOperationException {
        PlanMaskRuleToolHandler handler = new PlanMaskRuleToolHandler();
        MaskWorkflowPlanningService planningService = mock(MaskWorkflowPlanningService.class);
        when(planningService.plan(any(), any(), any(), any(), any())).thenReturn(createDetailedSnapshot());
        setField(handler, "planningService", planningService);
        setField(handler, "propertyTemplateService", new MaskAlgorithmPropertyTemplateService());
        MCPResponse actual = handler.handle(createRequestContextFixture().requestContext, "session-1", Map.of(
                "database", "logic_db",
                "table", "orders",
                "column", "phone"));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(((Map<?, ?>) ((Map<?, ?>) actualPayload.get("masked_property_preview")).get("primary")).get("first-n"), is("3"));
        assertThat(((List<?>) actualPayload.get("ddl_artifacts")).size(), is(0));
        assertThat(((List<?>) actualPayload.get("index_plan")).size(), is(0));
        assertTrue(String.valueOf(((Map<?, ?>) ((List<?>) actualPayload.get("distsql_artifacts")).get(0)).get("sql")).contains("keep_first_n_last_m"));
    }
    
    @Test
    void assertGetValidateMaskRuleToolDescriptor() {
        MCPToolDescriptor actual = new WorkflowValidationToolHandler(MaskFeatureDefinition.VALIDATE_TOOL_NAME,
                (contextStore, metadataQueryFacade, queryFacade, executionFacade, sessionId, planId) -> Map.of()).getToolDescriptor();
        assertThat(actual.getName(), is("validate_mask_rule"));
    }
    
    @Test
    void assertHandleValidateMaskRule() {
        MaskWorkflowValidationService validationService = mock(MaskWorkflowValidationService.class);
        when(validationService.validate(any(), any(), any(), any(), any(), any())).thenReturn(Map.of("status", "validated"));
        WorkflowValidationToolHandler handler = new WorkflowValidationToolHandler(MaskFeatureDefinition.VALIDATE_TOOL_NAME, validationService::validate);
        RequestContextFixture fixture = createRequestContextFixture();
        MCPResponse actual = handler.handle(fixture.requestContext, "session-1", Map.of("plan_id", "plan-1"));
        verify(validationService).validate(eq(fixture.contextStore), eq(fixture.metadataQueryFacade),
                eq(fixture.queryFacade), eq(fixture.executionFacade), eq("session-1"), eq("plan-1"));
        assertThat(actual.toPayload().get("status"), is("validated"));
    }
    
    @Test
    void assertGetApplyMaskRuleToolDescriptor() {
        MCPToolDescriptor actual = new WorkflowExecutionToolHandler(MaskFeatureDefinition.APPLY_TOOL_NAME).getToolDescriptor();
        assertThat(actual.getName(), is("apply_mask_rule"));
    }
    
    @Test
    void assertHandleApplyMaskRule() {
        WorkflowExecutionService executionService = mock(WorkflowExecutionService.class);
        when(executionService.apply(any(), any(), any(), any(), any(), any())).thenReturn(Map.of("status", "completed"));
        WorkflowExecutionToolHandler handler = new WorkflowExecutionToolHandler(MaskFeatureDefinition.APPLY_TOOL_NAME, executionService);
        RequestContextFixture fixture = createRequestContextFixture();
        MCPResponse actual = handler.handle(fixture.requestContext, "session-1", Map.of(
                "plan_id", "plan-1",
                "approved_steps", List.of("rule_distsql"),
                "execution_mode", "review-then-execute"));
        verify(executionService).apply(eq(fixture.contextStore), eq(fixture.executionFacade),
                eq("session-1"), eq("plan-1"), eq(List.of("rule_distsql")), eq("review-then-execute"));
        assertThat(actual.toPayload().get("status"), is("completed"));
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String status) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setStatus(status);
        return result;
    }
    
    private WorkflowContextSnapshot createDetailedSnapshot() {
        WorkflowRequest request = new WorkflowRequest();
        request.getPrimaryAlgorithmProperties().put("first-n", "3");
        request.getPrimaryAlgorithmProperties().put("last-m", "2");
        request.getPrimaryAlgorithmProperties().put("replace-char", "*");
        final WorkflowContextSnapshot result = createSnapshot("plan-1", "planned");
        result.setRequest(request);
        result.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "first-n", true, false, "from", ""));
        result.getRuleArtifacts().add(new RuleArtifact("create", "CREATE MASK RULE orders (TYPE(NAME='keep_first_n_last_m'))"));
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
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private RequestContextFixture createRequestContextFixture() {
        MCPFeatureContext result = mock(MCPFeatureContext.class);
        WorkflowContextStore contextStore = new WorkflowContextStore();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(result.getWorkflowContextStore()).thenReturn(contextStore);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        when(result.getExecutionFacade()).thenReturn(executionFacade);
        return new RequestContextFixture(result, contextStore, metadataQueryFacade, queryFacade, executionFacade);
    }
    
    private record RequestContextFixture(MCPFeatureContext requestContext, WorkflowContextStore contextStore,
                                         MCPMetadataQueryFacade metadataQueryFacade, MCPFeatureQueryFacade queryFacade,
                                         MCPFeatureExecutionFacade executionFacade) {
    }
}
