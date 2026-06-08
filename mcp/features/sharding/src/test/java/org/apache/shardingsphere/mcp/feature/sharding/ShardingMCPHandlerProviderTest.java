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

package org.apache.shardingsphere.mcp.feature.sharding;

import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingMCPHandlerProviderTest {
    
    @Test
    void assertGetResourceHandlers() {
        Collection<MCPResourceHandler<?>> actual = new ShardingMCPHandlerProvider().getResourceHandlers();
        assertThat(actual.size(), is(22));
        assertTrue(actual.stream().map(MCPResourceHandler::getResourceUriTemplate).toList().contains(ShardingFeatureDefinition.TABLE_RULES_RESOURCE_URI));
        assertTrue(actual.stream().map(MCPResourceHandler::getResourceUriTemplate).toList().contains(ShardingFeatureDefinition.UNUSED_AUDITORS_RESOURCE_URI));
    }
    
    @Test
    void assertGetToolHandlers() {
        assertThat(new ShardingMCPHandlerProvider().getToolHandlers().stream().map(MCPToolHandler::getToolName).toList(),
                containsInAnyOrder(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME,
                        ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_TOOL_NAME, ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_TOOL_NAME,
                        ShardingFeatureDefinition.PLAN_KEY_GENERATOR_TOOL_NAME, ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME,
                        ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_TOOL_NAME));
    }
    
    @Test
    void assertGetWorkflowDefinitions() {
        Collection<WorkflowRuntimeDefinition> actual = new ShardingMCPHandlerProvider().getWorkflowDefinitions();
        assertThat(actual.stream().map(each -> each.getWorkflowKind().getValue()).toList(),
                containsInAnyOrder("sharding.table.rule", "sharding.table.reference", "sharding.default.strategy",
                        "sharding.key.generator", "sharding.key.generate.strategy", "sharding.component.cleanup"));
    }
    
    @Test
    void assertSPIRegistration() {
        assertTrue(ServiceLoader.load(MCPHandlerProvider.class).stream().anyMatch(each -> ShardingMCPHandlerProvider.class.equals(each.type())));
    }
}
