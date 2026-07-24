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
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;

import java.util.List;
import java.util.Map;

/**
 * MCP runtime database recovery payload factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPRuntimeDatabaseRecoveryPayloadFactory {
    
    /**
     * Create a runtime database recovery payload for a validation diagnostic.
     *
     * @param database database name
     * @param category recovery category
     * @return recovery payload
     */
    public static Map<String, Object> create(final String database, final String category) {
        Map<String, Object> result = create(category);
        if (!database.isBlank()) {
            result.put(WorkflowFieldNames.DATABASE, database);
        }
        return result;
    }
    
    static Map<String, Object> create(final RuntimeDatabaseConnectionException cause) {
        Map<String, Object> result = create(cause.getCategory());
        result.put(WorkflowFieldNames.DATABASE, cause.getDatabaseName());
        return result;
    }
    
    private static Map<String, Object> create(final String category) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(category, createModelAction(category));
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, createResourcesToRead(category));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createNextActions(category));
        return result;
    }
    
    private static String createModelAction(final String category) {
        if (RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER.equals(category)) {
            return "Install or configure the JDBC driver for the MCP runtime database, then retry.";
        }
        if (RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED.equals(category)) {
            return "Check the runtime database credentials outside MCP, then retry.";
        }
        if (RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED.equals(category)) {
            return "Check runtime database account privileges outside MCP, then retry.";
        }
        if (RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT.equals(category)) {
            return "Check database reachability and timeout settings, then retry.";
        }
        if (RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION.equals(category)) {
            return "Fix the MCP runtime database configuration outside MCP, then retry.";
        }
        if (RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE.equals(category)) {
            return "Connect to the intended logical database or update the expected database name before retrying.";
        }
        return "Check the runtime database availability and configuration, then retry.";
    }
    
    private static List<Map<String, Object>> createResourcesToRead(final String category) {
        Map<String, Object> runtimeResource = MCPResourceHintUtils.create(
                "shardingsphere://runtime", "runtime", "read_first", "Read current MCP runtime status before retrying.", MCPPayloadFieldNames.RESOURCES_TO_READ);
        if (!RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE.equals(category)) {
            return List.of(runtimeResource);
        }
        return List.of(runtimeResource, MCPResourceHintUtils.create(
                "shardingsphere://databases", "logical-database", "read_first", "Read visible logical databases before retrying.", MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private static List<Map<String, Object>> createNextActions(final String category) {
        Map<String, Object> readRuntime = MCPNextActionUtils.readResource("shardingsphere://runtime", "Read current MCP runtime status and configured database visibility.");
        if (!RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE.equals(category)) {
            return MCPNextActionUtils.ordered(readRuntime,
                    MCPNextActionUtils.dependsOn(MCPNextActionUtils.askUser(
                            "Ask the operator to fix the MCP runtime database configuration before retrying.", List.of("runtime_database_configuration")), 1));
        }
        return MCPNextActionUtils.ordered(readRuntime,
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.readResource("shardingsphere://databases", "Read visible logical databases before retrying."), 1),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.askUser(
                        "Ask the operator to choose a visible logical database or update runtimeDatabases before retrying.",
                        List.of(WorkflowFieldNames.DATABASE, "runtimeDatabases")), 2));
    }
}
