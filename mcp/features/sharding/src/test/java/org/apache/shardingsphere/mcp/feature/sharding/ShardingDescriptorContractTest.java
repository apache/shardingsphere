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

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingDescriptorContractTest {
    
    @Test
    void assertPlanningToolAnnotationTitles() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        for (Entry<String, String> entry : Map.of(
                ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME, "Plan Sharding Table Rule",
                ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_TOOL_NAME, "Plan Sharding Table Reference Rule",
                ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_TOOL_NAME, "Plan Sharding Default Strategy",
                ShardingFeatureDefinition.PLAN_KEY_GENERATOR_TOOL_NAME, "Plan Sharding Key Generator",
                ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME, "Plan Sharding Key Generate Strategy",
                ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_TOOL_NAME, "Plan Sharding Rule Component Cleanup").entrySet()) {
            assertThat(findTool(catalog, entry.getKey()).getAnnotations().getTitle(), is(entry.getValue()));
        }
    }
    
    @Test
    void assertPromptCompletionArguments() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        assertCompletionTargetArguments(catalog, ShardingFeatureDefinition.PLAN_TABLE_RULE_PROMPT_NAME, "database", "algorithm_type", "plan_id");
        assertCompletionTargetArguments(catalog, ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_PROMPT_NAME, "database", "plan_id");
        assertCompletionTargetArguments(catalog, ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_PROMPT_NAME, "database", "algorithm_type", "plan_id");
        assertCompletionTargetArguments(catalog, ShardingFeatureDefinition.PLAN_KEY_GENERATOR_PROMPT_NAME, "database", "key_generator_type", "plan_id");
        assertCompletionTargetArguments(catalog, ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_PROMPT_NAME, "database", "key_generator_type", "plan_id");
        assertCompletionTargetArguments(catalog, ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_PROMPT_NAME, "database", "plan_id");
    }
    
    private MCPToolDescriptor findTool(final MCPDescriptorCatalog catalog, final String toolName) {
        return catalog.getProtocolDescriptors().getToolDescriptors().stream().filter(each -> toolName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private void assertCompletionTargetArguments(final MCPDescriptorCatalog catalog, final String promptName, final String... expectedArguments) {
        MCPCompletionTargetDescriptor actual = catalog.getShardingSphereDescriptors().getCompletionTargetDescriptors().stream()
                .filter(each -> "prompt".equals(each.getReferenceType()) && promptName.equals(each.getReference())).findFirst().orElseThrow();
        assertTrue(actual.getArguments().containsAll(List.of(expectedArguments)));
    }
}
