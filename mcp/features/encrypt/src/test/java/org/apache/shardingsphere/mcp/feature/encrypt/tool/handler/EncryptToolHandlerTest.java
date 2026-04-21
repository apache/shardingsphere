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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowPlanningService;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowValidationService;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowExecutionService;
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
    void assertGetPlanEncryptRuleToolDescriptor() {
        MCPToolDescriptor actual = new PlanEncryptRuleToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("plan_encrypt_rule"));
    }
    
    @Test
    void assertHandlePlanEncryptRule() throws ReflectiveOperationException {
        PlanEncryptRuleToolHandler handler = new PlanEncryptRuleToolHandler();
        EncryptWorkflowPlanningService planningService = mock(EncryptWorkflowPlanningService.class);
        when(planningService.plan(any(), any(), any())).thenReturn(createSnapshot("plan-1", "planned"));
        setField(handler, "planningService", planningService);
        setField(handler, "propertyTemplateService", new EncryptAlgorithmPropertyTemplateService());
        MCPResponse actual = handler.handle(mock(MCPFeatureContext.class), "session-1", Map.of(
                "database", "logic_db",
                "table", "orders",
                "column", "phone",
                "allow_index_ddl", false,
                "algorithm_type", "AES",
                "structured_intent_evidence", Map.of("field_semantics", "phone", "requires_decrypt", true),
                "user_overrides", Map.of("cipher_column_name", "phone_cipher")));
        assertThat(actual.toPayload().get("plan_id"), is("plan-1"));
        ArgumentCaptor<EncryptWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(EncryptWorkflowRequest.class);
        verify(planningService).plan(any(), eq("session-1"), requestCaptor.capture());
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
        when(planningService.plan(any(), any(), any())).thenReturn(createDetailedSnapshot());
        setField(handler, "planningService", planningService);
        setField(handler, "propertyTemplateService", new EncryptAlgorithmPropertyTemplateService());
        MCPResponse actual = handler.handle(mock(MCPFeatureContext.class), "session-1", Map.of(
                "database", "logic_db",
                "table", "orders",
                "column", "phone"));
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(((Map<?, ?>) ((Map<?, ?>) actualPayload.get("masked_property_preview")).get("primary")).get("aes-key-value"), is("******"));
        assertThat(((Map<?, ?>) actualPayload.get("derived_column_plan")).get("cipher_column_name"), is("phone_cipher"));
        assertThat(((List<?>) actualPayload.get("ddl_artifacts")).size(), is(1));
        assertThat(((List<?>) actualPayload.get("index_plan")).size(), is(1));
        assertTrue(String.valueOf(((Map<?, ?>) ((List<?>) actualPayload.get("distsql_artifacts")).get(0)).get("sql")).contains("******"));
    }
    
    @Test
    void assertGetValidateEncryptRuleToolDescriptor() {
        MCPToolDescriptor actual = new ValidateEncryptRuleToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("validate_encrypt_rule"));
    }
    
    @Test
    void assertHandleValidateEncryptRule() throws ReflectiveOperationException {
        ValidateEncryptRuleToolHandler handler = new ValidateEncryptRuleToolHandler();
        EncryptWorkflowValidationService validationService = mock(EncryptWorkflowValidationService.class);
        when(validationService.validate(any(), any(), any())).thenReturn(Map.of("status", "validated"));
        setField(handler, "validationService", validationService);
        MCPResponse actual = handler.handle(mock(MCPFeatureContext.class), "session-1", Map.of("plan_id", "plan-1"));
        verify(validationService).validate(any(), eq("session-1"), eq("plan-1"));
        assertThat(actual.toPayload().get("status"), is("validated"));
    }
    
    @Test
    void assertGetApplyEncryptRuleToolDescriptor() {
        MCPToolDescriptor actual = new ApplyEncryptRuleToolHandler().getToolDescriptor();
        assertThat(actual.getName(), is("apply_encrypt_rule"));
    }
    
    @Test
    void assertHandleApplyEncryptRule() throws ReflectiveOperationException {
        ApplyEncryptRuleToolHandler handler = new ApplyEncryptRuleToolHandler();
        WorkflowExecutionService executionService = mock(WorkflowExecutionService.class);
        when(executionService.apply(any(), any(), any(), any(), any())).thenReturn(Map.of("status", "completed"));
        setField(handler, "executionService", executionService);
        MCPResponse actual = handler.handle(mock(MCPFeatureContext.class), "session-1", Map.of(
                "plan_id", "plan-1",
                "approved_steps", List.of("ddl", "rule_distsql"),
                "execution_mode", "manual-only"));
        verify(executionService).apply(any(), eq("session-1"), eq("plan-1"), eq(List.of("ddl", "rule_distsql")), eq("manual-only"));
        assertThat(actual.toPayload().get("status"), is("completed"));
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String status) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setStatus(status);
        return result;
    }
    
    private WorkflowContextSnapshot createDetailedSnapshot() {
        WorkflowContextSnapshot result = createSnapshot("plan-1", "planned");
        WorkflowRequest request = new WorkflowRequest();
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
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
}
