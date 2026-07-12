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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

class EncryptWorkflowValidationServiceTest {
    
    private MockedStatic<TypedSPILoader> typedSPILoader;
    
    @BeforeEach
    void setUp() {
        typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
    }
    
    @AfterEach
    void tearDown() {
        typedSPILoader.close();
    }
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot("plan-1", "session-1", "executed", "create"));
        Map<String, Object> actual = createService(mock(EncryptRuleInspectionService.class))
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-2",
                        workflowSessionContext.getRequired("plan-1"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).getFirst()).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateHappyPath() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(createRuleRow()));
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
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
    void assertValidateApplyArtifactsRejectsInvalidGeneratedEncryptRuleArtifact() {
        String sql = "CREATE ENCRYPT RULE t_user (COLUMNS((NAME=name, CIPHER=name_cipher, "
                + "ENCRYPT_ALGORITHM(TYPE(NAME=AES, PROPERTIES('aes-key-value'='123456'))))))";
        List<Map<String, Object>> actual = new EncryptWorkflowValidationService().validate(new WorkflowContextSnapshot(),
                List.of(createRuleDistSQLArtifact(sql, sql.replace("123456", "******"))));
        assertThat(actual.size(), is(3));
        assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        assertThat(actual.getFirst().get("message"), is("Generated encrypt DistSQL uses reserved logical column identifier `name` without DistSQL quoting."));
        assertFalse(String.valueOf(actual).contains("123456"));
    }
    
    @Test
    void assertValidateApplyArtifactsAllowsNamePrefixColumn() {
        String sql = "CREATE ENCRYPT RULE t_user (COLUMNS((NAME=name_cipher, CIPHER=name_cipher_value, "
                + "ENCRYPT_ALGORITHM(TYPE(NAME='aes', PROPERTIES('aes-key-value'='123456', 'digest-algorithm-name'='SHA-1'))))))";
        List<Map<String, Object>> actual = new EncryptWorkflowValidationService().validate(new WorkflowContextSnapshot(),
                List.of(createRuleDistSQLArtifact(sql, sql.replace("123456", "******"))));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertValidateApplyArtifactsRejectsUnavailableEncryptAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setAlgorithmType("AES");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "raw-secret");
        request.getPrimaryAlgorithmProperties().put("digest-algorithm-name", "SHA-1");
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setRequest(request);
        String sql = "CREATE ENCRYPT RULE `t_user` (COLUMNS((NAME=`phone`, CIPHER=`phone_cipher`, "
                + "ENCRYPT_ALGORITHM(TYPE(NAME='aes', PROPERTIES('aes-key-value'='******', 'digest-algorithm-name'='SHA-1'))))))";
        typedSPILoader.when(() -> TypedSPILoader.checkService(EncryptAlgorithm.class, "AES", WorkflowSQLUtils.createProperties(request.getPrimaryAlgorithmProperties())))
                .thenThrow(new IllegalArgumentException("raw-secret"));
        List<Map<String, Object>> actual = new EncryptWorkflowValidationService().validate(snapshot, List.of(createRuleDistSQLArtifact(sql, sql)));
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().get("message"), is("Generated encrypt DistSQL references encrypt algorithm `AES`, "
                + "but it cannot be loaded or initialized by EncryptAlgorithm SPI."));
        assertFalse(String.valueOf(actual).contains("raw-secret"));
    }
    
    @Test
    void assertValidateApplyArtifactsIgnoresNonEncryptRuleArtifacts() {
        String sql = "CREATE MASK RULE orders (COLUMNS((NAME=name, "
                + "MASK_ALGORITHM(TYPE(NAME=AES, PROPERTIES('description'='CREATE ENCRYPT RULE', 'aes-key-value'='123456'))))))";
        List<Map<String, Object>> actual = new EncryptWorkflowValidationService().validate(new WorkflowContextSnapshot(), List.of(createRuleDistSQLArtifact(sql, sql)));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertSynchronize() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(createRuleRow()));
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        createService(ruleInspectionService).synchronize(snapshot, metadataQueryFacade, createQueryFacade(), executionFacade, "session-1");
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertValidateDropWorkflowAfterRuleRemoval() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop");
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWhenRuleMissing() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(actual.get("overall_status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("code"), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertValidateWhenRuleMappingMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher_old",
                "encryptor_type", "AES")));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("expected"), is("cipher_column=phone_cipher"));
    }
    
    @Test
    void assertValidateExpectedStateMasksSecretPropertyMismatch() {
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
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertFalse(String.valueOf(actual.get("mismatches")).contains("123456"));
        assertFalse(String.valueOf(actual.get("mismatches")).contains("old-key"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("expected"), is("encryptor_props={aes-key-value=******}"));
    }
    
    @Test
    void assertValidateExpectedStateAcceptsResolvedReferencedProperty() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().getPrimaryAlgorithmProperties().put("aes-key-value", "secret_reference:primary.aes-key-value");
        snapshot.getRequest().getPrimaryAlgorithmSecretReferences().put("aes-key-value", SecretReferenceValue.create());
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "encryptor_type", "AES",
                "encryptor_props", Map.of("aes-key-value", "secret_reference:primary.aes-key-value")))));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "encryptor_type", "AES",
                "encryptor_props", Map.of("aes-key-value", "raw-actual-secret"))));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(actual.get("overall_status"), is("passed"));
        assertThat(((List<?>) actual.get("mismatches")).size(), is(0));
        assertFalse(String.valueOf(actual).contains("raw-actual-secret"));
        assertFalse(String.valueOf(actual).contains("placeholder://secret-value-1"));
    }
    
    @Test
    void assertValidateExpectedStateDetectsUnresolvedReferencedProperty() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().getPrimaryAlgorithmProperties().put("aes-key-value", "secret_reference:primary.aes-key-value");
        snapshot.getRequest().getPrimaryAlgorithmSecretReferences().put("aes-key-value", SecretReferenceValue.create());
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "encryptor_type", "AES",
                "encryptor_props", Map.of("aes-key-value", "secret_reference:primary.aes-key-value")))));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "encryptor_type", "AES",
                "encryptor_props", Map.of("aes-key-value", "secret_reference:primary.aes-key-value"))));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("expected"), is("encryptor_props={aes-key-value=******}"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("actual"), is("encryptor_props={aes-key-value=******}"));
        assertFalse(String.valueOf(actual).contains("placeholder://secret-value-1"));
    }
    
    @Test
    void assertValidateExpectedStateDetectsMissingNonTargetRule() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(
                createRuleRow(),
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES"))));
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(createRuleRow()));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("expected"), is("logic_column=email"));
    }
    
    @Test
    void assertValidateExpectedStateDetectsUnexpectedExtraRule() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(createRuleRow())));
        workflowSessionContext.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(
                createRuleRow(),
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES")));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("actual"), is("logic_column=email"));
    }
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "phone", "phone")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "phone_cipher", "phone_cipher")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "email", "email")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "email_cipher", "email_cipher")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "", "")).thenReturn(true);
        return result;
    }
    
    private EncryptWorkflowValidationService createService(final EncryptRuleInspectionService ruleInspectionService) {
        try (
                MockedConstruction<EncryptRuleInspectionService> ignored = mockConstruction(
                        EncryptRuleInspectionService.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(ruleInspectionService)))) {
            return new EncryptWorkflowValidationService();
        }
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
    
    private ExecutableWorkflowArtifact createRuleDistSQLArtifact(final String sql, final String displaySql) {
        return new ExecutableWorkflowArtifact(WorkflowArtifactPayloadUtils.STEP_RULE_DISTSQL, WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL, sql, displaySql, true);
    }
}
