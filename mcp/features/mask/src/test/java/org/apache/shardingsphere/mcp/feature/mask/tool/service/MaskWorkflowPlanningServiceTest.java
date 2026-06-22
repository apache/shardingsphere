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

import org.apache.shardingsphere.mcp.feature.mask.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskWorkflowPlanningServiceTest {
    
    @Test
    void assertPlanRejectsMissingPlanningContext() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot actual = createService(mock(MaskRuleInspectionService.class), mock(MaskAlgorithmRecommendationService.class),
                mock(MaskAlgorithmPropertyTemplateService.class), mock(MaskRuleDistSQLPlanningService.class))
                .plan(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), "session-1", new WorkflowRequest());
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsMissingTableAndColumn() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        WorkflowContextSnapshot actual = createService(mock(MaskRuleInspectionService.class), mock(MaskAlgorithmRecommendationService.class),
                mock(MaskAlgorithmPropertyTemplateService.class), mock(MaskRuleDistSQLPlanningService.class))
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertTrue(actual.getClarifiedIntent().getClarificationMessages().contains("Please specify target table."));
        assertTrue(actual.getClarifiedIntent().getClarificationMessages().contains("Please specify target column."));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.TABLE_REQUIRED));
        assertThat(actual.getIssues().get(1).getCode(), is(WorkflowIssueCode.COLUMN_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsMissingLogicalColumn() {
        MCPMetadataQueryFacade metadataQueryFacade = createMetadataQueryFacade();
        when(metadataQueryFacade.queryTableColumn(any(), any(), any(), any())).thenReturn(Optional.empty());
        WorkflowContextSnapshot actual = createService(mock(MaskRuleInspectionService.class), mock(MaskAlgorithmRecommendationService.class),
                mock(MaskAlgorithmPropertyTemplateService.class), mock(MaskRuleDistSQLPlanningService.class))
                .plan(new TestWorkflowSessionContext(), metadataQueryFacade, mock(MCPFeatureQueryFacade.class), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.COLUMN_NOT_FOUND));
    }
    
    @Test
    void assertPlanRejectsLifecycleMismatchForCreate() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone")));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.getDatabaseType("logic_db")).thenReturn("MySQL");
        WorkflowRequest request = createRequest("create");
        request.setColumn("Phone");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, mock(MaskAlgorithmRecommendationService.class),
                mock(MaskAlgorithmPropertyTemplateService.class), mock(MaskRuleDistSQLPlanningService.class))
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), queryFacade, "session-1", request);
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanRejectsAddingColumnToExistingTableRule() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5")));
        when(ruleInspectionService.queryMaskAlgorithms(any())).thenReturn(List.of());
        MaskWorkflowPlanningService service = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), createEmptyPropertyTemplateService(),
                new MaskRuleDistSQLPlanningService());
        WorkflowRequest request = createRequest("create");
        request.setColumn("amount");
        request.getPrimaryAlgorithmProperties().put("from-x", "1");
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.MASK_ALTER_SCOPE_LIMITED));
        assertThat(actual.getRuleArtifacts().size(), is(0));
    }
    
    @Test
    void assertPlanRejectsAlterWhenExistingColumnsMustBePreserved() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone", "algorithm_type", "MD5"), Map.of("column", "email", "algorithm_type", "MD5")));
        MaskWorkflowPlanningService service = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), createEmptyPropertyTemplateService(),
                new MaskRuleDistSQLPlanningService());
        WorkflowRequest request = createRequest("alter");
        request.getPrimaryAlgorithmProperties().put("from-x", "1");
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.MASK_ALTER_SCOPE_LIMITED));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanDropWorkflow() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone")));
        MaskRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(MaskRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planMaskDropRule(any())).thenReturn(new RuleArtifact("drop", "DROP MASK RULE orders"));
        WorkflowContextSnapshot actual = createService(ruleInspectionService, mock(MaskAlgorithmRecommendationService.class),
                mock(MaskAlgorithmPropertyTemplateService.class), ruleDistSQLPlanningService)
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", createRequest("drop"));
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
    }
    
    @Test
    void assertPlanRejectsDropWithRemainingTableRules() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone"), Map.of("column", "email")));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.getDatabaseType("logic_db")).thenReturn("MySQL");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, mock(MaskAlgorithmRecommendationService.class),
                mock(MaskAlgorithmPropertyTemplateService.class), mock(MaskRuleDistSQLPlanningService.class))
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), queryFacade, "session-1", createRequest("drop"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.MASK_ALTER_SCOPE_LIMITED));
        assertThat(actual.getRuleArtifacts().size(), is(0));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertPlanWithNaturalLanguageInferenceArguments")
    void assertPlanWithNaturalLanguageInference(final String name, final String naturalLanguageIntent, final boolean ruleExists,
                                                final String expectedOperationType, final String expectedFieldSemantics,
                                                final String expectedStatus) {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(ruleExists ? List.of(Map.of("column", "phone")) : List.of());
        when(ruleInspectionService.queryMaskAlgorithms(any())).thenReturn(List.of(Map.of("type", "MASK_FROM_X_TO_Y")));
        WorkflowContextSnapshot actual = createService(ruleInspectionService, new MaskAlgorithmRecommendationService(), new MaskAlgorithmPropertyTemplateService(),
                new MaskRuleDistSQLPlanningService()).plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1",
                        createNaturalLanguageRequest(naturalLanguageIntent));
        assertThat(actual.getClarifiedIntent().getOperationType(), is(expectedOperationType));
        assertThat(actual.getClarifiedIntent().getFieldSemantics(), is(expectedFieldSemantics));
        assertThat(actual.getStatus(), is(expectedStatus));
    }
    
    @Test
    void assertPlanStopsOnBlockingAlgorithmIssue() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryMaskAlgorithms(any())).thenReturn(List.of());
        MaskAlgorithmRecommendationService algorithmRecommendationService = mock(MaskAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendMaskAlgorithms(any(), any(), any(), any())).thenAnswer(invocation -> {
            List<WorkflowIssue> issues = invocation.getArgument(3);
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm", "missing", "fix", false, Map.of()));
            return List.of();
        });
        WorkflowContextSnapshot actual = createService(ruleInspectionService, algorithmRecommendationService, mock(MaskAlgorithmPropertyTemplateService.class),
                mock(MaskRuleDistSQLPlanningService.class)).plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1",
                        createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().getFirst(), is("Please use a mask algorithm visible in the current Proxy."));
    }
    
    @Test
    void assertPlanRequiresMissingProperties() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        WorkflowContextSnapshot actual = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), createRequiredPropertyTemplateService(),
                mock(MaskRuleDistSQLPlanningService.class)).plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1",
                        createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
    }
    
    @Test
    void assertPlanCreatesRuleArtifactWithoutMetadata() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        WorkflowRequest request = createRequest("create");
        request.getPrimaryAlgorithmProperties().put("from-x", "1");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), createEmptyPropertyTemplateService(), new MaskRuleDistSQLPlanningService())
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
    }
    
    private MaskAlgorithmRecommendationService createPrimaryCandidateRecommendation() {
        MaskAlgorithmRecommendationService result = mock(MaskAlgorithmRecommendationService.class);
        when(result.recommendMaskAlgorithms(any(), any(), any(), any())).thenReturn(List.of(new AlgorithmCandidate("primary", "MASK_FROM_X_TO_Y", null, null, null, 100, "reason", "")));
        return result;
    }
    
    private MaskAlgorithmPropertyTemplateService createEmptyPropertyTemplateService() {
        MaskAlgorithmPropertyTemplateService result = mock(MaskAlgorithmPropertyTemplateService.class);
        when(result.findRequirements("MASK_FROM_X_TO_Y")).thenReturn(List.of());
        return result;
    }
    
    private MaskAlgorithmPropertyTemplateService createRequiredPropertyTemplateService() {
        MaskAlgorithmPropertyTemplateService result = mock(MaskAlgorithmPropertyTemplateService.class);
        when(result.findRequirements("MASK_FROM_X_TO_Y")).thenReturn(List.of(new AlgorithmPropertyRequirement("primary", "from-x", true, false, "from", "")));
        return result;
    }
    
    private WorkflowRequest createRequest(final String operationType) {
        WorkflowRequest result = new WorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("orders");
        result.setColumn("phone");
        result.setOperationType(operationType);
        return result;
    }
    
    private WorkflowRequest createNaturalLanguageRequest(final String naturalLanguageIntent) {
        WorkflowRequest result = new WorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("orders");
        result.setColumn("phone");
        result.setNaturalLanguageIntent(naturalLanguageIntent);
        result.getPrimaryAlgorithmProperties().put("from-x", "1");
        result.getPrimaryAlgorithmProperties().put("to-y", "3");
        return result;
    }
    
    private MCPMetadataQueryFacade createMetadataQueryFacade() {
        MCPMetadataQueryFacade result = mock(MCPMetadataQueryFacade.class);
        when(result.queryDatabase(any())).thenReturn(Optional.of(createDatabaseMetadata()));
        when(result.queryTable(any(), any(), any())).thenReturn(Optional.of(createTableMetadata()));
        when(result.queryTableColumn(any(), any(), any(), any())).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        return result;
    }
    
    private MCPDatabaseMetadata createDatabaseMetadata() {
        return new MCPDatabaseMetadata("logic_db", "MySQL", "8.0", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(createTableMetadata()), List.of(), List.of())));
    }
    
    private MCPTableMetadata createTableMetadata() {
        return new MCPTableMetadata("logic_db", "public", "orders", List.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")), List.of());
    }
    
    private MaskWorkflowPlanningService createService(final MaskRuleInspectionService ruleInspectionService,
                                                      final MaskAlgorithmRecommendationService algorithmRecommendationService,
                                                      final MaskAlgorithmPropertyTemplateService algorithmPropertyTemplateService,
                                                      final MaskRuleDistSQLPlanningService ruleDistSQLPlanningService) {
        MaskWorkflowPlanningService result = new MaskWorkflowPlanningService();
        try {
            setField(result, "ruleInspectionService", ruleInspectionService);
            setField(result, "algorithmRecommendationService", algorithmRecommendationService);
            setField(result, "algorithmPropertyTemplateService", algorithmPropertyTemplateService);
            setField(result, "ruleDistSQLPlanningService", ruleDistSQLPlanningService);
            return result;
        } catch (final ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private static Stream<Arguments> assertPlanWithNaturalLanguageInferenceArguments() {
        return Stream.of(
                Arguments.of("create from default verb", "mask phone column", false, "create", "phone", "planned"),
                Arguments.of("alter from english verb", "update phone number mask rule", true, "alter", "phone", "clarifying"),
                Arguments.of("drop from english verb", "delete phone number mask rule", true, "drop", "phone", "planned"));
    }
}
