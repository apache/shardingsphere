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
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowAlgorithmCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowDefaultAlgorithmWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowQueryResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class ShadowWorkflowPlanningServiceTest {
    
    private MockedConstruction<ShadowInspectionService> mockedInspectionServices;
    
    @BeforeEach
    void setUp() {
        mockedInspectionServices = mockConstruction(ShadowInspectionService.class,
                (mock, context) -> when(mock.queryAlgorithmPlugins(any())).thenReturn(WorkflowQueryResult.fallback(List.of())));
    }
    
    @AfterEach
    void tearDown() {
        mockedInspectionServices.close();
    }
    
    @Test
    void assertPlanRuleCreate() {
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of());
        WorkflowContextSnapshot actual = service.planRule(new TestWorkflowSessionContext(), queryFacade, createRuleRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getAlgorithmCandidates().getFirst().getAlgorithmType(), is("VALUE_MATCH"));
        assertThat(actual.getPropertyRequirements().getFirst().getPropertyKey(), is("operation"));
        assertThat(actual.getResourceUriTemplates(), is(List.of(ShadowFeatureDefinition.STORAGE_UNITS_RESOURCE_URI,
                ShadowFeatureDefinition.SINGLE_TABLES_RESOURCE_URI, ShadowFeatureDefinition.SINGLE_TABLE_RESOURCE_URI)));
        assertTrue(actual.getRuleArtifacts().getFirst().getSql().startsWith("CREATE SHADOW RULE `shadow_rule`"));
    }
    
    @Test
    void assertPlanRuleMissingProperties() {
        ShadowRuleWorkflowRequest request = createRuleRequest();
        request.getAlgorithmProperties().clear();
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planRule(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanRuleWithoutDatabase() {
        ShadowRuleWorkflowRequest request = createRuleRequest();
        request.setDatabase("");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planRule(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanRuleWithUnsupportedIdentifier() {
        ShadowRuleWorkflowRequest request = createRuleRequest();
        request.setTableName("bad`table");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planRule(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertPlanRuleWithUnsupportedDatabaseIdentifier() {
        ShadowRuleWorkflowRequest request = createRuleRequest();
        request.setDatabase("bad`database");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planRule(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertPlanRuleWithoutRuleName() {
        ShadowRuleWorkflowRequest request = createRuleRequest();
        request.setRuleName("");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planRule(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getDetails().get("missing_inputs"), is(List.of("rule")));
    }
    
    @Test
    void assertPlanRuleWhenCreateTargetExists() {
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "shadow_rule", "shadow_rule")).thenReturn(true);
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(Map.of("rule_name", "shadow_rule")));
        WorkflowContextSnapshot actual = service.planRule(new TestWorkflowSessionContext(), queryFacade, createRuleRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanRuleDrop() {
        ShadowRuleWorkflowRequest request = new ShadowRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("shadow_rule");
        request.setOperationType("drop");
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "shadow_rule", "shadow_rule")).thenReturn(true);
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(Map.of("rule_name", "shadow_rule")));
        WorkflowContextSnapshot actual = service.planRule(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("DROP SHADOW RULE `shadow_rule`"));
    }
    
    @Test
    void assertPlanRuleAlter() {
        ShadowRuleWorkflowRequest request = createRuleRequest();
        request.setOperationType("alter");
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "shadow_rule", "shadow_rule")).thenReturn(true);
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of(Map.of("rule_name", "shadow_rule")));
        WorkflowContextSnapshot actual = service.planRule(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertTrue(actual.getRuleArtifacts().getFirst().getSql().startsWith("ALTER SHADOW RULE `shadow_rule`"));
    }
    
    @Test
    void assertPlanRuleWithDefaultSqlHintRecommendation() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        ShadowRuleWorkflowRequest request = createRuleRequest();
        request.setAlgorithmType("");
        request.getAlgorithmProperties().clear();
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        when(inspectionService.queryAlgorithmPlugins(queryFacade)).thenReturn(WorkflowQueryResult.confirmed(List.of(Map.of("type", "SQL_HINT"))));
        when(inspectionService.queryRules(queryFacade, "logic_db")).thenReturn(List.of());
        WorkflowContextSnapshot actual = service.planRule(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getAlgorithmCandidates().getFirst().getAlgorithmType(), is("SQL_HINT"));
        assertTrue(actual.getPropertyRequirements().isEmpty());
    }
    
    @Test
    void assertPlanDefaultAlgorithmDrop() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setDatabase("logic_db");
        request.setOperationType("drop");
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "default_shadow_algorithm")));
        WorkflowContextSnapshot actual = service.planDefaultAlgorithm(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("DROP DEFAULT SHADOW ALGORITHM"));
    }
    
    @Test
    void assertPlanDefaultAlgorithmCreate() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        ShadowDefaultAlgorithmWorkflowRequest request = createDefaultAlgorithmRequest();
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        when(inspectionService.queryAlgorithmPlugins(queryFacade)).thenReturn(WorkflowQueryResult.confirmed(List.of(Map.of("type", "SQL_HINT"))));
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of());
        WorkflowContextSnapshot actual = service.planDefaultAlgorithm(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertTrue(actual.getRuleArtifacts().getFirst().getSql().startsWith("CREATE DEFAULT SHADOW ALGORITHM"));
    }
    
    @Test
    void assertPlanDefaultAlgorithmWithoutDatabase() {
        ShadowDefaultAlgorithmWorkflowRequest request = createDefaultAlgorithmRequest();
        request.setDatabase("");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planDefaultAlgorithm(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanDefaultAlgorithmWithoutAlgorithmType() {
        ShadowDefaultAlgorithmWorkflowRequest request = createDefaultAlgorithmRequest();
        request.setAlgorithmType("");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planDefaultAlgorithm(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        WorkflowIssue actualIssue = actual.getIssues().stream()
                .filter(each -> WorkflowIssueCode.RULE_INPUT_REQUIRED.equals(each.getCode())).findFirst().orElseThrow();
        assertThat(actualIssue.getDetails().get("missing_inputs"), is(List.of("algorithm_type")));
    }
    
    @Test
    void assertPlanDefaultAlgorithmWhenCreateTargetExists() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        ShadowDefaultAlgorithmWorkflowRequest request = createDefaultAlgorithmRequest();
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        when(inspectionService.queryAlgorithmPlugins(queryFacade)).thenReturn(WorkflowQueryResult.confirmed(List.of(Map.of("type", "SQL_HINT"))));
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "default_shadow_algorithm")));
        WorkflowContextSnapshot actual = service.planDefaultAlgorithm(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanDefaultAlgorithmAlter() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        ShadowDefaultAlgorithmWorkflowRequest request = createDefaultAlgorithmRequest();
        request.setOperationType("alter");
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        when(inspectionService.queryAlgorithmPlugins(queryFacade)).thenReturn(WorkflowQueryResult.confirmed(List.of(Map.of("type", "SQL_HINT"))));
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "default_shadow_algorithm")));
        WorkflowContextSnapshot actual = service.planDefaultAlgorithm(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertTrue(actual.getRuleArtifacts().getFirst().getSql().startsWith("ALTER DEFAULT SHADOW ALGORITHM"));
    }
    
    @Test
    void assertRejectNonHintDefaultAlgorithm() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        ShadowDefaultAlgorithmWorkflowRequest request = new ShadowDefaultAlgorithmWorkflowRequest();
        request.setDatabase("logic_db");
        request.setAlgorithmType("VALUE_MATCH");
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        when(getInspectionService().queryAlgorithmPlugins(queryFacade)).thenReturn(WorkflowQueryResult.confirmed(List.of(Map.of("type", "VALUE_MATCH"))));
        WorkflowContextSnapshot actual = service.planDefaultAlgorithm(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_CAPABILITY_CONFLICT));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanAlgorithmCleanup() {
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "unused_algorithm", "unused_algorithm")).thenReturn(true);
        ShadowAlgorithmCleanupWorkflowRequest request = createCleanupRequest();
        when(inspectionService.queryAlgorithms(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "unused_algorithm")));
        when(inspectionService.queryTableRules(queryFacade, "logic_db")).thenReturn(List.of());
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of());
        WorkflowContextSnapshot actual = service.planAlgorithmCleanup(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("DROP SHADOW ALGORITHM `unused_algorithm`"));
    }
    
    @Test
    void assertPlanAlgorithmCleanupReferenced() {
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "unused_algorithm", "unused_algorithm")).thenReturn(true);
        ShadowAlgorithmCleanupWorkflowRequest request = createCleanupRequest();
        when(inspectionService.queryAlgorithms(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "unused_algorithm")));
        when(inspectionService.queryTableRules(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "other_algorithm, , unused_algorithm")));
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of());
        WorkflowContextSnapshot actual = service.planAlgorithmCleanup(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @Test
    void assertPlanAlgorithmCleanupWithoutName() {
        ShadowAlgorithmCleanupWorkflowRequest request = createCleanupRequest();
        request.setAlgorithmName("");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planAlgorithmCleanup(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getDetails().get("missing_inputs"), is(List.of("algorithm_name")));
    }
    
    @Test
    void assertPlanAlgorithmCleanupWithoutDatabase() {
        ShadowAlgorithmCleanupWorkflowRequest request = createCleanupRequest();
        request.setDatabase("");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planAlgorithmCleanup(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertPlanAlgorithmCleanupRejectsUnsupportedName() {
        ShadowAlgorithmCleanupWorkflowRequest request = createCleanupRequest();
        request.setAlgorithmName("unused_algorithm\ndrop");
        WorkflowContextSnapshot actual = new ShadowWorkflowPlanningService().planAlgorithmCleanup(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertPlanAlgorithmCleanupWhenNotConfigured() {
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(inspectionService.queryAlgorithms(queryFacade, "logic_db")).thenReturn(List.of());
        when(inspectionService.queryTableRules(queryFacade, "logic_db")).thenReturn(List.of());
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of());
        WorkflowContextSnapshot actual = service.planAlgorithmCleanup(new TestWorkflowSessionContext(), queryFacade, createCleanupRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanAlgorithmCleanupWhenDefaultReferenced() {
        ShadowWorkflowPlanningService service = new ShadowWorkflowPlanningService();
        ShadowInspectionService inspectionService = getInspectionService();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "unused_algorithm", "unused_algorithm")).thenReturn(true);
        when(inspectionService.queryAlgorithms(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "unused_algorithm")));
        when(inspectionService.queryTableRules(queryFacade, "logic_db")).thenReturn(List.of());
        when(inspectionService.queryDefaultAlgorithm(queryFacade, "logic_db")).thenReturn(List.of(Map.of("shadow_algorithm_name", "unused_algorithm")));
        WorkflowContextSnapshot actual = service.planAlgorithmCleanup(new TestWorkflowSessionContext(), queryFacade, createCleanupRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    private ShadowInspectionService getInspectionService() {
        return mockedInspectionServices.constructed().getFirst();
    }
    
    private ShadowRuleWorkflowRequest createRuleRequest() {
        ShadowRuleWorkflowRequest result = new ShadowRuleWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("shadow_rule");
        result.setSourceStorageUnit("demo_ds");
        result.setShadowStorageUnit("demo_ds_shadow");
        result.setTableName("t_order");
        result.setAlgorithmType("VALUE_MATCH");
        result.putAlgorithmProperties(Map.of("operation", "insert", "column", "user_id", "value", "1"));
        return result;
    }
    
    private ShadowAlgorithmCleanupWorkflowRequest createCleanupRequest() {
        ShadowAlgorithmCleanupWorkflowRequest result = new ShadowAlgorithmCleanupWorkflowRequest();
        result.setDatabase("logic_db");
        result.setAlgorithmName("unused_algorithm");
        return result;
    }
    
    private ShadowDefaultAlgorithmWorkflowRequest createDefaultAlgorithmRequest() {
        ShadowDefaultAlgorithmWorkflowRequest result = new ShadowDefaultAlgorithmWorkflowRequest();
        result.setDatabase("logic_db");
        result.setAlgorithmType("SQL_HINT");
        return result;
    }
}
