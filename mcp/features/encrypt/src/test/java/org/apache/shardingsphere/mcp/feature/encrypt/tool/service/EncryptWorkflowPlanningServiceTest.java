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

import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.feature.encrypt.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata.Nullability;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowQueryResult;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

class EncryptWorkflowPlanningServiceTest {
    
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
    void assertPlanRejectsMissingPlanningContext() {
        EncryptWorkflowPlanningService service = createService(mock(EncryptRuleInspectionService.class), mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), mock(MCPMetadataQueryFacade.class), createQueryFacade(),
                new EncryptWorkflowRequest());
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsMissingTableAndColumn() {
        EncryptWorkflowPlanningService service = createService(mock(EncryptRuleInspectionService.class), mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(EncryptRuleDistSQLPlanningService.class));
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setDatabase("logic_db");
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertTrue(actual.getClarifiedIntent().getClarificationMessages().contains("Please specify target table."));
        assertTrue(actual.getClarifiedIntent().getClarificationMessages().contains("Please specify target column."));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.TABLE_REQUIRED));
        assertThat(actual.getIssues().get(1).getCode(), is(WorkflowIssueCode.COLUMN_REQUIRED));
    }
    
    @Test
    void assertPlanRejectsMissingLogicalColumn() {
        MCPMetadataQueryFacade metadataQueryFacade = createMetadataQueryFacade();
        when(metadataQueryFacade.querySchemas(any())).thenReturn(List.of(
                new ShardingSphereSchema("public", mock(DatabaseType.class), List.of(new ShardingSphereTable("orders", List.of(), List.of(), List.of(), TableType.TABLE)), List.of())));
        when(metadataQueryFacade.queryTableColumns(any(), any(), any())).thenReturn(List.of());
        EncryptWorkflowPlanningService service = createService(mock(EncryptRuleInspectionService.class), mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(EncryptRuleDistSQLPlanningService.class));
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), metadataQueryFacade, createQueryFacade(), createRequest("create"));
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.COLUMN_NOT_FOUND));
    }
    
    @Test
    void assertPlanRejectsLifecycleMismatchForCreate() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        EncryptWorkflowPlanningService service = createService(ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(EncryptRuleDistSQLPlanningService.class));
        EncryptWorkflowRequest request = createRequest("create");
        WorkflowContextSnapshot actual = service.plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), request);
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanDropWorkflow() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of("logic_column", "phone")));
        EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService = mock(EncryptRuleDistSQLPlanningService.class);
        when(ruleDistSQLPlanningService.planEncryptDropRule(any())).thenReturn(List.of(new RuleArtifact("drop", "DROP ENCRYPT RULE `orders`")));
        WorkflowContextSnapshot actual = createService(ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), ruleDistSQLPlanningService)
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), createRequest("drop"));
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.ENCRYPT_DROP_SCOPE_LIMITED));
        assertThat(actual.getIssues().size(), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertPlanWithNaturalLanguageInferenceArguments")
    void assertPlanWithNaturalLanguageInference(final String name, final String naturalLanguageIntent, final boolean ruleExists,
                                                final String expectedOperationType, final String expectedFieldSemantics, final String expectedStatus) {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(ruleExists ? List.of(Map.of("logic_column", "phone")) : List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(WorkflowQueryResult.confirmed(List.of(Map.of("type", "AES", "supports_like", false))));
        EncryptWorkflowRequest request = createNaturalLanguageRequest(naturalLanguageIntent);
        request.getOptions().setCipherColumnName("phone_cipher");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, new EncryptAlgorithmRecommendationService(), new EncryptAlgorithmPropertyTemplateService(),
                new EncryptRuleDistSQLPlanningService()).plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), request);
        assertThat(actual.getClarifiedIntent().getOperationType(), is(expectedOperationType));
        assertThat(actual.getClarifiedIntent().getFieldSemantics(), is(expectedFieldSemantics));
        assertThat(actual.getStatus(), is(expectedStatus));
        if ("failed".equals(expectedStatus)) {
            assertRuleDistSQLOnlyPayloadClearsOperationType(actual);
        }
    }
    
    @Test
    void assertPlanStopsOnBlockingAlgorithmIssue() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(WorkflowQueryResult.confirmed(List.of()));
        EncryptAlgorithmRecommendationService algorithmRecommendationService = mock(EncryptAlgorithmRecommendationService.class);
        when(algorithmRecommendationService.recommendEncryptAlgorithms(any(), any(), any())).thenAnswer(invocation -> {
            List<WorkflowIssue> issues = invocation.getArgument(2);
            issues.add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm", "missing", "fix", false, Map.of()));
            return List.of();
        });
        WorkflowContextSnapshot actual = createService(ruleInspectionService, algorithmRecommendationService, mock(EncryptAlgorithmPropertyTemplateService.class),
                mock(EncryptRuleDistSQLPlanningService.class)).plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(),
                        createRequest("create"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().getFirst(), is("Please use an encrypt algorithm that is visible in the current Proxy and satisfies the requirements."));
    }
    
    @Test
    void assertPlanRequiresExplicitCipherColumn() {
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setCipherColumnName("");
        WorkflowContextSnapshot actual = planWithPrimaryCandidate(request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(actual.getIssues().getFirst().getDetails().get("missing_inputs"), is(List.of("cipher_column_name")));
    }
    
    @Test
    void assertPlanRequiresExplicitEqualityInputs() {
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setRequiresEqualityFilter(true);
        request.getOptions().setAssistedQueryColumnName("");
        request.getOptions().setAssistedQueryAlgorithmType("");
        WorkflowContextSnapshot actual = planWithPrimaryCandidate(request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(actual.getIssues().getFirst().getDetails().get("missing_inputs"), is(List.of("assisted_query_column_name", "assisted_query_algorithm_type")));
    }
    
    @Test
    void assertPlanDoesNotApplyLikeQueryCandidateWithoutExplicitInput() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        when(ruleInspectionService.queryEncryptAlgorithms(any())).thenReturn(WorkflowQueryResult.confirmed(List.of(
                Map.of("type", "AES", "supports_like", false),
                Map.of("type", "FPE", "supports_like", true))));
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of());
        EncryptWorkflowRequest request = createRequest("create");
        request.getOptions().setRequiresLikeQuery(true);
        request.getOptions().setLikeQueryColumnName("phone_like_query");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, new EncryptAlgorithmRecommendationService(), propertyTemplateService, new EncryptRuleDistSQLPlanningService())
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(((EncryptWorkflowRequest) actual.getRequest()).getOptions().getLikeQueryAlgorithmType(), is("FPE"));
    }
    
    @Test
    void assertPlanCreatesRuleArtifactsOnly() {
        EncryptWorkflowRequest request = createRequest("create");
        WorkflowContextSnapshot actual = planWithPrimaryCandidate(request);
        assertThat(actual.getStatus(), is("planned"));
        assertThat(actual.getRuleArtifacts().size(), is(1));
    }
    
    @Test
    void assertPlanEncryptRuleWithReservedColumnAndDefaultDigest() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        EncryptWorkflowRequest request = createRequest("create");
        request.setTable("t_user");
        request.setColumn("name");
        request.getOptions().setCipherColumnName("name_cipher");
        MCPMetadataQueryFacade metadataQueryFacade = createMetadataQueryFacade();
        when(metadataQueryFacade.querySchemas(any())).thenReturn(List.of(new ShardingSphereSchema(
                "public", mock(DatabaseType.class), List.of(createTableMetadata("t_user")), List.of())));
        WorkflowContextSnapshot actual = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), new EncryptAlgorithmPropertyTemplateService(),
                new EncryptRuleDistSQLPlanningService()).plan(new TestWorkflowSessionContext(), metadataQueryFacade, createQueryFacade(), request);
        String actualSQL = actual.getRuleArtifacts().getFirst().getSql();
        assertThat(actual.getStatus(), is("planned"));
        assertTrue(actualSQL.startsWith("CREATE ENCRYPT RULE `t_user`"));
        assertTrue(actualSQL.contains("NAME=`name`"));
        assertTrue(actualSQL.contains("CIPHER=`name_cipher`"));
        assertTrue(actualSQL.contains("TYPE(NAME='aes'"));
        assertTrue(actualSQL.contains("'digest-algorithm-name'='SHA-1'"));
    }
    
    @Test
    void assertPlanRejectsAddingColumnToExistingTableRule() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old")));
        EncryptWorkflowRequest request = createRequest("create");
        request.setColumn("phone");
        WorkflowContextSnapshot actual = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), createEmptyPropertyTemplateService(), new EncryptRuleDistSQLPlanningService())
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), request);
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.ENCRYPT_RULE_REWRITE_LIMITED));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanRejectsDroppingColumnFromExistingTableRule() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(
                Map.of("logic_column", "phone", "cipher_column", "phone_cipher"),
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old")));
        WorkflowContextSnapshot actual = createService(ruleInspectionService, mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), new EncryptRuleDistSQLPlanningService())
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), createRequest("drop"));
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.ENCRYPT_RULE_REWRITE_LIMITED));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanRejectsUnsupportedOperationType() {
        EncryptWorkflowRequest request = createRequest("replace");
        WorkflowContextSnapshot actual = createService(mock(EncryptRuleInspectionService.class), mock(EncryptAlgorithmRecommendationService.class),
                mock(EncryptAlgorithmPropertyTemplateService.class), mock(EncryptRuleDistSQLPlanningService.class))
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), request);
        assertThat(actual.getStatus(), is("failed"));
        assertThat(actual.getClarifiedIntent().getOperationType(), is(""));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        assertRuleDistSQLOnlyPayloadDoesNotExpose(actual, "replace");
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanRequiresMissingProperties() {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        EncryptAlgorithmPropertyTemplateService propertyTemplateService = mock(EncryptAlgorithmPropertyTemplateService.class);
        when(propertyTemplateService.findRequirements(any(), any(), any())).thenReturn(List.of(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "key", "")));
        WorkflowContextSnapshot actual = createService(ruleInspectionService, createPrimaryCandidateRecommendation(), propertyTemplateService, mock(EncryptRuleDistSQLPlanningService.class))
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), createRequestWithoutProperties());
        assertThat(actual.getStatus(), is("clarifying"));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
        assertThat(actual.getClarifiedIntent().getClarificationMessages().getFirst(), is("Please provide property `aes-key-value`."));
    }
    
    private WorkflowContextSnapshot planWithPrimaryCandidate(final EncryptWorkflowRequest request) {
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        return createService(ruleInspectionService, createPrimaryCandidateRecommendation(), createEmptyPropertyTemplateService(), new EncryptRuleDistSQLPlanningService())
                .plan(new TestWorkflowSessionContext(), createMetadataQueryFacade(), createQueryFacade(), request);
    }
    
    private EncryptAlgorithmRecommendationService createPrimaryCandidateRecommendation() {
        EncryptAlgorithmRecommendationService result = mock(EncryptAlgorithmRecommendationService.class);
        when(result.recommendEncryptAlgorithms(any(), any(), any())).thenReturn(List.of(AlgorithmCandidate.builder()
                .algorithmRole("primary").algorithmType("AES").supportsDecrypt(true).supportsEquivalentFilter(true).supportsLike(false)
                .recommendationScore(100).recommendationReason("reason").riskNotes("").build()));
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
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.SCHEMA, "public", "public")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "orders", "orders")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "phone", "phone")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "t_user", "t_user")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "name", "name")).thenReturn(true);
        return result;
    }
    
    private MCPMetadataQueryFacade createMetadataQueryFacade() {
        MCPMetadataQueryFacade result = mock(MCPMetadataQueryFacade.class);
        when(result.queryDatabase(any())).thenReturn(Optional.of(createDatabaseMetadata()));
        when(result.querySchemas(any())).thenReturn(List.of(createSchemaMetadata()));
        when(result.queryTableColumns(any(), any(), eq("orders"))).thenReturn(List.of(
                createColumnMetadata("orders", "phone"), createColumnMetadata("orders", "email")));
        when(result.queryTableColumns(any(), any(), eq("t_user"))).thenReturn(List.of(createColumnMetadata("t_user", "name")));
        return result;
    }
    
    private RuntimeDatabaseProfile createDatabaseMetadata() {
        return new RuntimeDatabaseProfile("logic_db", "FixtureDB", "1.0", TransactionCapability.LOCAL_WITH_SAVEPOINT,
                new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newInsensitivePolicySet()));
    }
    
    private ShardingSphereSchema createSchemaMetadata() {
        return new ShardingSphereSchema("public", mock(DatabaseType.class), List.of(createTableMetadata()), List.of());
    }
    
    private ShardingSphereTable createTableMetadata() {
        return createTableMetadata("orders");
    }
    
    private ShardingSphereTable createTableMetadata(final String tableName) {
        return new ShardingSphereTable(tableName, List.of(), List.of(), List.of(), TableType.TABLE);
    }
    
    private MCPColumnMetadata createColumnMetadata(final String tableName, final String columnName) {
        return new MCPColumnMetadata(tableName, columnName, 1, java.sql.Types.VARCHAR, "VARCHAR", Nullability.NULLABLE);
    }
    
    private EncryptWorkflowPlanningService createService(final EncryptRuleInspectionService ruleInspectionService,
                                                         final EncryptAlgorithmRecommendationService algorithmRecommendationService,
                                                         final EncryptAlgorithmPropertyTemplateService algorithmPropertyTemplateService,
                                                         final EncryptRuleDistSQLPlanningService ruleDistSQLPlanningService) {
        try (
                MockedConstruction<EncryptRuleInspectionService> ignoredInspectionService = mockConstruction(
                        EncryptRuleInspectionService.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(ruleInspectionService)));
                MockedConstruction<EncryptAlgorithmRecommendationService> ignoredRecommendationService = mockConstruction(
                        EncryptAlgorithmRecommendationService.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(algorithmRecommendationService)));
                MockedConstruction<EncryptAlgorithmPropertyTemplateService> ignoredPropertyTemplateService = mockConstruction(
                        EncryptAlgorithmPropertyTemplateService.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(algorithmPropertyTemplateService)));
                MockedConstruction<EncryptRuleDistSQLPlanningService> ignoredDistSQLPlanningService = mockConstruction(
                        EncryptRuleDistSQLPlanningService.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(ruleDistSQLPlanningService)))) {
            return new EncryptWorkflowPlanningService();
        }
    }
    
    private void assertRuleDistSQLOnlyPayloadDoesNotExpose(final WorkflowContextSnapshot snapshot, final String term) {
        Map<String, Object> actualPayload = WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest());
        assertFalse(String.valueOf(actualPayload).toLowerCase(Locale.ENGLISH).contains(term));
    }
    
    private void assertRuleDistSQLOnlyPayloadClearsOperationType(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> actualPayload = WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest());
        Map<?, ?> actualIntentInference = (Map<?, ?>) actualPayload.get("intent_inference");
        assertThat(actualIntentInference.get(WorkflowFieldNames.OPERATION_TYPE), is(""));
        assertFalse(((Map<?, ?>) actualIntentInference.get("inferred_values")).containsKey(WorkflowFieldNames.OPERATION_TYPE));
        assertFalse(((Map<?, ?>) actualPayload.get("argument_provenance")).containsKey(WorkflowFieldNames.OPERATION_TYPE));
    }
    
    private static Stream<Arguments> assertPlanWithNaturalLanguageInferenceArguments() {
        return Stream.of(
                Arguments.of("create from default verb", "encrypt phone column", false, "create", "phone", "planned"),
                Arguments.of("unsupported update verb", "update phone number encrypt rule", true, "", "phone", "failed"),
                Arguments.of("drop from english verb", "delete phone number encrypt rule", true, "drop", "phone", "planned"));
    }
}
