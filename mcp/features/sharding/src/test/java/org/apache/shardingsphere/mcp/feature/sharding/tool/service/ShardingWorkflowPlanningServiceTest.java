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

import org.apache.shardingsphere.mcp.feature.sharding.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingWorkflowPlanningServiceTest {
    
    @Test
    void assertPlanTableRuleCreate() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        when(inspectionService.queryTableRule(queryFacade, "logic_db", "t_order")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planTableRule(new TestWorkflowSessionContext(), queryFacade, "session-1", createTableRuleRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.table.rule"));
        assertTrue(actual.getRuleArtifacts().get(0).getSql().startsWith("CREATE SHARDING TABLE RULE t_order"));
    }
    
    @Test
    void assertPlanTableRuleCreateWithComplexStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setStrategyType("complex");
        request.setShardingColumns("order_id, user_id");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        when(inspectionService.queryTableRule(queryFacade, "logic_db", "t_order")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planTableRule(new TestWorkflowSessionContext(), queryFacade, "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().get(0).getSql(), is("CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'), "
                + "TABLE_STRATEGY(TYPE='complex', SHARDING_COLUMNS=order_id, user_id, "
                + "SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}')))))"));
    }
    
    @Test
    void assertPlanTableRuleCreateWithHintStrategy() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setStrategyType("hint");
        when(inspectionService.queryTableRule(queryFacade, "logic_db", "t_order")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planTableRule(new TestWorkflowSessionContext(), queryFacade, "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().get(0).getSql(), is("CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'), "
                + "TABLE_STRATEGY(TYPE='hint', SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}')))))"));
    }
    
    @Test
    void assertPlanTableRuleCreateWithNoneStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setStrategyType("none");
        request.setAlgorithmType("");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        when(inspectionService.queryTableRule(queryFacade, "logic_db", "t_order")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planTableRule(new TestWorkflowSessionContext(), queryFacade, "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().get(0).getSql(), is("CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'))"));
    }
    
    @Test
    void assertPlanTableRuleMissingInputs() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setAlgorithmType("");
        WorkflowContextSnapshot actual = new ShardingWorkflowPlanningService()
                .planTableRule(new TestWorkflowSessionContext(), createQueryFacade(), "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanTableRuleKeyGenerateMissingGenerator() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateColumn("id");
        WorkflowContextSnapshot actual = new ShardingWorkflowPlanningService()
                .planTableRule(new TestWorkflowSessionContext(), createQueryFacade(), "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanTableRuleComplexStrategyMissingColumns() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setStrategyType("complex");
        WorkflowContextSnapshot actual = new ShardingWorkflowPlanningService()
                .planTableRule(new TestWorkflowSessionContext(), createQueryFacade(), "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanTableRuleUnsupportedIdentifier() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setTable("t_order\nbad");
        WorkflowContextSnapshot actual = new ShardingWorkflowPlanningService()
                .planTableRule(new TestWorkflowSessionContext(), createQueryFacade(), "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_FAILED));
    }
    
    @Test
    void assertPlanTableReferenceRule() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        when(inspectionService.queryTableReferenceRule(queryFacade, "logic_db", "ref_rule")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planTableReferenceRule(new TestWorkflowSessionContext(), queryFacade, "session-1", createReferenceRuleRequest());
        assertThat(actual.getRuleArtifacts().get(0).getSql(), is("CREATE SHARDING TABLE REFERENCE RULE ref_rule(t_order, t_order_item)"));
    }
    
    @Test
    void assertPlanDefaultStrategyDrop() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setOperationType("drop");
        request.setDefaultStrategyType("DATABASE");
        when(inspectionService.queryDefaultStrategy(queryFacade, "logic_db")).thenReturn(List.of(Map.of("name", "DATABASE", "type", "standard")));
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planDefaultStrategy(new TestWorkflowSessionContext(), queryFacade, "session-1", request);
        assertThat(actual.getRuleArtifacts().get(0).getSql(), is("DROP DEFAULT SHARDING DATABASE STRATEGY"));
    }
    
    @Test
    void assertPlanDefaultStrategyInvalidType() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("FOO");
        when(inspectionService.queryDefaultStrategy(queryFacade, "logic_db")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planDefaultStrategy(new TestWorkflowSessionContext(), queryFacade, "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanKeyGenerator() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        when(inspectionService.queryKeyGenerator(queryFacade, "logic_db", "snowflake_generator")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planKeyGenerator(new TestWorkflowSessionContext(), queryFacade, "session-1", createKeyGeneratorRequest());
        assertTrue(actual.getRuleArtifacts().get(0).getSql().startsWith("CREATE SHARDING KEY GENERATOR snowflake_generator"));
    }
    
    @Test
    void assertPlanKeyGenerateStrategy() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        when(inspectionService.queryKeyGenerateStrategy(queryFacade, "logic_db", "order_key_strategy")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planKeyGenerateStrategy(new TestWorkflowSessionContext(), queryFacade, "session-1", createKeyGenerateStrategyRequest());
        assertTrue(actual.getRuleArtifacts().get(0).getSql().startsWith("CREATE SHARDING KEY GENERATE STRATEGY order_key_strategy"));
    }
    
    @Test
    void assertPlanComponentCleanup() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        ShardingWorkflowRequest request = createCleanupRequest();
        when(inspectionService.queryUnusedAlgorithms(queryFacade, "logic_db")).thenReturn(List.of(Map.of("name", "unused_algorithm")));
        when(inspectionService.queryTableRulesUsedAlgorithm(queryFacade, "logic_db", "unused_algorithm")).thenReturn(List.of());
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planComponentCleanup(new TestWorkflowSessionContext(), queryFacade, "session-1", request);
        assertThat(actual.getRuleArtifacts().get(0).getSql(), is("DROP SHARDING ALGORITHM unused_algorithm"));
    }
    
    @Test
    void assertPlanComponentCleanupReferenced() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        ShardingWorkflowRequest request = createCleanupRequest();
        when(inspectionService.queryUnusedAlgorithms(queryFacade, "logic_db")).thenReturn(List.of(Map.of("name", "unused_algorithm")));
        when(inspectionService.queryTableRulesUsedAlgorithm(queryFacade, "logic_db", "unused_algorithm")).thenReturn(List.of(Map.of("table", "t_order")));
        WorkflowContextSnapshot actual = createPlanningService(inspectionService)
                .planComponentCleanup(new TestWorkflowSessionContext(), queryFacade, "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    private ShardingWorkflowPlanningService createPlanningService(final ShardingInspectionService inspectionService) {
        return new ShardingWorkflowPlanningService(inspectionService, new ShardingDistSQLPlanningService());
    }
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.getDatabaseType(any())).thenReturn("MySQL");
        return result;
    }
    
    private ShardingWorkflowRequest createTableRuleRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("t_order");
        result.setColumn("order_id");
        result.setDataNodes("ds_${0..1}.t_order_${0..1}");
        result.setStrategyType("standard");
        result.setAlgorithmType("INLINE");
        result.putAlgorithmProperties(Map.of("algorithm-expression", "t_order_${order_id % 2}"));
        return result;
    }
    
    private ShardingWorkflowRequest createReferenceRuleRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("ref_rule");
        result.getReferenceTables().addAll(List.of("t_order", "t_order_item"));
        return result;
    }
    
    private ShardingWorkflowRequest createKeyGeneratorRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setKeyGeneratorName("snowflake_generator");
        result.setKeyGeneratorType("SNOWFLAKE");
        return result;
    }
    
    private ShardingWorkflowRequest createKeyGenerateStrategyRequest() {
        ShardingWorkflowRequest result = createTableRuleRequest();
        result.setKeyGenerateStrategyName("order_key_strategy");
        result.setKeyGeneratorType("SNOWFLAKE");
        return result;
    }
    
    private ShardingWorkflowRequest createCleanupRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setComponentType("algorithm");
        result.setComponentName("unused_algorithm");
        return result;
    }
}
