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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingWorkflowPlanningServiceTest {
    
    private final ShardingWorkflowPlanningService planningService = new ShardingWorkflowPlanningService();
    
    @Test
    void assertPlanTableRuleClarifiesMissingDatabase() {
        WorkflowContextSnapshot actual = planningService.planTableRule(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), new ShardingWorkflowRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_CLARIFYING));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide logical database first.")));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("database")));
        assertThat(actual.getRequest().getOperationType(), is("create"));
        assertThat(actual.getResourceUriTemplates(), is(List.of(ShardingFeatureDefinition.STORAGE_UNITS_RESOURCE_URI,
                ShardingFeatureDefinition.SINGLE_TABLES_RESOURCE_URI, ShardingFeatureDefinition.SINGLE_TABLE_RESOURCE_URI)));
    }
    
    @Test
    void assertPlanTableRuleRejectsUnsupportedOperation() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setOperationType("replace");
        WorkflowContextSnapshot actual = planningService.planTableRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_FAILED));
        assertThat(actual.getClarifiedIntent().getOperationType(), is(""));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        assertRuleDistSQLOnlyPayloadDoesNotExpose(actual, "replace");
    }
    
    @Test
    void assertPlanTableReferenceRuleClarifiesMissingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        WorkflowContextSnapshot actual = planningService.planTableReferenceRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.table.reference"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide table reference rule name.")));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("rule")));
    }
    
    @Test
    void assertPlanTableReferenceRuleClarifiesMissingReferenceTables() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        request.setRuleName("order_reference");
        WorkflowContextSnapshot actual = planningService.planTableReferenceRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("reference_tables")));
    }
    
    @Test
    void assertPlanDefaultStrategyClarifiesMissingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        WorkflowContextSnapshot actual = planningService.planDefaultStrategy(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.default.strategy"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide DATABASE or TABLE default strategy type.")));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("default_strategy_type")));
    }
    
    @Test
    void assertPlanKeyGeneratorClarifiesMissingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        WorkflowContextSnapshot actual = planningService.planKeyGenerator(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.key.generator"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide key generator name.")));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("key_generator")));
    }
    
    @Test
    void assertPlanKeyGenerateStrategyClarifiesMissingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        WorkflowContextSnapshot actual = planningService.planKeyGenerateStrategy(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.key.generate.strategy"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide key generate strategy name.")));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("key_generate_strategy")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTableRuleConflictCases")
    void assertPlanTableRuleRejectsConflictingInputs(final String name, final Consumer<ShardingWorkflowRequest> requestCustomizer,
                                                     final List<String> expectedConflictingInputs) {
        ShardingWorkflowRequest request = createTableRuleRequest();
        requestCustomizer.accept(request);
        WorkflowContextSnapshot actual = planningService.planTableRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_CONFLICT));
        assertThat(actual.getIssues().getFirst().getDetails().get("conflicting_inputs"), is(expectedConflictingInputs));
    }
    
    @Test
    void assertPlanDefaultStrategyRejectsConflictingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        request.setDefaultStrategyType("DATABASE");
        request.setStrategyType("hint");
        request.setColumn("order_id");
        request.setAlgorithmType("HINT_INLINE");
        WorkflowContextSnapshot actual = planningService.planDefaultStrategy(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_CONFLICT));
        assertThat(actual.getIssues().getFirst().getDetails().get("conflicting_inputs"), is(List.of("strategy_type", "column")));
    }
    
    @Test
    void assertPlanKeyGenerateStrategyRejectsConflictingTargetModes() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        request.setKeyGenerateStrategyName("order_key_strategy");
        request.setSequenceName("order_seq");
        request.setTable("t_order");
        request.setColumn("order_id");
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot actual = planningService.planKeyGenerateStrategy(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_CONFLICT));
        assertThat(actual.getIssues().getFirst().getDetails().get("conflicting_inputs"), is(List.of("sequence", "table", "column")));
    }
    
    @Test
    void assertPlanTableRuleRequiresKeyGenerateColumnForGenerator() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot actual = planningService.planTableRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("key_generate_column")));
    }
    
    @Test
    void assertPlanTableRuleRequiresAuditorsForAllowHintDisable() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setAllowHintDisable("true");
        WorkflowContextSnapshot actual = planningService.planTableRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("auditors")));
    }
    
    @Test
    void assertPlanTableRuleRejectsInvalidAllowHintDisable() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.getAuditorNames().add("dml_auditor");
        request.setAllowHintDisable("sometimes");
        WorkflowContextSnapshot actual = planningService.planTableRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("allow_hint_disable")));
    }
    
    @Test
    void assertPlanTableRuleDropIgnoresCreateInputConflicts() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setOperationType("drop");
        request.setStorageUnits("ds_0");
        request.setAlgorithmType("INLINE");
        WorkflowContextSnapshot actual = planningService.planTableRule(new TestWorkflowSessionContext(),
                createQueryFacade(List.of(Map.of("table", "t_order"))), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("DROP SHARDING TABLE RULE `t_order`"));
    }
    
    @Test
    void assertPlanSequenceKeyGenerateStrategy() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        request.setKeyGenerateStrategyName("order_sequence_strategy");
        request.setSequenceName("order_seq");
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot actual = planningService.planKeyGenerateStrategy(new TestWorkflowSessionContext(), createQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getWorkflowKind(), is(ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND));
    }
    
    @Test
    void assertPlanSequenceKeyGenerateStrategyRejectsUnsupportedIdentifier() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        request.setKeyGenerateStrategyName("order_sequence_strategy");
        request.setSequenceName("order\nsequence");
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot actual = planningService.planKeyGenerateStrategy(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertPlanComponentCleanupClarifiesMissingComponentType() {
        WorkflowContextSnapshot actual = planningService.planComponentCleanup(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), createDatabaseRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("component_type")));
    }
    
    @Test
    void assertPlanComponentCleanupRejectsNonDropOperation() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setDatabase("logic_db");
        request.setComponentType("algorithm");
        request.setComponentName("inline_algorithm");
        request.setOperationType("create");
        WorkflowContextSnapshot actual = planningService.planComponentCleanup(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        assertThat(actual.getIssues().getFirst().getDetails(), is(Map.of("operation_type", "create")));
    }
    
    @Test
    void assertPlanComponentCleanupRejectsUnsupportedIdentifier() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        request.setComponentType("algorithm");
        request.setComponentName("inline\nalgorithm");
        WorkflowContextSnapshot actual = planningService.planComponentCleanup(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertPlanComponentCleanupClarifiesUnsupportedComponentType() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        request.setComponentType("storage-unit");
        request.setComponentName("ds_0");
        WorkflowContextSnapshot actual = planningService.planComponentCleanup(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
        assertThat(WorkflowPlanPayloadBuilder.build(actual).get("missing_required_inputs"), is(List.of("component_type")));
    }
    
    @Test
    void assertPlanComponentCleanupIgnoresUnrelatedIdentifiers() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        request.setTable("t\norder");
        request.setComponentType("algorithm");
        request.setComponentName("inline_algorithm");
        WorkflowContextSnapshot actual = planningService.planComponentCleanup(new TestWorkflowSessionContext(), createQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    private ShardingWorkflowRequest createDatabaseRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        return result;
    }
    
    private ShardingWorkflowRequest createTableRuleRequest() {
        ShardingWorkflowRequest result = createDatabaseRequest();
        result.setTable("t_order");
        result.setDataNodes("ds_0.t_order");
        result.setStrategyType("none");
        return result;
    }
    
    private MCPFeatureQueryFacade createQueryFacade(final List<Map<String, Object>> rows) {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.query(anyString(), anyString())).thenReturn(rows);
        return result;
    }
    
    private static Stream<Arguments> getTableRuleConflictCases() {
        return Stream.of(
                Arguments.of("data nodes and storage units", (Consumer<ShardingWorkflowRequest>) request -> request.setStorageUnits("ds_0"),
                        List.of("data_nodes", "storage_units")),
                Arguments.of("complex strategy with column", (Consumer<ShardingWorkflowRequest>) request -> {
                    request.setStrategyType("complex");
                    request.setColumn("order_id");
                    request.setShardingColumns("order_id,user_id");
                    request.setAlgorithmType("COMPLEX_INLINE");
                }, List.of("strategy_type", "column")),
                Arguments.of("none strategy with algorithm", (Consumer<ShardingWorkflowRequest>) request -> request.setAlgorithmType("INLINE"),
                        List.of("strategy_type", "algorithm_type")),
                Arguments.of("named and inline key generator", (Consumer<ShardingWorkflowRequest>) request -> {
                    request.setKeyGenerateColumn("order_id");
                    request.setKeyGeneratorName("snowflake_generator");
                    request.setKeyGeneratorType("SNOWFLAKE");
                }, List.of("key_generator", "key_generator_type")));
    }
    
    private void assertRuleDistSQLOnlyPayloadDoesNotExpose(final WorkflowContextSnapshot snapshot, final String term) {
        Map<String, Object> actualPayload = WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest());
        assertFalse(String.valueOf(actualPayload).toLowerCase(Locale.ENGLISH).contains(term));
    }
}
