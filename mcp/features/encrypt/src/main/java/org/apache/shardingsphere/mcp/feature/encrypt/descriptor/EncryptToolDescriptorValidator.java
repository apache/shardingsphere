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

package org.apache.shardingsphere.mcp.feature.encrypt.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Encrypt tool descriptor validator.
 */
public final class EncryptToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    private static final Collection<String> REQUIRED_SECRET_WORKFLOW_OUTPUT_FIELDS = List.of("masked_property_preview", "secret_reference_summary");
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return EncryptFeatureDefinition.PLAN_TOOL_NAME.equals(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanOutputFields(toolDescriptor, REQUIRED_SECRET_WORKFLOW_OUTPUT_FIELDS);
        validateExecutableDistSQLArtifacts(toolDescriptor);
    }
    
    private void validateExecutableDistSQLArtifacts(final MCPToolDescriptor toolDescriptor) {
        Object examples = toolDescriptor.getOutputSchema().get("examples");
        if (examples instanceof Collection) {
            for (Object each : (Collection<?>) examples) {
                validateExecutableDistSQLArtifact(toolDescriptor, each);
            }
        }
    }
    
    private void validateExecutableDistSQLArtifact(final MCPToolDescriptor toolDescriptor, final Object value) {
        if (value instanceof Map) {
            validateExecutableDistSQLArtifactMap(toolDescriptor, (Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                validateExecutableDistSQLArtifact(toolDescriptor, each);
            }
        }
    }
    
    private void validateExecutableDistSQLArtifactMap(final MCPToolDescriptor toolDescriptor, final Map<?, ?> value) {
        Object sql = value.get("sql");
        if (null != sql && isEncryptRuleDistSQL(sql.toString())) {
            validateEncryptRuleDistSQL(toolDescriptor, sql.toString());
        }
        for (Object each : value.values()) {
            validateExecutableDistSQLArtifact(toolDescriptor, each);
        }
    }
    
    private boolean isEncryptRuleDistSQL(final String sql) {
        String actualSQL = sql.toUpperCase(Locale.ENGLISH);
        return actualSQL.contains("CREATE ENCRYPT RULE") || actualSQL.contains("ALTER ENCRYPT RULE");
    }
    
    private void validateEncryptRuleDistSQL(final MCPToolDescriptor toolDescriptor, final String sql) {
        String actualSQL = sql.toLowerCase(Locale.ENGLISH);
        if (actualSQL.contains("type(name=aes")) {
            throw new IllegalStateException(String.format("Tool `%s` output example executable encrypt DistSQL must quote algorithm type as a string literal.", toolDescriptor.getName()));
        }
        if (actualSQL.contains("'aes-key-value'") && !actualSQL.contains("'digest-algorithm-name'")) {
            throw new IllegalStateException(String.format("Tool `%s` output example executable AES DistSQL must include `digest-algorithm-name`.", toolDescriptor.getName()));
        }
    }
}
