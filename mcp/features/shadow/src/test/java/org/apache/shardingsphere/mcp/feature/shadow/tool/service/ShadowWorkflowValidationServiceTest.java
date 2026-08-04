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

package org.apache.shardingsphere.mcp.feature.shadow.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.feature.shadow.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowAlgorithmCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowDefaultAlgorithmWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

class ShadowWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRule() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "shadow_rule", "shadow_rule")).thenReturn(true);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "demo_ds", "demo_ds")).thenReturn(true);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "demo_ds_shadow", "demo_ds_shadow")).thenReturn(true);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "t_order", "t_order")).thenReturn(true);
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(Map.of(
                "rule_name", "shadow_rule",
                "source_name", "demo_ds",
                "shadow_name", "demo_ds_shadow",
                "shadow_table", "t_order",
                "algorithm_type", "VALUE_MATCH",
                "algorithm_props", Map.of("operation", "INSERT", "column", "shadow", "value", "true"))));
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade, mock(MCPFeatureExecutionFacade.class), "session-1", createRuleSnapshot());
        assertThat(actual.get("overall_status"), is(WorkflowLifecycle.STATUS_PASSED));
    }
    
    @Test
    void assertValidateRuleRejectsDifferentAlgorithmProperties() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createRuleQueryFacade();
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(createRuleRow(Map.of("operation", "INSERT", "column", "shadow", "value", "false"))));
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade, mock(MCPFeatureExecutionFacade.class), "session-1", createRuleSnapshot());
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
        Map<?, ?> actualMismatch = (Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst();
        assertThat(actualMismatch.get("layer"), is("shadow_rule.algorithm_properties"));
        assertThat(String.valueOf(actualMismatch.get("actual")), containsString("value=false"));
    }
    
    @Test
    void assertValidateRuleRejectsExtraRow() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createRuleQueryFacade();
        Map<String, Object> ruleRow = createRuleRow(Map.of("operation", "INSERT", "column", "shadow", "value", "true"));
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(ruleRow, ruleRow));
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade, mock(MCPFeatureExecutionFacade.class), "session-1", createRuleSnapshot());
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
        Map<?, ?> actualMismatch = (Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst();
        assertThat(actualMismatch.get("layer"), is("shadow_rule.row_count"));
        assertThat(actualMismatch.get("actual"), is("2"));
    }
    
    @Test
    void assertValidateRuleWhenMissing() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createRuleQueryFacade();
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                        mock(MCPFeatureExecutionFacade.class), "session-1", createRuleSnapshot());
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("layer"), is("shadow_rule"));
    }
    
    @Test
    void assertValidateRuleDrop() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createRuleQueryFacade();
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of());
        WorkflowContextSnapshot snapshot = createRuleSnapshot();
        snapshot.getClarifiedIntent().setOperationType(WorkflowLifecycle.OPERATION_DROP);
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                        mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_VALIDATED));
    }
    
    @Test
    void assertValidateRuleDropWhenRuleRemains() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createRuleQueryFacade();
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(createRuleRow(Map.of())));
        WorkflowContextSnapshot snapshot = createRuleSnapshot();
        snapshot.getClarifiedIntent().setOperationType(WorkflowLifecycle.OPERATION_DROP);
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                        mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("mismatchedRuleShapes")
    void assertValidateRuleReportsMismatchedShape(final String name, final String fieldName, final String actualValue, final String expectedLayer) {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createRuleQueryFacade();
        Map<String, Object> ruleRow = new LinkedHashMap<>(createRuleRow(Map.of("operation", "INSERT", "column", "shadow", "value", "true")));
        ruleRow.put(fieldName, actualValue);
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(ruleRow));
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade, mock(MCPFeatureExecutionFacade.class), "session-1", createRuleSnapshot());
        Map<?, ?> actualMismatch = (Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst();
        assertThat(actualMismatch.get("layer"), is(expectedLayer));
        assertThat(actualMismatch.get("actual"), is(actualValue));
    }
    
    @Test
    void assertValidateDefaultAlgorithmRejectsDifferentProperties() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setDatabase("logic_db");
        request.setAlgorithmType("SQL_HINT");
        request.putAlgorithmProperties(Map.of("foo", "bar"));
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(Map.of("type", "SQL_HINT", "props", Map.of("foo", "baz"))));
        Map<String, Object> actual = createService(inspectionService).validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", createSnapshot(request, "create"));
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @Test
    void assertValidateDefaultAlgorithmRejectsDifferentType() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setDatabase("logic_db");
        request.setAlgorithmType("SQL_HINT");
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(Map.of("type", "VALUE_MATCH")));
        Map<String, Object> actual = createService(inspectionService).validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", createSnapshot(request, "create"));
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @Test
    void assertValidateDefaultAlgorithmRejectsDuplicateRows() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setDatabase("logic_db");
        request.setAlgorithmType("SQL_HINT");
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        Map<String, Object> row = Map.of("type", "SQL_HINT", "props", Map.of());
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(row, row));
        Map<String, Object> actual = createService(inspectionService).validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", createSnapshot(request, "create"));
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @Test
    void assertValidateDefaultAlgorithm() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setDatabase("logic_db");
        request.setAlgorithmType("SQL_HINT");
        request.putAlgorithmProperties(Map.of("foo", "bar"));
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(Map.of("type", "SQL_HINT", "props", Map.of("foo", "bar"))));
        Map<String, Object> actual = createService(inspectionService).validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", createSnapshot(request, "create"));
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_VALIDATED));
    }
    
    @Test
    void assertValidateDefaultAlgorithmDrop() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setDatabase("logic_db");
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService).validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", createSnapshot(request, WorkflowLifecycle.OPERATION_DROP));
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_VALIDATED));
    }
    
    @Test
    void assertValidateDefaultAlgorithmDropWhenAlgorithmRemains() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setDatabase("logic_db");
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(Map.of("type", "SQL_HINT")));
        Map<String, Object> actual = createService(inspectionService).validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", createSnapshot(request, WorkflowLifecycle.OPERATION_DROP));
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @Test
    void assertValidateCleanupFailure() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "unused_algorithm", "unused_algorithm")).thenReturn(true);
        when(inspectionService.queryAlgorithms(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "unused_algorithm")));
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade, mock(MCPFeatureExecutionFacade.class), "session-1", createCleanupSnapshot());
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @Test
    void assertValidateCleanup() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(inspectionService.queryAlgorithms(queryFacade, "logic_db")).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService)
                .validate(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), queryFacade,
                        mock(MCPFeatureExecutionFacade.class), "session-1", createCleanupSnapshot());
        assertThat(actual.get("status"), is(WorkflowLifecycle.STATUS_VALIDATED));
    }
    
    @Test
    void assertValidateApplyArtifactsRejectsUnavailableRuleAlgorithm() {
        WorkflowContextSnapshot snapshot = createRuleSnapshot();
        ShadowRuleWorkflowRequest request = (ShadowRuleWorkflowRequest) snapshot.getRequest();
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(ShadowAlgorithm.class, "VALUE_MATCH",
                    WorkflowAlgorithmUtils.createProperties(request.getAlgorithmProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShadowWorkflowValidationService().validate(snapshot, List.of(createRuleDistSQLArtifact("CREATE SHADOW RULE `shadow_rule`(...)")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        }
    }
    
    @Test
    void assertValidateApplyArtifactsRejectsUnavailableDefaultAlgorithm() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setAlgorithmType("SQL_HINT");
        request.putAlgorithmProperties(Map.of("foo", "bar"));
        WorkflowContextSnapshot snapshot = createSnapshot(request, "create");
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(ShadowAlgorithm.class, "SQL_HINT",
                    WorkflowAlgorithmUtils.createProperties(request.getAlgorithmProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShadowWorkflowValidationService().validate(snapshot,
                    List.of(createRuleDistSQLArtifact("CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME='sql_hint')")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("message"), is("Generated shadow DistSQL references algorithm `SQL_HINT`, "
                    + "but it cannot be loaded or initialized by ShadowAlgorithm SPI."));
        }
    }
    
    @Test
    void assertValidateApplyArtifactsAcceptsAvailableDefaultAlgorithm() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setAlgorithmType("SQL_HINT");
        WorkflowContextSnapshot snapshot = createSnapshot(request, "create");
        try (MockedStatic<TypedSPILoader> ignored = mockStatic(TypedSPILoader.class)) {
            assertThat(new ShadowWorkflowValidationService().validate(snapshot,
                    List.of(createRuleDistSQLArtifact("CREATE DEFAULT SHADOW ALGORITHM TYPE(NAME='sql_hint')"))).size(), is(0));
        }
    }
    
    @Test
    void assertValidateApplyArtifactsWithoutRuleAlgorithm() {
        WorkflowContextSnapshot snapshot = createRuleSnapshot();
        ((ShadowRuleWorkflowRequest) snapshot.getRequest()).setAlgorithmType("");
        assertThat(new ShadowWorkflowValidationService().validate(
                snapshot, List.of(createRuleDistSQLArtifact("ALTER SHADOW RULE `shadow_rule`(...)"))).size(), is(0));
    }
    
    @Test
    void assertValidateApplyArtifactsWithoutDefaultAlgorithm() {
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        WorkflowContextSnapshot snapshot = createSnapshot(request, "create");
        assertThat(new ShadowWorkflowValidationService().validate(
                snapshot, List.of(createRuleDistSQLArtifact("ALTER DEFAULT SHADOW ALGORITHM TYPE(NAME='sql_hint')"))).size(), is(0));
    }
    
    @Test
    void assertValidateApplyArtifactsIgnoresCleanupArtifact() {
        assertThat(new ShadowWorkflowValidationService().validate(
                createCleanupSnapshot(), List.of(createRuleDistSQLArtifact("DROP SHADOW ALGORITHM `unused_algorithm`"))).size(), is(0));
    }
    
    @Test
    void assertSynchronize() {
        ShadowInspectionService inspectionService = mock(ShadowInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createRuleQueryFacade();
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(createRuleRow(
                Map.of("operation", "INSERT", "column", "shadow", "value", "true"))));
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        createService(inspectionService).synchronize(createRuleSnapshot(), metadataQueryFacade, queryFacade, executionFacade, "session-1");
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    private ShadowWorkflowValidationService createService(final ShadowInspectionService inspectionService) {
        try (
                MockedConstruction<ShadowInspectionService> ignored = mockConstruction(
                        ShadowInspectionService.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(inspectionService)))) {
            return new ShadowWorkflowValidationService();
        }
    }
    
    private WorkflowContextSnapshot createRuleSnapshot() {
        ShadowRuleWorkflowRequest request = new ShadowRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("shadow_rule");
        request.setSourceStorageUnit("demo_ds");
        request.setShadowStorageUnit("demo_ds_shadow");
        request.setTableName("t_order");
        request.setAlgorithmType("VALUE_MATCH");
        request.putAlgorithmProperties(Map.of("operation", "INSERT", "column", "shadow", "value", "true"));
        return createSnapshot(request, "create");
    }
    
    private MCPFeatureQueryFacade createRuleQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "shadow_rule", "shadow_rule")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "demo_ds", "demo_ds")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "demo_ds_shadow", "demo_ds_shadow")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "t_order", "t_order")).thenReturn(true);
        return result;
    }
    
    private Map<String, Object> createRuleRow(final Map<String, String> algorithmProperties) {
        return Map.of(
                "rule_name", "shadow_rule",
                "source_name", "demo_ds",
                "shadow_name", "demo_ds_shadow",
                "shadow_table", "t_order",
                "algorithm_type", "VALUE_MATCH",
                "algorithm_props", algorithmProperties);
    }
    
    private static Stream<Arguments> mismatchedRuleShapes() {
        return Stream.of(
                Arguments.of("source storage unit", "source_name", "other_ds", "shadow_rule.source_name"),
                Arguments.of("shadow storage unit", "shadow_name", "other_shadow_ds", "shadow_rule.shadow_name"),
                Arguments.of("shadow table", "shadow_table", "other_table", "shadow_rule.shadow_table"),
                Arguments.of("algorithm type", "algorithm_type", "SQL_HINT", "shadow_rule.algorithm_type"));
    }
    
    private WorkflowContextSnapshot createCleanupSnapshot() {
        ShadowAlgorithmCleanupWorkflowRequest request = new ShadowAlgorithmCleanupWorkflowRequest();
        request.setDatabase("logic_db");
        request.setAlgorithmName("unused_algorithm");
        return createSnapshot(request, WorkflowLifecycle.OPERATION_DROP);
    }
    
    private WorkflowContextSnapshot createSnapshot(final WorkflowRequest request, final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setSessionId("session-1");
        result.setStatus(WorkflowLifecycle.STATUS_EXECUTED);
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    private ExecutableWorkflowArtifact createRuleDistSQLArtifact(final String sql) {
        return new ExecutableWorkflowArtifact(sql, sql);
    }
}
