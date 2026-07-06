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

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalog;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogLoader;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

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
    
    private MCPToolDescriptor findToolDescriptor() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        return catalog.getProtocolDescriptors().getToolDescriptors().stream()
                .filter(each -> EncryptFeatureDefinition.PLAN_TOOL_NAME.equals(each.getName())).findFirst().orElseThrow();
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
        return actualSQL.contains("CREATE ENCRYPT RULE") || actualSQL.contains("ALTER ENCRYPT RULE");
    }
    
    private void assertEncryptRuleDistSQL(final String sql) {
        String actualSQL = sql.toLowerCase(Locale.ENGLISH);
        assertFalse(actualSQL.contains("type(name=aes"));
        assertFalse(actualSQL.contains("'aes-key-value'") && !actualSQL.contains("'digest-algorithm-name'"));
    }
}
