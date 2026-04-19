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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptWorkflowPlanningServiceTest {
    
    @Test
    void assertPlanRejectsMissingPlanningContext() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        EncryptWorkflowPlanningService service = new EncryptWorkflowPlanningService(contextStore, mock(EncryptRuleInspectionService.class),
                mock(EncryptAlgorithmRecommendationService.class), mock(EncryptAlgorithmPropertyTemplateService.class),
                mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class),
                mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(createRequestContext(mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class)), "session-1", new EncryptWorkflowRequest());
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsLifecycleMismatchForCreate() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        EncryptWorkflowPlanningService service = new EncryptWorkflowPlanningService(contextStore, ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(createResolvedRequestContext(), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanDropWorkflow() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptDropRule(any(), any())).thenReturn(new RuleArtifact("drop", "DROP ENCRYPT RULE orders"));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        EncryptWorkflowPlanningService service = new EncryptWorkflowPlanningService(contextStore, ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), ruleDistSQLPlanningService);
        WorkflowContextSnapshot actual = service.plan(createResolvedRequestContext(), "session-1", createRequest("drop"));
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
        assertThat(actual.getIssues().size(), is(2));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertPlanWithNaturalLanguageInferenceArguments")
    void assertPlanWithNaturalLanguageInference(final String name, final String naturalLanguageIntent, final boolean ruleExists,
                                                final String expectedOperationType, final String expectedFieldSemantics, final String expectedStatus,
                                                final boolean expectedHasPendingQuestions) {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(ruleExists ? List.of(Map.of("logic_column", "phone")) : List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of(Map.of("type", "AES"), Map.of("type", "MD5")));
        when(ruleInspectionService.enrichEncryptAlgorithms(any())).thenReturn(List.of(
                Map.of("type", "AES", "supports_like", false),
                Map.of("type", "MD5", "supports_like", false)));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        EncryptWorkflowPlanningService service = new EncryptWorkflowPlanningService(contextStore, ruleInspectionService, new EncryptAlgorithmRecommendationService(),
                new EncryptAlgorithmPropertyTemplateService(), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), new EncryptRuleDistSQLPlanningService());
        WorkflowContextSnapshot actual = service.plan(createResolvedRequestContext(), "session-1", createNaturalLanguageRequest(naturalLanguageIntent));
        assertThat(actual.getClarifiedIntent().getOperationType(), is(expectedOperationType));
        assertThat(actual.getClarifiedIntent().getFieldSemantics(), is(expectedFieldSemantics));
        assertThat(actual.getStatus(), is(expectedStatus));
        assertThat(actual.getClarifiedIntent().getPendingQuestions().isEmpty(), is(!expectedHasPendingQuestions));
    }
    
    @Test
    void assertPlanStopsOnBlockingAlgorithmIssue() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of());
        when(ruleInspectionService.enrichEncryptAlgorithms(any())).thenReturn(List.of());
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any(), any())).thenAnswer(invocation -> {
            List<WorkflowIssue> issues = invocation.getArgument(3);
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm", "missing", "fix", false, Map.of()));
            return List.of();
        });
        WorkflowContextStore contextStore = new WorkflowContextStore();
        EncryptWorkflowPlanningService service = new EncryptWorkflowPlanningService(contextStore, ruleInspectionService, algorithmRecommendationService,
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(createResolvedRequestContext(), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getClarifiedIntent().getPendingQuestions().get(0), is("请改用当前 Proxy 可见且满足需求的加密算法。"));
    }
    
    @Test
    void assertPlanInfersEncryptCapabilitiesFromNaturalLanguage() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of(Map.of("type", "AES"), Map.of("type", "MD5")));
        when(ruleInspectionService.enrichEncryptAlgorithms(any())).thenReturn(List.of(
                Map.of("type", "AES", "supports_like", false),
                Map.of("type", "MD5", "supports_like", false)));
        DerivedColumnNamingService derivedColumnNamingService = mock(DerivedColumnNamingService.class);
        when(derivedColumnNamingService.createPlan(any(), any(), any(), any())).thenReturn(createDerivedColumnPlan());
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptRule(any(), any(), any())).thenReturn(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        EncryptWorkflowRequest request = createNaturalLanguageRequest("给手机号加密，需要可逆和等值查询，不需要like");
        request.setAlgorithmType("AES");
        request.setAssistedQueryAlgorithmType("MD5");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        request.setAllowIndexDDL(false);
        EncryptWorkflowPlanningService service = new EncryptWorkflowPlanningService(new WorkflowContextStore(), ruleInspectionService,
                new EncryptAlgorithmRecommendationService(), new EncryptAlgorithmPropertyTemplateService(), derivedColumnNamingService,
                mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class), ruleDistSQLPlanningService);
        WorkflowContextSnapshot actual = service.plan(createResolvedRequestContext(), "session-1", request);
        EncryptWorkflowState actualState = (EncryptWorkflowState) actual.getFeatureData();
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getClarifiedIntent().getOperationType(), is("create"));
        assertTrue(actualState.getRequiresDecrypt());
        assertTrue(actualState.getRequiresEqualityFilter());
        assertFalse(actualState.getRequiresLikeQuery());
    }
    
    @Test
    void assertPlanRequiresMissingProperties() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of());
        when(ruleInspectionService.enrichEncryptAlgorithms(any())).thenReturn(List.of());
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any(), any()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "AES", "builtin", true, true, false, 100, "reason", "")));
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of(
                new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", "")));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        EncryptWorkflowPlanningService service = new EncryptWorkflowPlanningService(contextStore, ruleInspectionService, algorithmRecommendationService,
                propertyTemplateService, mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class),
                mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(createResolvedRequestContext(), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
        assertThat(actual.getClarifiedIntent().getPendingQuestions().get(0), is("请提供属性 `aes-key-value`。"));
    }
    
    @Test
    void assertPlanCreatesArtifactsWithoutIndexDdl() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of());
        when(ruleInspectionService.enrichEncryptAlgorithms(any())).thenReturn(List.of());
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any(), any()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "AES", "builtin", true, true, false, 100, "reason", "")));
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of());
        DerivedColumnNamingService derivedColumnNamingService = mock(DerivedColumnNamingService.class);
        when(derivedColumnNamingService.createPlan(any(), any(), any(), any())).thenReturn(createDerivedColumnPlan());
        PhysicalDDLPlanningService physicalDDLPlanningService = mock(PhysicalDDLPlanningService.class);
        when(physicalDDLPlanningService.planAddColumnArtifacts(any(), any(), any(), any()))
                .thenReturn(List.of(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN phone_cipher VARCHAR(32)", 10)));
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptRule(any(), any(), any())).thenReturn(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        IndexPlanningService indexPlanningService = mock(IndexPlanningService.class);
        WorkflowContextStore contextStore = new WorkflowContextStore();
        EncryptWorkflowPlanningService service = new EncryptWorkflowPlanningService(contextStore, ruleInspectionService, algorithmRecommendationService,
                propertyTemplateService, derivedColumnNamingService, physicalDDLPlanningService, indexPlanningService, ruleDistSQLPlanningService);
        EncryptWorkflowRequest request = createRequest("create");
        request.setAllowIndexDDL(false);
        WorkflowContextSnapshot actual = service.plan(createResolvedRequestContext(), "session-1", request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getDdlArtifacts().size(), is(1));
        assertThat(actual.getRuleArtifacts().size(), is(1));
        assertTrue(actual.getIndexPlans().isEmpty());
        verify(indexPlanningService, never()).planIndexes(any(), any(), any());
    }
    
    private MCPFeatureContext createResolvedRequestContext() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryDatabase("logic_db")).thenReturn(Optional.of(createDatabaseMetadata()));
        when(metadataQueryFacade.queryTable("logic_db", "public", "orders")).thenReturn(Optional.of(createTableMetadata()));
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(createColumnMetadata()));
        when(metadataQueryFacade.queryTableColumns("logic_db", "public", "orders")).thenReturn(List.of(createColumnMetadata()));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryColumnDefinition("logic_db", "public", "orders", "phone")).thenReturn("VARCHAR(32)");
        return createRequestContext(metadataQueryFacade, queryFacade);
    }
    
    private MCPFeatureContext createRequestContext(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade) {
        MCPFeatureContext result = mock(MCPFeatureContext.class);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        return result;
    }
    
    private EncryptWorkflowRequest createRequest(final String operationType) {
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("orders");
        result.setColumn("phone");
        result.setOperationType(operationType);
        result.setRequiresDecrypt(true);
        result.setRequiresEqualityFilter(false);
        result.setRequiresLikeQuery(false);
        return result;
    }
    
    private EncryptWorkflowRequest createNaturalLanguageRequest(final String naturalLanguageIntent) {
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
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
    
    private DerivedColumnPlan createDerivedColumnPlan() {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setCipherColumnRequired(true);
        result.setCipherColumnName("phone_cipher");
        return result;
    }
    
    private static Stream<Arguments> assertPlanWithNaturalLanguageInferenceArguments() {
        return Stream.of(
                Arguments.of("create from default verb", "给 phone 列加密", false, "create", "phone", "clarifying", true),
                Arguments.of("alter from chinese verb", "修改手机号加密规则", true, "alter", "phone", "clarifying", true),
                Arguments.of("drop from chinese verb", "删除手机号加密规则", true, "drop", "phone", "planned", false));
    }
}
