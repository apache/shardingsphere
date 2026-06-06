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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleWorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EncryptWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRejectsDifferentSession() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot("plan-1", "session-1", "executed", "create"));
        Map<String, Object> actual = createService(mock(EncryptRuleInspectionService.class))
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-2",
                        workflowSessionContext.getRequired("plan-1"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateHappyPath() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setColumn("Phone");
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(createRuleRow()));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.getDatabaseType("logic_db")).thenReturn("PostgreSQL");
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(actual.get("overall_status"), is("passed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
        assertFalse(actual.containsKey("ddl_validation"));
        assertFalse(actual.containsKey("logical_metadata_validation"));
        assertFalse(actual.containsKey("sql_executability_validation"));
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertSynchronize() throws ReflectiveOperationException {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(createRuleRow()));
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        createService(ruleInspectionService).synchronize(snapshot, metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade, "session-1");
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertSynchronizeWhenStateDoesNotConverge() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        WorkflowSynchronizationException actual = assertThrows(WorkflowSynchronizationException.class,
                () -> createService(ruleInspectionService).synchronize(snapshot, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                        mock(MCPFeatureExecutionFacade.class), "session-1"));
        assertThat(actual.getIssueCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertValidateDropWorkflowAfterRuleRemoval() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop");
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWhenRuleMissing() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(actual.get("overall_status"), is("failed"));
    }
    
    @Test
    void assertValidateWhenRuleMappingMismatch() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher_old",
                "encryptor_type", "AES")));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).get(0)).get("expected"), is("cipher_column=phone_cipher"));
    }
    
    @Test
    void assertValidateExpectedStateMasksSecretPropertyMismatch() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", ""));
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "encryptor_type", "AES",
                "encryptor_props", Map.of("aes-key-value", "123456")))));
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "encryptor_type", "AES",
                "encryptor_props", Map.of("aes-key-value", "old-key"))));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertFalse(String.valueOf(actual.get("mismatches")).contains("123456"));
        assertFalse(String.valueOf(actual.get("mismatches")).contains("old-key"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).get(0)).get("expected"), is("encryptor_props={aes-key-value=******}"));
    }
    
    @Test
    void assertValidateExpectedStateDetectsMissingNonTargetRule() throws ReflectiveOperationException {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(
                createRuleRow(),
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES"))));
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(createRuleRow()));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).get(0)).get("expected"), is("logic_column=email"));
    }
    
    private EncryptWorkflowValidationService createService(final EncryptRuleInspectionService ruleInspectionService) throws ReflectiveOperationException {
        EncryptWorkflowValidationService result = new EncryptWorkflowValidationService();
        setField(result, "ruleInspectionService", ruleInspectionService);
        setField(result, "workflowSynchronizationSupport", new WorkflowSynchronizationSupport(1, 0L));
        return result;
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status, final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        result.setWorkflowKind(EncryptFeatureDefinition.WORKFLOW_KIND);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        result.setRequest(createRequest());
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    private EncryptWorkflowRequest createRequest() {
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
        result.setDatabase("logic_db");
        result.setSchema("public");
        result.setTable("orders");
        result.setColumn("phone");
        result.setAlgorithmType("AES");
        result.getOptions().setCipherColumnName("phone_cipher");
        result.getOptions().setRequiresEqualityFilter(false);
        result.getOptions().setRequiresLikeQuery(false);
        return result;
    }
    
    private Map<String, Object> createRuleRow() {
        return Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "encryptor_type", "AES");
    }
}
