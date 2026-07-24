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

package org.apache.shardingsphere.mcp.feature.shadow;

import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.apache.shardingsphere.mcp.support.descriptor.ShardingSphereMCPResourceMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShadowDescriptorContractTest {
    
    @Test
    void assertPlanningToolAnnotationTitles() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        for (Entry<String, String> entry : Map.of(
                ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME, "Plan Shadow Rule",
                ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_TOOL_NAME, "Plan Default Shadow Algorithm",
                ShadowFeatureDefinition.PLAN_ALGORITHM_CLEANUP_TOOL_NAME, "Plan Shadow Algorithm Cleanup").entrySet()) {
            assertThat(findTool(catalog, entry.getKey()).getAnnotations().getTitle(), is(entry.getValue()));
        }
    }
    
    @Test
    void assertPromptCompletionArguments() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        assertCompletionTargetArguments(catalog, ShadowFeatureDefinition.PLAN_RULE_PROMPT_NAME, "database", "source_storage_unit", "shadow_storage_unit", "table", "algorithm_type", "plan_id");
        assertCompletionTargetArguments(catalog, ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_PROMPT_NAME, "database", "algorithm_type", "plan_id");
        assertCompletionTargetArguments(catalog, ShadowFeatureDefinition.PLAN_ALGORITHM_CLEANUP_PROMPT_NAME, "database", "plan_id");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertDefaultAlgorithmTypeContract() {
        Map<String, Object> properties = (Map<String, Object>) findTool(MCPDescriptorCatalogLoader.load(),
                ShadowFeatureDefinition.PLAN_DEFAULT_ALGORITHM_TOOL_NAME).getInputSchema().get("properties");
        assertThat(((Map<String, Object>) properties.get("algorithm_type")).get("enum"), is(List.of("SQL_HINT")));
        Map<String, Object> structuredIntentProperties = (Map<String, Object>) ((Map<String, Object>) properties.get("structured_intent_evidence")).get("properties");
        assertThat(((Map<String, Object>) structuredIntentProperties.get("algorithm_type")).get("enum"), is(List.of("SQL_HINT")));
    }
    
    @Test
    void assertAlgorithmResourceScope() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        assertThat(findResourceMetadata(catalog, "shardingsphere://features/shadow/databases/{database}/algorithms").getObjectScope(), is("algorithm"));
    }
    
    private MCPToolDescriptor findTool(final MCPDescriptorCatalog catalog, final String toolName) {
        return catalog.getProtocolDescriptors().getToolDescriptors().stream().filter(each -> toolName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private ShardingSphereMCPResourceMetadata findResourceMetadata(final MCPDescriptorCatalog catalog, final String uriTemplate) {
        return catalog.getShardingSphereDescriptors().getResourceMetadata().stream().filter(each -> uriTemplate.equals(each.getUriTemplate())).findFirst().orElseThrow();
    }
    
    private void assertCompletionTargetArguments(final MCPDescriptorCatalog catalog, final String promptName, final String... expectedArguments) {
        MCPCompletionTargetDescriptor actual = catalog.getShardingSphereDescriptors().getCompletionTargetDescriptors().stream()
                .filter(each -> "prompt".equals(each.getReferenceType()) && promptName.equals(each.getReference())).findFirst().orElseThrow();
        assertThat(actual.getArguments(), is(List.of(expectedArguments)));
    }
}
