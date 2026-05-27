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

package org.apache.shardingsphere.mcp.core.protocol.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP recovery payload support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPRecoveryPayloadSupport {
    
    static Map<String, Object> createBaseRecovery(final String category, final String modelAction) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("response_mode", MCPResponseMode.RECOVERY);
        result.put("recovery_category", normalizeRecoveryCategory(category));
        result.put("recoverable", true);
        result.put("category", category);
        result.put("model_action", modelAction);
        return result;
    }
    
    static Map<String, Object> getSuggestedArguments(final Map<String, Object> suggestedArguments, final Map<String, Object> defaultArguments) {
        return suggestedArguments.isEmpty() ? defaultArguments : suggestedArguments;
    }
    
    static List<Map<String, Object>> createResourceHintList(final String uri, final String resourceKind, final String reason) {
        return List.of(MCPResourceHintUtils.create(uri, resourceKind, "read_first", reason, MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private static String normalizeRecoveryCategory(final String category) {
        if (isRuntimeRecoveryCategory(category)) {
            return "unavailable_runtime";
        }
        if ("banned_sql_statement".equals(category)) {
            return "terminal_operator_action";
        }
        if (category.startsWith("missing_")) {
            return "missing_context";
        }
        if (category.startsWith("unsupported_") || "read_only_sql_sent_to_update_tool".equals(category)) {
            return "unsupported_target";
        }
        if ("invalid_enum_value".equals(category)) {
            return "invalid_enum";
        }
        if ("unsafe_sql_attempted".equals(category) || "multiple_sql_statements".equals(category)) {
            return "unsafe_sql";
        }
        if ("stale_workflow".equals(category) || "workflow_state_error".equals(category)) {
            return "stale_workflow";
        }
        if (category.endsWith("_not_found")) {
            return "not_found";
        }
        if (category.contains("conflict")) {
            return "ambiguous";
        }
        return "validation";
    }
    
    private static boolean isRuntimeRecoveryCategory(final String category) {
        return RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER.equals(category) || RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED.equals(category)
                || RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT.equals(category) || RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION.equals(category)
                || RuntimeDatabaseConnectionException.CATEGORY_DATABASE_UNAVAILABLE.equals(category) || RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED.equals(category);
    }
}
