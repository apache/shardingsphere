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

package org.apache.shardingsphere.mcp.feature.sharding.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingToolDescriptorValidatorTest {
    
    @Test
    void assertSupports() {
        ShardingToolDescriptorValidator validator = new ShardingToolDescriptorValidator();
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_REFERENCE_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_KEY_GENERATOR_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME)));
        assertTrue(validator.supports(MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_COMPONENT_CLEANUP_TOOL_NAME)));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertInputSchemaIsShardingSpecific() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME);
        Map<String, Object> properties = (Map<String, Object>) descriptor.getInputSchema().get("properties");
        assertTrue(properties.containsKey("data_nodes"));
        assertTrue(properties.containsKey("storage_units"));
        assertTrue(properties.containsKey("sharding_columns"));
        assertTrue(properties.containsKey("algorithm_properties"));
        assertTrue(properties.containsKey("key_generator_properties"));
        assertTrue(properties.containsKey("reference_tables"));
        assertTrue(properties.containsKey("auditors"));
        assertFalse(properties.containsKey("ddl_artifacts"));
    }
    
    @Test
    void assertPromptGuardsPhysicalOperations() throws IOException {
        String prompt = readResource("META-INF/shardingsphere-mcp/prompts/plan-sharding-table-rule.md");
        assertTrue(prompt.contains("Do not create physical databases"));
        assertTrue(prompt.contains("storage units"));
        assertTrue(prompt.contains("data probes"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertValidateRejectsMissingOutputField() {
        MCPToolDescriptor descriptor = MCPDescriptorCatalogIndex.getRequiredToolDescriptor(ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME);
        Map<String, Object> outputSchema = new LinkedHashMap<>(descriptor.getOutputSchema());
        Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) outputSchema.get("properties"));
        properties.remove("resources_to_read");
        outputSchema.put("properties", properties);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> new ShardingToolDescriptorValidator().validate(new MCPToolDescriptor(
                descriptor.getName(), descriptor.getTitle(), descriptor.getDescription(), descriptor.getInputSchema(), outputSchema, descriptor.getAnnotations(), descriptor.getMeta())));
        assertThat(actual.getMessage(), is("Tool `database_gateway_plan_sharding_table_rule` outputSchema must declare `resources_to_read`."));
    }
    
    @Test
    void assertSPIRegistration() {
        assertTrue(ServiceLoader.load(MCPToolDescriptorValidator.class).stream().anyMatch(each -> ShardingToolDescriptorValidator.class.equals(each.type())));
    }
    
    private String readResource(final String resourceName) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
