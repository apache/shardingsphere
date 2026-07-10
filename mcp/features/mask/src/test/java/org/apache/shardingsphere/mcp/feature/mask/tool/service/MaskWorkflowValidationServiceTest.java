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

package org.apache.shardingsphere.mcp.feature.mask.tool.service;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleWorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationSupport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MaskWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot("plan-1", "session-1", "executed", "create"));
        Map<String, Object> actual = createService(mock(MaskRuleInspectionService.class))
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-2",
                        workflowSessionContext.getRequired("plan-1"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).getFirst()).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateHappyPath() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y")));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.getDatabaseType("logic_db")).thenReturn("FixtureDB");
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
    void assertValidateApplyArtifactsRejectsUnavailableMaskAlgorithm() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("MASK_FROM_X_TO_Y");
        request.getPrimaryAlgorithmProperties().put("replace-char", "raw-secret");
        snapshot.setRequest(request);
        String sql = "CREATE MASK RULE `orders` (COLUMNS((NAME=`phone`, TYPE(NAME='mask_from_x_to_y', PROPERTIES('replace-char'='******')))))";
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(MaskAlgorithm.class, "MASK_FROM_X_TO_Y",
                    WorkflowSQLUtils.createProperties(request.getPrimaryAlgorithmProperties()))).thenThrow(new IllegalArgumentException("raw-secret"));
            List<Map<String, Object>> actual = new MaskWorkflowValidationService().validate(snapshot, List.of(createRuleDistSQLArtifact(sql)));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
            assertFalse(String.valueOf(actual).contains("raw-secret"));
        }
    }
    
    @Test
    void assertValidateApplyArtifactsIgnoresDropMaskRule() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        WorkflowRequest request = new WorkflowRequest();
        request.setAlgorithmType("MASK_FROM_X_TO_Y");
        snapshot.setRequest(request);
        assertTrue(new MaskWorkflowValidationService().validate(snapshot, List.of(createRuleDistSQLArtifact("DROP MASK RULE `orders`"))).isEmpty());
    }
    
    @Test
    void assertSynchronize() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MASK_FROM_X_TO_Y")));
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        createService(ruleInspectionService).synchronize(snapshot, metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade, "session-1");
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertSynchronizeWhenStateDoesNotConverge() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        WorkflowSynchronizationException actual = assertThrows(WorkflowSynchronizationException.class,
                () -> createService(ruleInspectionService).synchronize(snapshot, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class),
                        mock(MCPFeatureExecutionFacade.class), "session-1"));
        assertThat(actual.getIssueCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertValidateDropWorkflowAfterRuleRemoval() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop");
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWhenAlgorithmMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("MASK_FROM_X_TO_Y");
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateExpectedStateDetectsPropertyMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(Map.of(
                "column", "phone",
                "algorithm_type", "MASK_FROM_X_TO_Y",
                "algorithm_props", Map.of("from-x", "1")))));
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "column", "phone",
                "algorithm_type", "MASK_FROM_X_TO_Y",
                "algorithm_props", Map.of("from-x", "2"))));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("expected"), is("algorithm_props={from-x=1}"));
    }
    
    @Test
    void assertValidateExpectedStateAcceptsResolvedReferencedProperty() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().getPrimaryAlgorithmProperties().put("replace-char", "secret_reference:primary.replace-char");
        snapshot.getRequest().getPrimaryAlgorithmSecretReferences().put("replace-char", SecretReferenceValue.create());
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(Map.of(
                "column", "phone",
                "algorithm_type", "MASK_FROM_X_TO_Y",
                "algorithm_props", Map.of("replace-char", "secret_reference:primary.replace-char")))));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "column", "phone",
                "algorithm_type", "MASK_FROM_X_TO_Y",
                "algorithm_props", Map.of("replace-char", "raw-actual-secret"))));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(actual.get("overall_status"), is("passed"));
        assertThat(((List<?>) actual.get("mismatches")).size(), is(0));
        assertFalse(String.valueOf(actual).contains("raw-actual-secret"));
        assertFalse(String.valueOf(actual).contains("placeholder://secret-value-1"));
    }
    
    @Test
    void assertValidateExpectedStateDetectsUnresolvedReferencedProperty() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().getPrimaryAlgorithmProperties().put("replace-char", "secret_reference:primary.replace-char");
        snapshot.getRequest().getPrimaryAlgorithmSecretReferences().put("replace-char", SecretReferenceValue.create());
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(Map.of(
                "column", "phone",
                "algorithm_type", "MASK_FROM_X_TO_Y",
                "algorithm_props", Map.of("replace-char", "secret_reference:primary.replace-char")))));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "column", "phone",
                "algorithm_type", "MASK_FROM_X_TO_Y",
                "algorithm_props", Map.of("replace-char", "secret_reference:primary.replace-char"))));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("expected"), is("algorithm_props={replace-char=******}"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("actual"), is("algorithm_props={replace-char=******}"));
        assertFalse(String.valueOf(actual).contains("placeholder://secret-value-1"));
    }
    
    @Test
    void assertValidateExpectedStateDetectsMissingNonTargetRule() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.setFeatureData(new RuleWorkflowFeatureData(List.of(), List.of(
                Map.of("column", "phone", "algorithm_type", "MD5"),
                Map.of("column", "email", "algorithm_type", "MD5"))));
        workflowSessionContext.save(snapshot);
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        Map<String, Object> actual = createService(ruleInspectionService)
                .validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("expected"), is("column=email"));
    }
    
    private MaskWorkflowValidationService createService(final MaskRuleInspectionService ruleInspectionService) {
        MaskWorkflowValidationService result = new MaskWorkflowValidationService();
        try {
            setField(result, "ruleInspectionService", ruleInspectionService);
            setField(result, "workflowSynchronizationSupport", new WorkflowSynchronizationSupport(1, 0L));
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
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
        result.setWorkflowKind(MaskFeatureDefinition.WORKFLOW_KIND);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("phone");
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    private ExecutableWorkflowArtifact createRuleDistSQLArtifact(final String sql) {
        return new ExecutableWorkflowArtifact("review-rule-sql", "rule_dist_sql", sql, sql, true);
    }
}
