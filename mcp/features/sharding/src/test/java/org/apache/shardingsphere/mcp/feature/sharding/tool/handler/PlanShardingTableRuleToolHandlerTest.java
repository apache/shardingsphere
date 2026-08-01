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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.handler.ShardingWorkflowToolHandlerTestFixture.WorkflowContextFixture;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingWorkflowPlanningService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanShardingTableRuleToolHandlerTest {
    
    @Test
    void assertHandle() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planTableRule(any(), any(), any())).thenReturn(ShardingWorkflowToolHandlerTestFixture.createSnapshot(
                                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                                "CREATE SHARDING TABLE RULE `t_order`(DATANODES('ds_${0..1}.t_order_${0..1}'))")))) {
            WorkflowContextFixture fixture = ShardingWorkflowToolHandlerTestFixture.createWorkflowContextFixture();
            MCPSuccessPayload actual = new PlanShardingTableRuleToolHandler().handle(fixture.requestContext(), Map.of(
                    "database", "logic_db",
                    "algorithm_type", "INLINE",
                    "algorithm_properties", Map.of("algorithm-expression", "t_order_${order_id % 2}"),
                    "structured_intent_evidence", Map.of("table", "t_order", "column", "order_id", "sharding_columns", "order_id, user_id")));
            List<?> actualResourcesToRead = (List<?>) actual.toPayload().get("resources_to_read");
            assertThat(ShardingWorkflowToolHandlerTestFixture.findResourceKind(
                    actualResourcesToRead, "shardingsphere://features/sharding/algorithm-plugins"), is("algorithm"));
            assertThat(ShardingWorkflowToolHandlerTestFixture.findResourceKind(
                    actualResourcesToRead, "shardingsphere://features/sharding/key-generate-algorithm-plugins"), is("algorithm"));
            assertThat(ShardingWorkflowToolHandlerTestFixture.findResourceKind(
                    actualResourcesToRead, "shardingsphere://features/sharding/databases/logic_db/table-rules"), is("rule"));
            assertThat(ShardingWorkflowToolHandlerTestFixture.findResourceKind(
                    actualResourcesToRead, "shardingsphere://features/sharding/databases/logic_db/table-nodes"), is("rule"));
            assertThat(ShardingWorkflowToolHandlerTestFixture.findResourceKind(
                    actualResourcesToRead, "shardingsphere://features/sharding/databases/logic_db/tables/t_order/table-rule"), is("rule"));
            assertThat(ShardingWorkflowToolHandlerTestFixture.findResourceKind(
                    actualResourcesToRead, "shardingsphere://features/sharding/databases/logic_db/tables/t_order/nodes"), is("rule"));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planTableRule(eq(fixture.workflowSessionContext()), eq(fixture.queryFacade()), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getTable(), is("t_order"));
            assertThat(requestCaptor.getValue().getShardingColumns(), is("order_id, user_id"));
            assertThat(requestCaptor.getValue().getAlgorithmType(), is("INLINE"));
            assertThat(requestCaptor.getValue().getPrimaryAlgorithmProperties(), is(Map.of("algorithm-expression", "t_order_${order_id % 2}")));
        }
    }
}
