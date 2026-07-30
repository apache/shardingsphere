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

package org.apache.shardingsphere.mcp.feature.encrypt;

import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptDescriptorContractTest {
    
    @Test
    void assertEncryptDistSQLExamples() {
        assertEncryptDistSQLExampleValue(findToolDescriptor().getOutputSchema().get("examples"));
    }
    
    @Test
    void assertPlanningOutputDeclaresSummary() {
        Map<?, ?> actualOutputSchema = findToolDescriptor().getOutputSchema();
        Map<?, ?> actualProperties = (Map<?, ?>) actualOutputSchema.get("properties");
        assertTrue(actualProperties.containsKey("summary"));
    }
    
    @Test
    void assertPromptCompletionArguments() {
        assertCompletionTargetArguments(EncryptFeatureDefinition.PLAN_PROMPT_NAME, "database", "schema", "table", "column", "algorithm_type", "assisted_query_algorithm_type",
                "like_query_algorithm_type", "plan_id");
    }
    
    @Test
    void assertPlanIdInputGuidesNewPlanOmission() {
        MCPToolDescriptor actualDescriptor = findToolDescriptor();
        Map<?, ?> actualPlanIdInput = MCPToolDescriptorValidationUtils.findToolInputProperty(actualDescriptor, "plan_id").orElseThrow();
        String actualDescription = actualPlanIdInput.get("description").toString();
        assertFalse(MCPToolDescriptorValidationUtils.isRequiredToolInput(actualDescriptor, "plan_id"));
        assertTrue(actualDescription.contains("Omit when starting a new plan"));
        assertTrue(actualDescription.contains("never use placeholder values"));
    }
    
    @Test
    void assertPlanToolOperationTypesExposeOnlySupportedLifecycleActions() {
        Map<?, ?> actualOperationType = MCPToolDescriptorValidationUtils.findToolInputProperty(findToolDescriptor(), "operation_type").orElseThrow();
        assertThat(actualOperationType.get("enum"), is(List.of("create", "drop")));
    }
    
    private MCPToolDescriptor findToolDescriptor() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        return catalog.getProtocolDescriptors().getToolDescriptors().stream()
                .filter(each -> EncryptFeatureDefinition.PLAN_TOOL_NAME.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private void assertCompletionTargetArguments(final String promptName, final String... expectedArguments) {
        MCPCompletionTargetDescriptor actual = MCPDescriptorCatalogLoader.load().getShardingSphereDescriptors().getCompletionTargetDescriptors().stream()
                .filter(each -> "prompt".equals(each.getReferenceType()) && promptName.equals(each.getReference())).findFirst().orElseThrow();
        assertThat(actual.getArguments(), is(List.of(expectedArguments)));
    }
    
    private void assertEncryptDistSQLExampleValue(final Object value) {
        if (value instanceof Map) {
            assertEncryptDistSQLExampleMap((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                assertEncryptDistSQLExampleValue(each);
            }
        }
    }
    
    private void assertEncryptDistSQLExampleMap(final Map<?, ?> value) {
        Object sql = value.get("sql");
        if (null != sql && isEncryptRuleDistSQL(sql.toString())) {
            assertEncryptRuleDistSQL(sql.toString());
        }
        for (Object each : value.values()) {
            assertEncryptDistSQLExampleValue(each);
        }
    }
    
    private boolean isEncryptRuleDistSQL(final String sql) {
        String actualSQL = sql.toUpperCase(Locale.ENGLISH);
        return actualSQL.contains("CREATE ENCRYPT RULE");
    }
    
    private void assertEncryptRuleDistSQL(final String sql) {
        String actualSQL = sql.toLowerCase(Locale.ENGLISH);
        assertFalse(actualSQL.contains("type(name=aes"));
        assertFalse(actualSQL.contains("'aes-key-value'") && !actualSQL.contains("'digest-algorithm-name'"));
    }
}
