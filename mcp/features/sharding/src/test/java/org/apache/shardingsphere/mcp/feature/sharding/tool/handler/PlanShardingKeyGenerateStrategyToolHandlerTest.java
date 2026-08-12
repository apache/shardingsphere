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

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.handler.ShardingWorkflowToolHandlerTestFixture.WorkflowContextFixture;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingWorkflowPlanningService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanShardingKeyGenerateStrategyToolHandlerTest {
    
    @Test
    void assertHandle() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planKeyGenerateStrategy(any(), any(), any())).thenReturn(ShardingWorkflowToolHandlerTestFixture.createSnapshot(
                                ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                                "CREATE SHARDING KEY GENERATE STRATEGY `order_key_strategy`(TABLE=`t_order`, COLUMN=`id`, GENERATOR=`snowflake_generator`)")))) {
            WorkflowContextFixture fixture = ShardingWorkflowToolHandlerTestFixture.createWorkflowContextFixture();
            new PlanShardingKeyGenerateStrategyToolHandler().handle(fixture.requestContext(), Map.of(
                    "database", "logic_db", "key_generate_strategy", "order_key_strategy", "table", "t_order", "column", "id",
                    "key_generator", "snowflake_generator"));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planKeyGenerateStrategy(
                    eq(fixture.workflowSessionContext()), eq(fixture.queryFacade()), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getKeyGenerateStrategyName(), is("order_key_strategy"));
            assertThat(requestCaptor.getValue().getKeyGeneratorName(), is("snowflake_generator"));
        }
    }
}
