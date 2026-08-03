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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadwriteSplittingRuleWorkflowPlanningServiceTest {
    
    @Test
    void assertPlanCreateRule() {
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), createRuleRequest("create"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getWorkflowKind().getValue(), is("readwrite.rule"));
        assertThat(actual.getResourceUriTemplates(), is(List.of(ReadwriteSplittingFeatureDefinition.STORAGE_UNITS_RESOURCE_URI)));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(),
                is("CREATE READWRITE_SPLITTING RULE `readwrite_ds` (WRITE_STORAGE_UNIT=`write_ds`, READ_STORAGE_UNITS(`read_ds_0`), TRANSACTIONAL_READ_QUERY_STRATEGY='DYNAMIC')"));
    }
    
    @Test
    void assertPlanClarifiesNaturalLanguageWithoutOperation() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("");
        request.setNaturalLanguageIntent("opaque request text");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getClarifiedIntent().getUnresolvedFields(), is(List.of(WorkflowFieldNames.OPERATION_TYPE)));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanCreateRuleWithLoadBalancerProperties() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setLoadBalancerType("WEIGHT");
        request.putLoadBalancerProperties(Map.of("read_ds_0", "2"));
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), request);
        assertThat(actual.getAlgorithmCandidates().getFirst().getAlgorithmType(), is("WEIGHT"));
        assertThat(actual.getPropertyRequirements().getFirst().getPropertyKey(), is("read_ds_0"));
        String expectedSQL = "CREATE READWRITE_SPLITTING RULE `readwrite_ds` (WRITE_STORAGE_UNIT=`write_ds`, READ_STORAGE_UNITS(`read_ds_0`), "
                + "TRANSACTIONAL_READ_QUERY_STRATEGY='DYNAMIC', TYPE(NAME='weight', PROPERTIES('read_ds_0'='2')))";
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is(expectedSQL));
    }
    
    @Test
    void assertPlanCreateRuleClarifiesMissingLoadBalancerProperties() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setLoadBalancerType("WEIGHT");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
        assertThat(actual.getPropertyRequirements().getFirst().getPropertyKey(), is("read_ds_0"));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanCreateRuleWithLoadBalancerRecommendation() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setLoadBalancerType("RANDOM");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getAlgorithmCandidates().getFirst().getAlgorithmType(), is("RANDOM"));
        assertTrue(actual.getPropertyRequirements().isEmpty());
    }
    
    @Test
    void assertPlanCreateRuleRejectsUnavailableLoadBalancer() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setLoadBalancerType("UNAVAILABLE");
        MCPFeatureQueryFacade queryFacade = mockRuleQueryFacade(List.of());
        when(queryFacade.queryWithAnyDatabase("SHOW LOAD BALANCE ALGORITHM PLUGINS")).thenReturn(List.of(Map.of("type", "RANDOM")));
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), queryFacade, request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.ALGORITHM_NOT_FOUND));
    }
    
    @Test
    void assertPlanAlterRule() {
        WorkflowContextSnapshot actual =
                createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of(Map.of("name", "readwrite_ds"))), createRuleRequest("alter"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().getFirst().getOperationType(), is("alter"));
    }
    
    @Test
    void assertPlanDropRule() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setOperationType("drop");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of(Map.of("name", "readwrite_ds"))), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("DROP READWRITE_SPLITTING RULE `readwrite_ds`"));
    }
    
    @Test
    void assertPlanDropClarifiesMissingRule() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setOperationType("drop");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getDetails().get("missing_inputs"), is(List.of("rule")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("missingRuleInputs")
    void assertPlanClarifiesMissingRuleInputs(final String name, final String ruleName, final String writeStorageUnit,
                                              final List<String> readStorageUnits, final String strategy, final List<String> expectedMissingInputs) {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setRuleName(ruleName);
        request.setWriteStorageUnit(writeStorageUnit);
        request.getReadStorageUnits().clear();
        request.setReadStorageUnits(readStorageUnits);
        request.setTransactionalReadQueryStrategy(strategy);
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(actual.getIssues().getFirst().getDetails().get("missing_inputs"), is(expectedMissingInputs));
    }
    
    @Test
    void assertPlanCreateFailsWhenRuleExists() {
        WorkflowContextSnapshot actual =
                createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of(Map.of("name", "readwrite_ds"))), createRuleRequest("create"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanDropFailsWhenRuleDoesNotExist() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setOperationType("drop");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND));
    }
    
    @Test
    void assertPlanAlterFailsWhenRuleDoesNotExist() {
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), createRuleRequest("alter"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanClarifiesMissingDatabase() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setDatabase("");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unsupportedIdentifiers")
    void assertPlanFailsForUnsupportedIdentifier(final String name, final String database, final String ruleName,
                                                 final String writeStorageUnit, final List<String> readStorageUnits) {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setDatabase(database);
        request.setRuleName(ruleName);
        request.setWriteStorageUnit(writeStorageUnit);
        request.setReadStorageUnits(readStorageUnits);
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    private ReadwriteSplittingRuleWorkflowPlanningService createRuleService() {
        return new ReadwriteSplittingRuleWorkflowPlanningService();
    }
    
    private static Stream<Arguments> missingRuleInputs() {
        return Stream.of(
                Arguments.of("missing rule", "", "write_ds", List.of("read_ds_0"), "DYNAMIC", List.of("rule")),
                Arguments.of("missing write storage unit", "readwrite_ds", "", List.of("read_ds_0"), "DYNAMIC", List.of("write_storage_unit")),
                Arguments.of("missing read storage units", "readwrite_ds", "write_ds", List.of(), "DYNAMIC", List.of("read_storage_units")),
                Arguments.of("missing strategy", "readwrite_ds", "write_ds", List.of("read_ds_0"), "", List.of("transactional_read_query_strategy")));
    }
    
    private static Stream<Arguments> unsupportedIdentifiers() {
        return Stream.of(
                Arguments.of("database", "bad`database", "readwrite_ds", "write_ds", List.of("read_ds_0")),
                Arguments.of("rule", "logic_db", "bad`rule", "write_ds", List.of("read_ds_0")),
                Arguments.of("write storage unit", "logic_db", "readwrite_ds", "bad`write", List.of("read_ds_0")),
                Arguments.of("read storage unit", "logic_db", "readwrite_ds", "write_ds", List.of("bad`read")));
    }
    
    private MCPFeatureQueryFacade mockRuleQueryFacade(final List<Map<String, Object>> rules) {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "readwrite_ds", "readwrite_ds")).thenReturn(true);
        when(result.query(eq("logic_db"), any())).thenReturn(rules);
        when(result.queryWithAnyDatabase("SHOW LOAD BALANCE ALGORITHM PLUGINS"))
                .thenReturn(List.of(Map.of("type", "RANDOM"), Map.of("type", "WEIGHT")));
        return result;
    }
    
    private ReadwriteSplittingRuleWorkflowRequest createRuleRequest(final String operationType) {
        ReadwriteSplittingRuleWorkflowRequest result = new ReadwriteSplittingRuleWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("readwrite_ds");
        result.setOperationType(operationType);
        result.setWriteStorageUnit("write_ds");
        result.setReadStorageUnits("read_ds_0");
        result.setTransactionalReadQueryStrategy("DYNAMIC");
        return result;
    }
    
}
