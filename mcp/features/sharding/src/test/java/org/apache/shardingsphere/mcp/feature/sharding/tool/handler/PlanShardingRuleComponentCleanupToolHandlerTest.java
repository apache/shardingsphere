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
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanShardingRuleComponentCleanupToolHandlerTest {
    
    @Test
    void assertHandle() {
        try (
                MockedConstruction<ShardingWorkflowPlanningService> mocked = mockConstruction(ShardingWorkflowPlanningService.class,
                        (mock, context) -> when(mock.planComponentCleanup(any(), any(), any())).thenReturn(ShardingWorkflowToolHandlerTestFixture.createSnapshot(
                                ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND, "DROP SHARDING ALGORITHM `unused_algorithm`")))) {
            WorkflowContextFixture fixture = ShardingWorkflowToolHandlerTestFixture.createWorkflowContextFixture();
            MCPSuccessPayload actual = new PlanShardingRuleComponentCleanupToolHandler().handle(fixture.requestContext(), Map.of(
                    "database", "logic_db", "component_type", "algorithm", "component_name", "unused_algorithm"));
            ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
            verify(mocked.constructed().getFirst()).planComponentCleanup(
                    eq(fixture.workflowSessionContext()), eq(fixture.queryFacade()), requestCaptor.capture());
            assertThat(requestCaptor.getValue().getComponentName(), is("unused_algorithm"));
            assertThat(actual.toPayload().get("workflow_kind"), is("sharding.component.cleanup"));
            List<?> actualResourcesToRead = (List<?>) actual.toPayload().get("resources_to_read");
            assertThat(ShardingWorkflowToolHandlerTestFixture.extractResourceUris(actualResourcesToRead), is(List.of(
                    "shardingsphere://features/sharding/databases/logic_db/algorithms",
                    "shardingsphere://features/sharding/databases/logic_db/key-generators",
                    "shardingsphere://features/sharding/databases/logic_db/auditors",
                    "shardingsphere://features/sharding/databases/logic_db/unused-algorithms",
                    "shardingsphere://features/sharding/databases/logic_db/unused-key-generators",
                    "shardingsphere://features/sharding/databases/logic_db/unused-auditors")));
            for (Entry<String, String> entry : Map.of(
                    "shardingsphere://features/sharding/databases/logic_db/algorithms", "algorithm",
                    "shardingsphere://features/sharding/databases/logic_db/key-generators", "rule",
                    "shardingsphere://features/sharding/databases/logic_db/auditors", "rule",
                    "shardingsphere://features/sharding/databases/logic_db/unused-algorithms", "algorithm",
                    "shardingsphere://features/sharding/databases/logic_db/unused-key-generators", "rule",
                    "shardingsphere://features/sharding/databases/logic_db/unused-auditors", "rule").entrySet()) {
                assertThat(ShardingWorkflowToolHandlerTestFixture.findResourceKind(actualResourcesToRead, entry.getKey()), is(entry.getValue()));
            }
        }
    }
}
