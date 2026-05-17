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

import org.apache.shardingsphere.mcp.feature.encrypt.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowState;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
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
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(mock(EncryptRuleInspectionService.class), mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, mock(MCPMetadataQueryFacade.class), mock(MCPFeatureQueryFacade.class), "session-1", new EncryptWorkflowRequest());
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsLifecycleMismatchForCreate() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createQueryFacade(), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanDropWorkflow() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptDropRule(any(), any())).thenReturn(new RuleArtifact("drop", "DROP ENCRYPT RULE orders"));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), ruleDistSQLPlanningService);
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createQueryFacade(), "session-1", createRequest("drop"));
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
        assertThat(actual.getIssues().size(), is(2));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertPlanWithNaturalLanguageInferenceArguments")
    void assertPlanWithNaturalLanguageInference(final String name, final String naturalLanguageIntent, final boolean ruleExists,
                                                final String expectedOperationType, final String expectedFieldSemantics, final String expectedStatus,
                                                final boolean expectedHasClarificationMessages) {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(ruleExists ? List.of(Map.of("logic_column", "phone")) : List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of(
                Map.of("type", "AES", "supports_like", false),
                Map.of("type", "MD5", "supports_like", false)));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, new EncryptAlgorithmRecommendationService(),
                new EncryptAlgorithmPropertyTemplateService(), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), new EncryptRuleDistSQLPlanningService());
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createQueryFacade(), "session-1",
                createNaturalLanguageRequest(naturalLanguageIntent));
        assertThat(actual.getClarifiedIntent().getOperationType(), is(expectedOperationType));
        assertThat(actual.getClarifiedIntent().getFieldSemantics(), is(expectedFieldSemantics));
        assertThat(actual.getStatus(), is(expectedStatus));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().isEmpty(), is(!expectedHasClarificationMessages));
    }
    
    @Test
    void assertPlanStopsOnBlockingAlgorithmIssue() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of());
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any())).thenAnswer(invocation -> {
            List<WorkflowIssue> issues = invocation.getArgument(2);
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm", "missing", "fix", false, Map.of()));
            return List.of();
        });
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, algorithmRecommendationService,
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createQueryFacade(), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().get(0), is("Please use an encrypt algorithm that is visible in the current Proxy and satisfies the requirements."));
    }
    
    @Test
    void assertPlanInfersEncryptCapabilitiesFromNaturalLanguage() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of(
                Map.of("type", "AES", "supports_like", false),
                Map.of("type", "MD5", "supports_like", false)));
        DerivedColumnNamingService derivedColumnNamingService = mock(DerivedColumnNamingService.class);
        when(derivedColumnNamingService.createPlan(any(), any(), any())).thenReturn(createDerivedColumnPlan());
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptRule(any(), any(), any())).thenReturn(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        EncryptWorkflowRequest request = createNaturalLanguageRequest("encrypt phone number, requires reversible and equality query, no like");
        request.setAlgorithmType("AES");
        request.getOptions().setAssistedQueryAlgorithmType("MD5");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        request.getOptions().setAllowIndexDDL(false);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, new EncryptAlgorithmRecommendationService(),
                new EncryptAlgorithmPropertyTemplateService(), derivedColumnNamingService, mock(PhysicalDDLPlanningService.class),
                mock(IndexPlanningService.class), ruleDistSQLPlanningService);
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createQueryFacade(), "session-1", request);
        EncryptWorkflowRequest actualRequest = (EncryptWorkflowRequest) actual.getRequest();
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getClarifiedIntent().getOperationType(), is("create"));
        assertTrue(actualRequest.getOptions().getRequiresDecrypt());
        assertTrue(actualRequest.getOptions().getRequiresEqualityFilter());
        assertFalse(actualRequest.getOptions().getRequiresLikeQuery());
        EncryptWorkflowState actualState = (EncryptWorkflowState) actual.getFeatureData();
        assertThat(actualState.getDerivedColumnPlan().getCipherColumnName(), is("phone_cipher"));
    }
    
    @Test
    void assertPlanAppliesLikeQueryAlgorithmCandidate() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of(
                Map.of("type", "AES", "supports_like", false),
                Map.of("type", "FPE", "supports_like", true)));
        DerivedColumnNamingService derivedColumnNamingService = mock(DerivedColumnNamingService.class);
        when(derivedColumnNamingService.createPlan(any(), any(), any())).thenReturn(createLikeQueryDerivedColumnPlan());
        PhysicalDDLPlanningService physicalDDLPlanningService = mock(PhysicalDDLPlanningService.class);
        when(physicalDDLPlanningService.planAddColumnArtifacts(any(), any(), any(), any()))
                .thenReturn(List.of(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN phone_like_query VARCHAR(32)", 10)));
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptRule(any(), any(), any())).thenReturn(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, new EncryptAlgorithmRecommendationService(),
                new EncryptAlgorithmPropertyTemplateService(), derivedColumnNamingService, physicalDDLPlanningService, mock(IndexPlanningService.class),
                ruleDistSQLPlanningService);
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setRequiresLikeQuery(true);
        request.getOptions().setAllowIndexDDL(false);
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createQueryFacade(), "session-1", request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getAlgorithmCandidates().size(), is(2));
        assertThat(actual.getAlgorithmCandidates().get(1).getAlgorithmRole(), is("like_query"));
        EncryptWorkflowRequest actualRequest = (EncryptWorkflowRequest) actual.getRequest();
        assertThat(actualRequest.getOptions().getLikeQueryAlgorithmType(), is("FPE"));
    }
    
    @Test
    void assertPlanRequiresMissingProperties() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of());
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "AES", true, true, false, 100, "reason", "")));
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of(
                new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", "")));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                mock(DerivedColumnNamingService.class), mock(PhysicalDDLPlanningService.class), mock(IndexPlanningService.class),
                mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createQueryFacade(), "session-1", createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().get(0), is("Please provide property `aes-key-value`."));
    }
    
    @Test
    void assertPlanCreatesArtifactsWithoutIndexDdl() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of());
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "AES", true, true, false, 100, "reason", "")));
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of());
        DerivedColumnNamingService derivedColumnNamingService = mock(DerivedColumnNamingService.class);
        when(derivedColumnNamingService.createPlan(any(), any(), any())).thenReturn(createDerivedColumnPlan());
        PhysicalDDLPlanningService physicalDDLPlanningService = mock(PhysicalDDLPlanningService.class);
        when(physicalDDLPlanningService.planAddColumnArtifacts(any(), any(), any(), any()))
                .thenReturn(List.of(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN phone_cipher VARCHAR(32)", 10)));
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptRule(any(), any(), any())).thenReturn(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        IndexPlanningService indexPlanningService = mock(IndexPlanningService.class);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                derivedColumnNamingService, physicalDDLPlanningService, indexPlanningService, ruleDistSQLPlanningService);
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setAllowIndexDDL(false);
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createQueryFacade(), "session-1", request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getDdlArtifacts().size(), is(1));
        assertThat(actual.getRuleArtifacts().size(), is(1));
        assertTrue(actual.getIndexPlans().isEmpty());
        verify(indexPlanningService, never()).planIndexes(any(), any(), any());
    }
    
    @Test
    void assertPlanWarnsWhenColumnDefinitionUnavailable() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of());
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any()))
                .thenReturn(List.of(new AlgorithmCandidate("primary", "FPE", null, null, true, 100, "reason", "")));
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of());
        DerivedColumnNamingService derivedColumnNamingService = mock(DerivedColumnNamingService.class);
        when(derivedColumnNamingService.createPlan(any(), any(), any())).thenReturn(createDerivedColumnPlan());
        PhysicalDDLPlanningService physicalDDLPlanningService = mock(PhysicalDDLPlanningService.class);
        when(physicalDDLPlanningService.planAddColumnArtifacts(any(), any(), any(), any()))
                .thenReturn(List.of(new DDLArtifact("add-column", "ALTER TABLE orders ADD COLUMN phone_cipher", 10)));
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptRule(any(), any(), any())).thenReturn(new RuleArtifact("create", "CREATE ENCRYPT RULE orders"));
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, algorithmRecommendationService, propertyTemplateService,
                derivedColumnNamingService, physicalDDLPlanningService, mock(IndexPlanningService.class), ruleDistSQLPlanningService);
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setAllowIndexDDL(false);
        WorkflowContextSnapshot actual = service.plan(workflowSessionContext, createResolvedMetadataQueryFacade(), createUnavailableColumnDefinitionQueryFacade(),
                "session-1", request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.LOGICAL_METADATA_UNAVAILABLE));
    }
    
    private MCPMetadataQueryFacade createResolvedMetadataQueryFacade() {
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryDatabase("logic_db")).thenReturn(Optional.of(createDatabaseMetadata()));
        when(metadataQueryFacade.queryTable("logic_db", "public", "orders")).thenReturn(Optional.of(createTableMetadata()));
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(createColumnMetadata()));
        when(metadataQueryFacade.queryTableColumns("logic_db", "public", "orders")).thenReturn(List.of(createColumnMetadata()));
        return metadataQueryFacade;
    }
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.queryColumnDefinition("logic_db", "public", "orders", "phone")).thenReturn("VARCHAR(32)");
        return result;
    }
    
    private MCPFeatureQueryFacade createUnavailableColumnDefinitionQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.queryColumnDefinition("logic_db", "public", "orders", "phone")).thenThrow(new IllegalStateException("metadata unavailable"));
        return result;
    }
    
    private EncryptWorkflowRequest createRequest(final String operationType) {
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("orders");
        result.setColumn("phone");
        result.setOperationType(operationType);
        result.getOptions().setRequiresDecrypt(true);
        result.getOptions().setRequiresEqualityFilter(false);
        result.getOptions().setRequiresLikeQuery(false);
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
        return new MCPDatabaseMetadata("logic_db", "MySQL", "8.0", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(createTableMetadata()), List.of(), List.of())));
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
    
    private DerivedColumnPlan createLikeQueryDerivedColumnPlan() {
        DerivedColumnPlan result = createDerivedColumnPlan();
        result.setLikeQueryColumnRequired(true);
        result.setLikeQueryColumnName("phone_like_query");
        return result;
    }
    
    private EncryptWorkflowPlanningService createService(final EncryptRuleInspectionService ruleInspectionService,
                                                         final EncryptAlgorithmRecommendationService algorithmRecommendationService,
                                                         final EncryptAlgorithmPropertyTemplateService algorithmPropertyTemplateService,
                                                         final DerivedColumnNamingService derivedColumnNamingService,
                                                         final PhysicalDDLPlanningService physicalDDLPlanningService,
                                                         final IndexPlanningService indexPlanningService,
                                                         final EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService) {
        EncryptWorkflowPlanningService result = new EncryptWorkflowPlanningService();
        try {
            setField(result, "ruleInspectionService", ruleInspectionService);
            setField(result, "algorithmRecommendationService", algorithmRecommendationService);
            setField(result, "algorithmPropertyTemplateService", algorithmPropertyTemplateService);
            setField(result, "derivedColumnNamingService", derivedColumnNamingService);
            setField(result, "physicalDDLPlanningService", physicalDDLPlanningService);
            setField(result, "indexPlanningService", indexPlanningService);
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
                Arguments.of("create from default verb", "encrypt phone column", false, "create", "phone", "clarifying", true),
                Arguments.of("alter from english verb", "update phone number encrypt rule", true, "alter", "phone", "clarifying", true),
                Arguments.of("drop from english verb", "delete phone number encrypt rule", true, "drop", "phone", "planned", false));
    }
}
