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
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingToolHandlerTest {
    
    @Test
    void assertHandlePlanTableRule() {
        ShardingWorkflowPlanningService planningService = mock(ShardingWorkflowPlanningService.class);
        when(planningService.planTableRule(any(), any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                createRequest(), "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'))"));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        MCPResponse actual = new PlanShardingTableRuleToolHandler(planningService).handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of(
                "database", "logic_db",
                "algorithm_type", "INLINE",
                "algorithm_properties", Map.of("algorithm-expression", "t_order_${order_id % 2}"),
                "structured_intent_evidence", Map.of("table", "t_order", "column", "order_id", "sharding_columns", "order_id, user_id"))));
        assertFalse(actual.toPayload().containsKey("ddl_artifacts"));
        ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
        verify(planningService).planTableRule(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getTable(), is("t_order"));
        assertThat(requestCaptor.getValue().getShardingColumns(), is("order_id, user_id"));
        assertThat(requestCaptor.getValue().getAlgorithmType(), is("INLINE"));
        assertThat(requestCaptor.getValue().getPrimaryAlgorithmProperties(), is(Map.of("algorithm-expression", "t_order_${order_id % 2}")));
    }
    
    @Test
    void assertHandlePlanTableReferenceRule() {
        ShardingWorkflowPlanningService planningService = mock(ShardingWorkflowPlanningService.class);
        when(planningService.planTableReferenceRule(any(), any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND,
                createRequest(), "CREATE SHARDING TABLE REFERENCE RULE ref_rule(t_order, t_order_item)"));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        new PlanShardingTableReferenceRuleToolHandler(planningService)
                .handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of("database", "logic_db", "rule", "ref_rule", "reference_tables", "t_order,t_order_item")));
        ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
        verify(planningService).planTableReferenceRule(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getReferenceTables(), is(List.of("t_order", "t_order_item")));
    }
    
    @Test
    void assertHandlePlanDefaultStrategy() {
        ShardingWorkflowPlanningService planningService = mock(ShardingWorkflowPlanningService.class);
        when(planningService.planDefaultStrategy(any(), any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                createRequest(), "CREATE DEFAULT SHARDING DATABASE STRATEGY (TYPE='none')"));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        MCPResponse actual = new PlanShardingDefaultStrategyToolHandler(planningService)
                .handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of("database", "logic_db", "default_strategy_type", "DATABASE", "strategy_type", "none")));
        ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
        verify(planningService).planDefaultStrategy(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getDefaultStrategyType(), is("DATABASE"));
        assertThat(actual.toPayload().get("workflow_kind"), is("sharding.default.strategy"));
    }
    
    @Test
    void assertHandlePlanKeyGenerator() {
        ShardingWorkflowPlanningService planningService = mock(ShardingWorkflowPlanningService.class);
        when(planningService.planKeyGenerator(any(), any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND,
                createRequest(), "CREATE SHARDING KEY GENERATOR snowflake_generator(TYPE(NAME='snowflake'))"));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        new PlanShardingKeyGeneratorToolHandler(planningService).handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of(
                "database", "logic_db", "key_generator", "snowflake_generator", "key_generator_type", "SNOWFLAKE", "key_generator_properties", Map.of("worker-id", "1"))));
        ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
        verify(planningService).planKeyGenerator(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getKeyGeneratorName(), is("snowflake_generator"));
        assertThat(requestCaptor.getValue().getKeyGeneratorProperties(), is(Map.of("worker-id", "1")));
    }
    
    @Test
    void assertHandlePlanKeyGenerateStrategy() {
        ShardingWorkflowPlanningService planningService = mock(ShardingWorkflowPlanningService.class);
        when(planningService.planKeyGenerateStrategy(any(), any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                createRequest(), "CREATE SHARDING KEY GENERATE STRATEGY order_key_strategy(TABLE=t_order, COLUMN=id, GENERATOR=snowflake_generator)"));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        new PlanShardingKeyGenerateStrategyToolHandler(planningService).handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of(
                "database", "logic_db", "key_generate_strategy", "order_key_strategy", "table", "t_order", "column", "id", "key_generator", "snowflake_generator")));
        ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
        verify(planningService).planKeyGenerateStrategy(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getKeyGenerateStrategyName(), is("order_key_strategy"));
        assertThat(requestCaptor.getValue().getKeyGeneratorName(), is("snowflake_generator"));
    }
    
    @Test
    void assertHandlePlanComponentCleanup() {
        ShardingWorkflowPlanningService planningService = mock(ShardingWorkflowPlanningService.class);
        when(planningService.planComponentCleanup(any(), any(), any(), any())).thenReturn(createSnapshot(ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND,
                createRequest(), "DROP SHARDING ALGORITHM unused_algorithm"));
        WorkflowContextFixture fixture = createWorkflowContextFixture();
        MCPResponse actual = new PlanShardingRuleComponentCleanupToolHandler(planningService).handle(fixture.workflowContext, new MCPToolCall("session-1", Map.of(
                "database", "logic_db", "component_type", "algorithm", "component_name", "unused_algorithm")));
        ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
        verify(planningService).planComponentCleanup(eq(fixture.workflowSessionContext), eq(fixture.queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getComponentName(), is("unused_algorithm"));
        assertThat(actual.toPayload().get("workflow_kind"), is("sharding.component.cleanup"));
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
    
    private WorkflowContextFixture createWorkflowContextFixture() {
        MCPWorkflowHandlerContext result = mock(MCPWorkflowHandlerContext.class);
        MCPDatabaseHandlerContext databaseContext = mock(MCPDatabaseHandlerContext.class);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(result.getDatabaseContext()).thenReturn(databaseContext);
        when(result.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        when(databaseContext.getQueryFacade()).thenReturn(queryFacade);
        when(databaseContext.getExecutionFacade()).thenReturn(executionFacade);
        return new WorkflowContextFixture(result, workflowSessionContext, queryFacade, executionFacade);
    }
    
    private record WorkflowContextFixture(MCPWorkflowHandlerContext workflowContext, WorkflowSessionContext workflowSessionContext,
                                          MCPFeatureQueryFacade queryFacade, MCPFeatureExecutionFacade executionFacade) {
    }
}
