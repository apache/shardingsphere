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
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MCP SQL recovery payload factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPSQLRecoveryPayloadFactory {
    
    static Map<String, Object> createSQLToolMismatchRecovery(final SQLToolMismatchException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(createSQLToolMismatchCategory(cause),
                "database_gateway_execute_update".equals(cause.getTargetTool())
                        ? "Use database_gateway_execute_update in preview mode, then execute only when the requested side effect is still intended."
                        : "Use database_gateway_execute_query for this read-only SQL.");
        result.put("source_tool", cause.getSourceTool());
        result.put("statement_class", cause.getClassificationResult().getStatementClass().name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", cause.getClassificationResult().getStatementType());
        result.put("normalized_sql", cause.getClassificationResult().getNormalizedSql());
        cause.getClassificationResult().getTargetObjectName().ifPresent(optional -> result.put("target_object", optional));
        cause.getClassificationResult().getSavepointName().ifPresent(optional -> result.put("savepoint", optional));
        result.put("suggested_arguments", cause.getSuggestedArguments());
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.callTool(cause.getTargetTool(), createSQLToolMismatchActionReason(cause), cause.getSuggestedArguments())));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    static Map<String, Object> createMetadataIntrospectionSQLRecovery(final MetadataIntrospectionSQLStatementException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "metadata_introspection_sql", "Use logical metadata resources or database_gateway_search_metadata instead of console-style metadata SQL.");
        result.put("statement_type", cause.getStatementType());
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, MCPRecoveryPayloadSupport.createResourceHintList(
                "shardingsphere://databases", "logical-database", "Read logical databases before choosing a metadata scope."));
        result.put("suggested_arguments", Map.of("object_types", List.of("database")));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, MCPNextActionUtils.ordered(
                MCPNextActionUtils.readResource("shardingsphere://databases", "Read logical databases before choosing a metadata scope."),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.callTool("database_gateway_search_metadata",
                        "Search metadata with an explicit database, schema, query, or object_types scope instead of executing metadata SQL.",
                        Map.of("object_types", List.of("database"))), 1)));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    static Map<String, Object> createMultipleStatementsRecovery() {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "multiple_sql_statements", "Split the user intent into separate MCP calls and handle each statement independently.");
        result.put("ask_user_when_uncertain", true);
        result.put("suggested_arguments", Map.of(MCPPayloadFieldNames.EXECUTION_MODE, "preview"));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.askUser(
                "Ask the user which single statement should be handled first.", List.of("single_sql_statement"))));
        return result;
    }
    
    static Map<String, Object> createUnsupportedStatementRecovery() {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "unsupported_sql_statement", "Ask the user for a supported SELECT, EXPLAIN ANALYZE, DML, DDL, DCL, transaction, or savepoint statement.");
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, MCPRecoveryPayloadSupport.createResourceHintList(
                "shardingsphere://capabilities", "capability", "Read supported SQL statement classes before retrying."));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read supported statement classes before retrying.")));
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    static Map<String, Object> createBannedStatementRecovery() {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "banned_sql_statement", "Do not execute this SQL through MCP; ask the user for a safer supported operation.");
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, MCPRecoveryPayloadSupport.createResourceHintList(
                "shardingsphere://capabilities", "capability", "Read supported safe alternatives before asking the user."));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, List.of(MCPNextActionUtils.askUser(
                "Ask for a safer supported operation instead of executing the banned SQL.", List.of("safe_sql_or_metadata_request"))));
        result.put("ask_user_when_uncertain", true);
        return result;
    }
    
    private static String createSQLToolMismatchCategory(final SQLToolMismatchException cause) {
        return "database_gateway_execute_update".equals(cause.getTargetTool()) ? "unsafe_sql_attempted" : "read_only_sql_sent_to_update_tool";
    }
    
    private static String createSQLToolMismatchActionReason(final SQLToolMismatchException cause) {
        return "database_gateway_execute_update".equals(cause.getTargetTool())
                ? "Retry side-effecting SQL in preview mode with the normalized SQL and preserved context."
                : "Retry the read-only SQL with database_gateway_execute_query using the normalized SQL and preserved context.";
    }
}
