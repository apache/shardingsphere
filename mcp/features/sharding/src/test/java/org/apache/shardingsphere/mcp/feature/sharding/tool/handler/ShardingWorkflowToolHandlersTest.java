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

package org.apache.shardingsphere.mcp.feature.sharding.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingWorkflowToolHandlersTest {
    
    @Test
    void assertHandlePlanTableRule() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planTableRule(any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                                createRequest(), "CREATE SHARDING TABLE RULE `t_order`(DATANODES('ds_${0..1}.t_order_${0..1}'))")))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPResponse actual = new PlanShardingTableRuleToolHandler().handle(fixture.workflowContext, Map.of(
                    "database", "logic_db",
                    "algorithm_type", "INLINE",
                    "algorithm_properties", Map.of("algorithm-expression", "t_order_${order_id % 2}"),
                    "structured_intent_evidence", Map.of("table", "t_order", "column", "order_id", "sharding_columns", "order_id, user_id")));
            Map<String, Object> actualPayload = actual.toPayload();
            assertFalse(actualPayload.containsKey("ddl_artifacts"));
            List<?> actualResourcesToRead = (List<?>) actualPayload.get("resources_to_read");
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/sharding/algorithm-plugins"), is("algorithm"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/sharding/key-generate-algorithm-plugins"), is("algorithm"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/sharding/databases/logic_db/table-rules"), is("rule"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/sharding/databases/logic_db/table-nodes"), is("rule"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/sharding/databases/logic_db/tables/t_order/table-rule"), is("rule"));
            assertThat(findResourceKind(actualResourcesToRead, "shardingsphere://features/sharding/databases/logic_db/tables/t_order/nodes"), is("rule"));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planTableRule(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getTable(), is("t_order"));
            assertThat(requestCaptor.getValue().getShardingColumns(), is("order_id, user_id"));
            assertThat(requestCaptor.getValue().getAlgorithmType(), is("INLINE"));
            assertThat(requestCaptor.getValue().getPrimaryAlgorithmProperties(), is(Map.of("algorithm-expression", "t_order_${order_id % 2}")));
        }
    }
    
    @Test
    void assertHandlePlanTableReferenceRule() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planTableReferenceRule(any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND,
                                createRequest(), "CREATE SHARDING TABLE REFERENCE RULE `ref_rule`(`t_order`, `t_order_item`)")))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            new PlanShardingTableReferenceRuleToolHandler()
                    .handle(fixture.workflowContext, Map.of("database", "logic_db", "rule", "ref_rule", "reference_tables", "t_order,t_order_item"));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planTableReferenceRule(
                    eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getReferenceTables(), is(List.of("t_order", "t_order_item")));
        }
    }
    
    @Test
    void assertHandlePlanDefaultStrategy() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planDefaultStrategy(any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                                createRequest(), "CREATE DEFAULT SHARDING DATABASE STRATEGY (TYPE='none')")))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPResponse actual = new PlanShardingDefaultStrategyToolHandler()
                    .handle(fixture.workflowContext, Map.of("database", "logic_db", "default_strategy_type", "DATABASE", "strategy_type", "none"));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planDefaultStrategy(
                    eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getDefaultStrategyType(), is("DATABASE"));
            assertThat(actual.toPayload().get("workflow_kind"), is("sharding.default.strategy"));
        }
    }
    
    @Test
    void assertHandlePlanKeyGenerator() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planKeyGenerator(any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND,
                                createRequest(), "CREATE SHARDING KEY GENERATOR `snowflake_generator`(TYPE(NAME='snowflake'))")))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            new PlanShardingKeyGeneratorToolHandler().handle(fixture.workflowContext, Map.of(
                    "database", "logic_db", "key_generator", "snowflake_generator", "key_generator_type", "SNOWFLAKE", "key_generator_properties", Map.of("worker-id", "1")));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planKeyGenerator(
                    eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getKeyGeneratorName(), is("snowflake_generator"));
            assertThat(requestCaptor.getValue().getKeyGeneratorProperties(), is(Map.of("worker-id", "1")));
        }
    }
    
    @Test
    void assertHandlePlanKeyGenerateStrategy() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planKeyGenerateStrategy(any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                                createRequest(), "CREATE SHARDING KEY GENERATE STRATEGY `order_key_strategy`(TABLE=`t_order`, COLUMN=`id`, GENERATOR=`snowflake_generator`)")))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            new PlanShardingKeyGenerateStrategyToolHandler().handle(fixture.workflowContext, Map.of(
                    "database", "logic_db", "key_generate_strategy", "order_key_strategy", "table", "t_order", "column", "id", "key_generator", "snowflake_generator"));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planKeyGenerateStrategy(
                    eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getKeyGenerateStrategyName(), is("order_key_strategy"));
            assertThat(requestCaptor.getValue().getKeyGeneratorName(), is("snowflake_generator"));
        }
    }
    
    @Test
    void assertHandlePlanComponentCleanup() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planComponentCleanup(any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND,
                                createRequest(), "DROP SHARDING ALGORITHM `unused_algorithm`")))) {
            WorkflowContextFixture fixture = createWorkflowContextFixture();
            MCPResponse actual = new PlanShardingRuleComponentCleanupToolHandler().handle(fixture.workflowContext, Map.of(
                    "database", "logic_db", "component_type", "algorithm", "component_name", "unused_algorithm"));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planComponentCleanup(
                    eq(fixture.workflowSessionContext), eq(fixture.queryFacade), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getComponentName(), is("unused_algorithm"));
            assertThat(actual.toPayload().get("workflow_kind"), is("sharding.component.cleanup"));
            List<?> actualResourcesToRead = (List<?>) actual.toPayload().get("resources_to_read");
            List<String> actualResourceUris = extractResourceUris(actualResourcesToRead);
            assertThat(actualResourceUris, is(List.of(
                    "shardingsphere://features/sharding/databases/logic_db/algorithms",
                    "shardingsphere://features/sharding/databases/logic_db/key-generators",
                    "shardingsphere://features/sharding/databases/logic_db/auditors",
                    "shardingsphere://features/sharding/databases/logic_db/unused-algorithms",
                    "shardingsphere://features/sharding/databases/logic_db/unused-key-generators",
                    "shardingsphere://features/sharding/databases/logic_db/unused-auditors")));
            for (String each : actualResourceUris) {
                assertThat(findResourceKind(actualResourcesToRead, each), is("rule"));
            }
        }
    }
    
    private WorkflowContextSnapshot createSnapshot(final WorkflowKind workflowKind, final ShardingWorkflowRequest request, final String sql) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(workflowKind);
        result.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        result.setClarifiedIntent(clarifiedIntent);
        result.setInteractionPlan(InteractionPlan.create("plan-1", request, "Sharding workflow plan.", List.of("review"), List.of("rules")));
        result.getRuleArtifacts().add(new RuleArtifact("create", sql));
        return result;
    }
    
    private ShardingWorkflowRequest createRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("t_order");
        return result;
    }
    
    private List<String> extractResourceUris(final List<?> resourcesToRead) {
        return resourcesToRead.stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
    }
    
    private String findResourceKind(final List<?> resourcesToRead, final String uri) {
        for (Object each : resourcesToRead) {
            Map<?, ?> resource = (Map<?, ?>) each;
            if (uri.equals(resource.get("uri"))) {
                return (String) resource.get("resource_kind");
            }
        }
        return "";
    }
    
    private WorkflowContextFixture createWorkflowContextFixture() {
        MCPWorkflowRequestContext result = mock(MCPWorkflowRequestContext.class);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(result.getSessionId()).thenReturn("session-1");
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        return new WorkflowContextFixture(result, workflowSessionContext, queryFacade);
    }
    
    private record WorkflowContextFixture(MCPWorkflowRequestContext workflowContext, WorkflowSessionContext workflowSessionContext,
                                          MCPFeatureQueryFacade queryFacade) {
    }
}
