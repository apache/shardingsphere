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
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptWorkflowPlanningServiceTest {
    
    @Test
    void assertPlanRejectsMissingPlanningContext() throws ReflectiveOperationException {
        EncryptWorkflowPlanningService service = createService(mock(EncryptRuleInspectionService.class), mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1",
                new EncryptWorkflowRequest());
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsMissingTableAndColumn() throws ReflectiveOperationException {
        EncryptWorkflowPlanningService service = createService(mock(EncryptRuleInspectionService.class), mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(EncryptRuleDistSQLPlanningService.class));
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setDatabase("logic_db");
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertTrue(actual.getClarifiedIntent().getClarificationMessages().contains("Please specify target table."));
        assertTrue(actual.getClarifiedIntent().getClarificationMessages().contains("Please specify target column."));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.TABLE_REQUIRED));
        assertThat(actual.getIssues().get(1).getCode(), is(WorkflowIssueCode.COLUMN_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsLifecycleMismatchForCreate() throws ReflectiveOperationException {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.getDatabaseType("logic_db")).thenReturn("MySQL");
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(EncryptRuleDistSQLPlanningService.class));
        EncryptWorkflowRequest request = createRequest("create");
        request.setColumn("Phone");
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), queryFacade, "session-1", request);
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanDropWorkflow() throws ReflectiveOperationException {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptDropRule(any(), any(), any())).thenReturn(List.of(new RuleArtifact("drop", "DROP ENCRYPT RULE orders")));
        WorkflowContextSnapshot actual = createService(ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), ruleDistSQLPlanningService)
                .plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", createRequest("drop"));
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
        assertTrue(actual.getIssues().isEmpty());
        assertTrue(actual.getDdlArtifacts().isEmpty());
        assertTrue(actual.getIndexPlans().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertPlanWithNaturalLanguageInferenceArguments")
    void assertPlanWithNaturalLanguageInference(final String name, final String naturalLanguageIntent, final boolean ruleExists,
                                                final String expectedOperationType, final String expectedFieldSemantics, final String expectedStatus) throws ReflectiveOperationException {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(ruleExists ? List.of(Map.of("logic_column", "phone")) : List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of(Map.of("type", "AES", "supports_like", false)));
        EncryptWorkflowRequest request = createNaturalLanguageRequest(naturalLanguageIntent);
        request.getOptions().setCipherColumnName("phone_cipher");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, new EncryptAlgorithmRecommendationService(), new EncryptAlgorithmPropertyTemplateService(),
                new EncryptRuleDistSQLPlanningService()).plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getClarifiedIntent().getOperationType(), is(expectedOperationType));
        assertThat(actual.getClarifiedIntent().getFieldSemantics(), is(expectedFieldSemantics));
        assertThat(actual.getStatus(), is(expectedStatus));
    }
    
    @Test
    void assertPlanStopsOnBlockingAlgorithmIssue() throws ReflectiveOperationException {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of());
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any())).thenAnswer(invocation -> {
            List<WorkflowIssue> issues = invocation.getArgument(2);
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm", "missing", "fix", false, Map.of()));
            return List.of();
        });
        WorkflowContextSnapshot actual = createService(ruleInspectionService, algorithmRecommendationService, mock(EncryptAlgorithmPropertyTemplateService.class),
                mock(EncryptRuleDistSQLPlanningService.class)).plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1",
                        createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().get(0), is("Please use an encrypt algorithm that is visible in the current Proxy and satisfies the requirements."));
    }
    
    @Test
    void assertPlanRequiresExplicitCipherColumn() throws ReflectiveOperationException {
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setCipherColumnName("");
        WorkflowContextSnapshot actual = planWithPrimaryCandidate(request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(actual.getIssues().get(0).getDetails().get("missing_inputs"), is(List.of("cipher_column_name")));
    }
    
    @Test
    void assertPlanRequiresExplicitEqualityInputs() throws ReflectiveOperationException {
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setRequiresEqualityFilter(true);
        request.getOptions().setAssistedQueryColumnName("");
        request.getOptions().setAssistedQueryAlgorithmType("");
        WorkflowContextSnapshot actual = planWithPrimaryCandidate(request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(actual.getIssues().get(0).getDetails().get("missing_inputs"), is(List.of("assisted_query_column_name", "assisted_query_algorithm_type")));
    }
    
    @Test
    void assertPlanDoesNotApplyLikeQueryCandidateWithoutExplicitInput() throws ReflectiveOperationException {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(List.of(
                Map.of("type", "AES", "supports_like", false),
                Map.of("type", "FPE", "supports_like", true)));
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of());
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setRequiresLikeQuery(true);
        request.getOptions().setLikeQueryColumnName("phone_like_query");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, new EncryptAlgorithmRecommendationService(), propertyTemplateService, new EncryptRuleDistSQLPlanningService())
                .plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(((EncryptWorkflowRequest) actual.getRequest()).getOptions().getLikeQueryAlgorithmType(), is(""));
        assertThat(actual.getIssues().get(0).getDetails().get("missing_inputs"), is(List.of("like_query_algorithm_type")));
    }
    
    @Test
    void assertPlanCreatesOnlyRuleArtifact() throws ReflectiveOperationException {
        EncryptWorkflowRequest request = createRequest("create");
        WorkflowContextSnapshot actual = planWithPrimaryCandidate(request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
        assertTrue(actual.getDdlArtifacts().isEmpty());
        assertTrue(actual.getIndexPlans().isEmpty());
    }
    
    @Test
    void assertPlanAddsColumnToExistingTableRule() throws ReflectiveOperationException {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old")));
        EncryptWorkflowRequest request = createRequest("create");
        request.setColumn("phone");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), createEmptyPropertyTemplateService(), new EncryptRuleDistSQLPlanningService())
                .plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getIssues().size(), is(0));
        assertThat(actual.getRuleArtifacts().size(), is(1));
        assertTrue(actual.getRuleArtifacts().get(0).getSql().contains("NAME=email"));
        assertTrue(actual.getRuleArtifacts().get(0).getSql().contains("NAME=phone"));
    }
    
    @Test
    void assertPlanRequiresMissingProperties() throws ReflectiveOperationException {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", "")));
        WorkflowContextSnapshot actual = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), propertyTemplateService, mock(EncryptRuleDistSQLPlanningService.class))
                .plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", createRequestWithoutProperties());
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().get(0).getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().get(0), is("Please provide property `aes-key-value`."));
    }
    
    private WorkflowContextSnapshot planWithPrimaryCandidate(final EncryptWorkflowRequest request) throws ReflectiveOperationException {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        return createService(ruleInspectionService, createPrimaryCandidateRecommendation(), createEmptyPropertyTemplateService(), new EncryptRuleDistSQLPlanningService())
                .plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", request);
    }
    
    private EncryptAlgorithmRecommendationService createPrimaryCandidateRecommendation() {
        EncryptAlgorithmRecommendationService result = mock(EncryptAlgorithmRecommendationService.class);
        when(result.recommendEncryptAlgorithms(any(), any(), any())).thenReturn(List.of(new AlgorithmCandidate("primary", "AES", true, true, false, 100, "reason", "")));
        return result;
    }
    
    private EncryptAlgorithmPropertyTemplateService createEmptyPropertyTemplateService() {
        EncryptAlgorithmPropertyTemplateService result = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(result.findRequirements(any(), any(), any())).thenReturn(List.of());
        return result;
    }
    
    private EncryptWorkflowRequest createRequest(final String operationType) {
        EncryptWorkflowRequest result = createRequestWithoutProperties();
        result.setOperationType(operationType);
        result.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        return result;
    }
    
    private EncryptWorkflowRequest createRequestWithoutProperties() {
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("orders");
        result.setColumn("phone");
        result.setOperationType("create");
        result.setAlgorithmType("AES");
        result.getOptions().setCipherColumnName("phone_cipher");
        result.getOptions().setRequiresDecrypt(true);
        result.getOptions().setRequiresEqualityFilter(false);
        result.getOptions().setRequiresLikeQuery(false);
        return result;
    }
    
    private EncryptWorkflowRequest createNaturalLanguageRequest(final String naturalLanguageIntent) {
        EncryptWorkflowRequest result = createRequestWithoutProperties();
        result.setOperationType("");
        result.setNaturalLanguageIntent(naturalLanguageIntent);
        return result;
    }
    
    private EncryptWorkflowPlanningService createService(final EncryptRuleInspectionService ruleInspectionService,
                                                         final EncryptAlgorithmRecommendationService algorithmRecommendationService,
                                                         final EncryptAlgorithmPropertyTemplateService algorithmPropertyTemplateService,
                                                         final EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService) throws ReflectiveOperationException {
        EncryptWorkflowPlanningService result = new EncryptWorkflowPlanningService();
        setField(result, "ruleInspectionService", ruleInspectionService);
        setField(result, "algorithmRecommendationService", algorithmRecommendationService);
        setField(result, "algorithmPropertyTemplateService", algorithmPropertyTemplateService);
        setField(result, "ruleDistSQLPlanningService", ruleDistSQLPlanningService);
        return result;
    }
    
    private void setField(final Object target, final String fieldName, final Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        Plugins.getMemberAccessor().set(field, target, value);
    }
    
    private static Stream<Arguments> assertPlanWithNaturalLanguageInferenceArguments() {
        return Stream.of(
                Arguments.of("create from default verb", "encrypt phone column", false, "create", "phone", "planned"),
                Arguments.of("alter from english verb", "update phone number encrypt rule", true, "alter", "phone", "planned"),
                Arguments.of("drop from english verb", "delete phone number encrypt rule", true, "drop", "phone", "planned"));
    }
}
