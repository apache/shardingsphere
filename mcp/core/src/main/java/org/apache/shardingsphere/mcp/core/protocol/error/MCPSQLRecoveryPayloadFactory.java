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
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.RuleDistSQLExecutionException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;

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
    
    static Map<String, Object> createRuleDistSQLExecutionRecovery(final RuleDistSQLExecutionException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery("rule_distsql_execution_failed",
                "Do not ask the user to rewrite the SQL yet; read workflow guidance and verify the runtime database can execute ShardingSphere rule DistSQL.");
        ClassificationResult classificationResult = cause.getClassificationResult();
        result.put("database", cause.getDatabase());
        result.put("statement_class", classificationResult.getStatementClass().name().toLowerCase(Locale.ENGLISH));
        result.put("statement_type", classificationResult.getStatementType());
        result.put("side_effect_scope", List.of(classificationResult.getSideEffectScope()));
        result.put("secret_safe", true);
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, createRuleDistSQLExecutionResources(cause.getDatabase()));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createRuleDistSQLExecutionNextActions(cause.getDatabase()));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static List<Map<String, Object>> createRuleDistSQLExecutionResources(final String database) {
        return List.of(
                MCPResourceHintUtils.create("shardingsphere://guidance", "guidance", "read_first",
                        "Read workflow guidance before retrying rule DistSQL execution.", MCPPayloadFieldNames.RESOURCES_TO_READ),
                createDatabaseCapabilityResourceHint(database));
    }
    
    private static Map<String, Object> createDatabaseCapabilityResourceHint(final String database) {
        return MCPResourceHintUtils.create(createDatabaseCapabilityUri(database), database.isEmpty() ? "logical-database" : "logical-database-capability", "read_first",
                createDatabaseCapabilityReason(database), MCPPayloadFieldNames.RESOURCES_TO_READ);
    }
    
    private static String createDatabaseCapabilityUri(final String database) {
        return database.isEmpty() ? "shardingsphere://databases" : String.format("shardingsphere://databases/%s/capabilities", MCPUriPathSegmentUtils.encodePathSegment(database));
    }
    
    private static String createDatabaseCapabilityReason(final String database) {
        return database.isEmpty()
                ? "Choose a configured logical database before retrying rule DistSQL execution."
                : "Verify the runtime database capabilities before retrying rule DistSQL execution.";
    }
    
    private static List<Map<String, Object>> createRuleDistSQLExecutionNextActions(final String database) {
        return MCPNextActionUtils.ordered(
                MCPNextActionUtils.readResource("shardingsphere://guidance", "Read workflow guidance and choose the matching database_gateway_plan_* workflow tool for rule changes."),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.readResource(createDatabaseCapabilityUri(database), createDatabaseCapabilityReason(database)), 1));
    }
    
    static Map<String, Object> createMetadataIntrospectionSQLRecovery(final MetadataIntrospectionSQLStatementException cause) {
        Map<String, Object> result = MCPRecoveryPayloadSupport.createBaseRecovery(
                "metadata_introspection_sql", "Use logical metadata resources or database_gateway_search_metadata instead of console-style metadata SQL.");
        result.put("statement_type", cause.getStatementType());
        List<Map<String, Object>> resourcesToRead = createMetadataIntrospectionResources(cause.getStatementType());
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, resourcesToRead);
        Map<String, Object> suggestedArguments = createMetadataSearchArguments(cause.getStatementType());
        result.put("suggested_arguments", suggestedArguments);
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createMetadataIntrospectionNextActions(resourcesToRead, suggestedArguments));
        result.put("ask_user_when_uncertain", false);
        return result;
    }
    
    private static List<Map<String, Object>> createMetadataIntrospectionResources(final String statementType) {
        switch (statementType) {
            case "SHOW STORAGE UNITS":
                return List.of(createResourceHint("shardingsphere://databases/{database}/storage-units", "storage-unit",
                        "Read storage units from the target logical database instead of executing SHOW STORAGE UNITS."));
            case "SHOW RULES USED STORAGE UNIT":
                return List.of(
                        createResourceHint("shardingsphere://databases/{database}/storage-units", "storage-unit",
                                "Choose the storage unit from the target logical database."),
                        createResourceHint("shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules", "storage-unit",
                                "Read rules that use the chosen storage unit."));
            case "SHOW SINGLE TABLES":
            case "SHOW SINGLE TABLE":
                return List.of(createResourceHint("shardingsphere://databases/{database}/single-tables", "single-table",
                        "Read single table mappings from the target logical database instead of executing SHOW SINGLE TABLE."));
            case "SHOW DEFAULT SINGLE TABLE STORAGE UNIT":
                return List.of(createResourceHint("shardingsphere://databases/{database}/single-table/default-storage-unit", "single-table",
                        "Read the default single table storage unit resource instead of executing SHOW DEFAULT SINGLE TABLE STORAGE UNIT."));
            default:
                return MCPRecoveryPayloadSupport.createResourceHintList(
                        "shardingsphere://databases", "logical-database", "Read logical databases before choosing a metadata scope.");
        }
    }
    
    private static Map<String, Object> createMetadataSearchArguments(final String statementType) {
        return statementType.startsWith("SHOW STORAGE") || statementType.startsWith("SHOW RULES USED STORAGE UNIT")
                ? Map.of("object_types", List.of("storage_unit"))
                : Map.of("object_types", List.of("database"));
    }
    
    private static List<Map<String, Object>> createMetadataIntrospectionNextActions(final List<Map<String, Object>> resourcesToRead,
                                                                                    final Map<String, Object> suggestedArguments) {
        Map<String, Object> resource = resourcesToRead.getFirst();
        Map<String, Object> readResource = MCPNextActionUtils.readResource(
                String.valueOf(resource.get(MCPPayloadFieldNames.URI)), String.valueOf(resource.get(MCPPayloadFieldNames.REASON)));
        if (List.of("storage_unit").equals(suggestedArguments.get("object_types"))) {
            return MCPNextActionUtils.ordered(readResource,
                    MCPNextActionUtils.dependsOn(MCPNextActionUtils.callTool("database_gateway_search_metadata",
                            "Search storage units with an explicit database or query scope instead of executing metadata SQL.", suggestedArguments), 1));
        }
        return MCPNextActionUtils.ordered(readResource,
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.callTool("database_gateway_search_metadata",
                        "Search metadata with an explicit database, schema, query, or object_types scope instead of executing metadata SQL.", suggestedArguments), 1));
    }
    
    private static Map<String, Object> createResourceHint(final String uri, final String resourceKind, final String reason) {
        return MCPResourceHintUtils.create(uri, resourceKind, "read_first", reason, MCPPayloadFieldNames.RESOURCES_TO_READ);
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
