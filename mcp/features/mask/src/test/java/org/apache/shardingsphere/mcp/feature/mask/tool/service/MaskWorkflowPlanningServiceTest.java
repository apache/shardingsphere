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

import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskWorkflowPlanningServiceTest {
    
    @Test
    void assertPlanRejectsMissingPlanningContext() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        MaskWorkflowPlanningService service = new MaskWorkflowPlanningService(contextStore, mock(MaskRuleInspectionService.class),
                mock(MaskAlgorithmRecommendationService.class), mock(MaskAlgorithmPropertyTemplateService.class), mock(MaskRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(null, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), "session-1", new WorkflowRequest());
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsLifecycleMismatchForCreate() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone")));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        MaskWorkflowPlanningService service = new MaskWorkflowPlanningService(contextStore, ruleInspectionService, mock(MaskAlgorithmRecommendationService.class),
                mock(MaskAlgorithmPropertyTemplateService.class), mock(MaskRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(null, createResolvedMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanDropWorkflow() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of(Map.of("column", "phone")));
        MaskRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(MaskRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planMaskDropRule(any(), any())).thenReturn(new RuleArtifact("drop", "DROP MASK RULE orders"));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        MaskWorkflowPlanningService service = new MaskWorkflowPlanningService(contextStore, ruleInspectionService, mock(MaskAlgorithmRecommendationService.class),
                mock(MaskAlgorithmPropertyTemplateService.class), ruleDistSQLPlanningService);
        WorkflowContextSnapshot actual = service.plan(null, createResolvedMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", createRequest("drop"));
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertPlanWithNaturalLanguageInferenceArguments")
    void assertPlanWithNaturalLanguageInference(final String name, final String naturalLanguageIntent, final boolean ruleExists,
                                                final String expectedOperationType, final String expectedFieldSemantics, final String expectedStatus) {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(ruleExists ? List.of(Map.of("column", "phone")) : List.of());
        when(ruleInspectionService.queryMaskAlgorithms(any())).thenReturn(List.of(Map.of("type", "MASK_FROM_X_TO_Y")));
        when(ruleInspectionService.enrichMaskAlgorithms(any())).thenReturn(List.of(Map.of("type", "MASK_FROM_X_TO_Y")));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        MaskWorkflowPlanningService service = new MaskWorkflowPlanningService(contextStore, ruleInspectionService, new MaskAlgorithmRecommendationService(),
                new MaskAlgorithmPropertyTemplateService(), new MaskRuleDistSQLPlanningService());
        WorkflowContextSnapshot actual = service.plan(null, createResolvedMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", createNaturalLanguageRequest(naturalLanguageIntent));
        assertThat(actual.getClarifiedIntent().getOperationType(), is(expectedOperationType));
        assertThat(actual.getClarifiedIntent().getFieldSemantics(), is(expectedFieldSemantics));
        assertThat(actual.getStatus(), is(expectedStatus));
    }
    
    @Test
    void assertPlanStopsOnBlockingAlgorithmIssue() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryMaskAlgorithms(any())).thenReturn(List.of());
        when(ruleInspectionService.enrichMaskAlgorithms(any())).thenReturn(List.of());
        MaskAlgorithmRecommendationService algorithmRecommendationService = mock(MaskAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendMaskAlgorithms(any(), any(), any(), any())).thenAnswer(invocation -> {
            List<WorkflowIssue> issues = invocation.getArgument(3);
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm", "missing", "fix", false, Map.of()));
            return List.of();
        });
        WorkflowContextStore contextStore = new WorkflowContextStore();
        MaskWorkflowPlanningService service = new MaskWorkflowPlanningService(contextStore, ruleInspectionService, algorithmRecommendationService,
                mock(MaskAlgorithmPropertyTemplateService.class), mock(MaskRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(null, createResolvedMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getClarifiedIntent().getPendingQuestions().get(0), is("请改用当前 Proxy 可见的脱敏算法。"));
    }
    
    @Test
    void assertPlanRequiresMissingProperties() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryMaskAlgorithms(any())).thenReturn(List.of());
        when(ruleInspectionService.enrichMaskAlgorithms(any())).thenReturn(List.of());
        MaskAlgorithmRecommendationService algorithmRecommendationService = mock(MaskAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendMaskAlgorithms(any(), any(), any(), any()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "MASK_FROM_X_TO_Y", "builtin", null, null, null, 100, "reason", "")));
        MaskAlgorithmPropertyTemplateService propertyTemplateService = mock(MaskAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements("MASK_FROM_X_TO_Y")).thenReturn(List.of(
                new AlgorithmPropertyRequirement("primary", "from-x", true, false, "from", "")));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        MaskWorkflowPlanningService service = new MaskWorkflowPlanningService(contextStore, ruleInspectionService, algorithmRecommendationService,
                propertyTemplateService, mock(MaskRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(null, createResolvedMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
    }
    
    @Test
    void assertPlanCreatesRuleArtifact() {
        MaskRuleInspectionService ruleInspectionService = mock(MaskRuleInspectionService.class);
        when(ruleInspectionService.queryMaskRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryMaskAlgorithms(any())).thenReturn(List.of());
        when(ruleInspectionService.enrichMaskAlgorithms(any())).thenReturn(List.of());
        MaskAlgorithmRecommendationService algorithmRecommendationService = mock(MaskAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendMaskAlgorithms(any(), any(), any(), any()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "MASK_FROM_X_TO_Y", "builtin", null, null, null, 100, "reason", "")));
        MaskAlgorithmPropertyTemplateService propertyTemplateService = mock(MaskAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements("MASK_FROM_X_TO_Y")).thenReturn(List.of());
        MaskRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(MaskRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planMaskRule(any(), any())).thenReturn(new RuleArtifact("create", "CREATE MASK RULE orders"));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        MaskWorkflowPlanningService service = new MaskWorkflowPlanningService(contextStore, ruleInspectionService, algorithmRecommendationService,
                propertyTemplateService, ruleDistSQLPlanningService);
        WorkflowContextSnapshot actual = service.plan(null, createResolvedMetadataQueryFacade(), mock(MCPFeatureQueryFacade.class), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
    }
    
    private MCPMetadataQueryFacade createResolvedMetadataQueryFacade() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryDatabase("logic_db")).thenReturn(Optional.of(createDatabaseMetadata()));
        when(metadataQueryFacade.queryTable("logic_db", "public", "orders")).thenReturn(Optional.of(createTableMetadata()));
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(createColumnMetadata()));
        return metadataQueryFacade;
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
        return result;
    }
    
    private MCPDatabaseMetadata createDatabaseMetadata() {
        return new MCPDatabaseMetadata("logic_db", "MySQL", "8.0", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(createTableMetadata()), List.of())));
    }
    
    private MCPTableMetadata createTableMetadata() {
        return new MCPTableMetadata("logic_db", "public", "orders", List.of(createColumnMetadata()), List.of());
    }
    
    private MCPColumnMetadata createColumnMetadata() {
        return new MCPColumnMetadata("logic_db", "public", "orders", "", "phone");
    }
    
    private static Stream<Arguments> assertPlanWithNaturalLanguageInferenceArguments() {
        return Stream.of(
                Arguments.of("create from default verb", "给 phone 列做脱敏", false, "create", "phone", "clarifying"),
                Arguments.of("alter from chinese verb", "修改手机号脱敏规则", true, "alter", "phone", "clarifying"),
                Arguments.of("drop from chinese verb", "删除手机号脱敏规则", true, "drop", "phone", "planned"));
    }
}
