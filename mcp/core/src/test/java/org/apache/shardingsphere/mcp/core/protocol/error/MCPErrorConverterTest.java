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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidApprovedStepsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidExecutionModeException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidMetadataObjectTypesException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMissingToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMultipleSQLStatementsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolArgumentContractViolationException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolCallLimitExceededException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ExplainSQLSyntaxException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.RuleDistSQLExecutionException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPErrorConverterTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertConvertCases")
    void assertConvert(final String name, final Throwable cause, final String expectedMessage) {
        MCPErrorResponse actual = MCPErrorConverter.convert(cause);
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("response_mode"), is("recovery"));
        assertThat(actualPayload.get("message"), is(expectedMessage));
        assertTrue(String.valueOf(actualPayload.get("request_id")).matches("[0-9a-f\\-]{36}"));
        assertFalse(actualPayload.containsKey("recovery"));
    }
    
    @Test
    void assertConvertUnsupportedToolWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new UnsupportedToolException("missing_tool")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("unsupported_tool"));
        assertThat(actualRecovery.get("recovery_category"), is("unsupported_target"));
        assertThat(actualRecovery.get("tool_name"), is("missing_tool"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("resource_uri"), is("shardingsphere://capabilities"));
        assertTrue(((Collection<?>) actualRecovery.get("supported_tools")).contains("database_gateway_execute_query"));
    }
    
    @Test
    void assertConvertUnsupportedResourceWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new UnsupportedResourceUriException("shardingsphere://unknown")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("unsupported_resource"));
        assertThat(actualRecovery.get("recovery_category"), is("unsupported_target"));
        assertThat(((Map<?, ?>) actualRecovery.get("resource")).get("uri"), is("shardingsphere://unknown"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("type"), is("resource_read"));
        assertTrue(((Collection<?>) actualRecovery.get("matching_resource_templates")).contains("shardingsphere://capabilities"));
    }
    
    @Test
    void assertConvertMissingArgumentWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPMissingToolArgumentException("database")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("missing_database"));
        assertThat(actualRecovery.get("recovery_category"), is("missing_context"));
        assertThat(actualRecovery.get("missing_fields"), is(List.of("database")));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("resource_uri"), is("shardingsphere://databases"));
        assertTrue((Boolean) actualRecovery.get("ask_user_when_uncertain"));
    }
    
    @Test
    void assertConvertMissingExecutionModeWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPExecutionModeRequiredException("database_gateway_execute_update", List.of("execute", "preview"), Map.of())).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("missing_execution_mode"));
        assertThat(actualRecovery.get("recovery_category"), is("missing_context"));
        assertThat(actualRecovery.get("missing_fields"), is(List.of("execution_mode")));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_execute_update"));
    }
    
    @Test
    void assertConvertMissingWorkflowExecutionModeWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPExecutionModeRequiredException(
                "database_gateway_apply_workflow", List.of("preview", "review-then-execute", "manual-only"), Map.of())).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("missing_execution_mode"));
        assertThat(actualRecovery.get("recovery_category"), is("missing_context"));
        assertThat(actualRecovery.get("field"), is("execution_mode"));
        assertThat(actualRecovery.get("source_tool"), is("database_gateway_apply_workflow"));
        assertThat(actualRecovery.get("tool_name"), is("database_gateway_apply_workflow"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("preview", "review-then-execute", "manual-only")));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
    }
    
    @Test
    void assertConvertInvalidWorkflowExecutionModeWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidExecutionModeException(
                "database_gateway_apply_workflow", List.of("preview", "review-then-execute", "manual-only"), Map.of())).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("recovery_category"), is("invalid_enum"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("preview", "review-then-execute", "manual-only")));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("tool_name"), is("database_gateway_apply_workflow"));
    }
    
    @Test
    void assertConvertInvalidApprovedStepsWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidApprovedStepsException(List.of("ddl", "index_ddl", "rule_distsql"), Map.of())).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("recovery_category"), is("invalid_enum"));
        assertThat(actualRecovery.get("field"), is("approved_steps"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("ddl", "index_ddl", "rule_distsql")));
        assertThat(actualRecovery.get("model_action"),
                is("Retry database_gateway_apply_workflow with execution_mode=preview, review the returned preview_artifacts, "
                        + "then pass explicit approved_steps copied from visible preview_artifacts.approval_step values."));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
        assertThat(actualNextAction.get("arguments"), is(Map.of("execution_mode", "preview")));
    }
    
    @Test
    void assertConvertInvalidObjectTypesWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidMetadataObjectTypesException("unknown",
                List.of("database", "schema", "table", "view", "column", "index", "sequence"))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("recovery_category"), is("invalid_enum"));
        assertThat(actualRecovery.get("field"), is("object_types"));
        assertTrue(((Collection<?>) actualRecovery.get("allowed_values")).contains("table"));
        assertTrue(((Collection<?>) ((Map<?, ?>) actualRecovery.get("suggested_arguments")).get("object_types")).contains("table"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
    }
    
    @Test
    void assertConvertInvalidSQLIntegerArgumentWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidToolArgumentException("database_gateway_execute_query", "database_gateway_execute_query", "max_rows", 0, 5000, 100,
                new MCPInvalidRequestException("max_rows must be an integer."))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_integer_argument"));
        assertThat(actualRecovery.get("argument_path"), is("max_rows"));
        assertThat(actualRecovery.get("source_tool"), is("database_gateway_execute_query"));
        assertThat(actualRecovery.get("tool_name"), is("database_gateway_execute_query"));
        assertThat(actualRecovery.get("minimum_value"), is(0));
        assertThat(actualRecovery.get("maximum_value"), is(5000));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("max_rows", 100)));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_execute_query"));
    }
    
    @Test
    void assertConvertInvalidIntegerArgumentWithoutTargetAsksUser() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidToolArgumentException("", "", "timeout_ms", 0, 300000, 0,
                new MCPInvalidRequestException("timeout_ms must be an integer."))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst();
        assertThat(actualRecovery.get("argument_path"), is("timeout_ms"));
        assertThat(actualNextAction.get("type"), is("ask_user"));
        assertFalse(actualNextAction.containsKey("tool_name"));
    }
    
    @Test
    void assertConvertToolArgumentContractViolationWithRecovery() {
        Map<String, Object> suggestedArguments = Map.of("query", "order");
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPToolArgumentContractViolationException("database_gateway_search_metadata", "object_types[0]",
                "invalid_enum_value", "", List.of("database", "schema", "table"), suggestedArguments)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("recovery_category"), is("invalid_enum"));
        assertThat(actualRecovery.get("field"), is("object_types[0]"));
        assertThat(actualRecovery.get("argument_path"), is("object_types[0]"));
        assertThat(actualRecovery.get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("database", "schema", "table")));
        assertThat(actualRecovery.get("suggested_arguments"), is(suggestedArguments));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst();
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
    }
    
    @Test
    void assertConvertMultipleStatementsWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPMultipleSQLStatementsException()).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("multiple_sql_statements"));
        assertThat(actualRecovery.get("recovery_category"), is("unsafe_sql"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        assertTrue((Boolean) actualRecovery.get("ask_user_when_uncertain"));
    }
    
    @Test
    void assertConvertUnsupportedSQLWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPUnsupportedSQLStatementException()).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("unsupported_sql_statement"));
        assertThat(actualRecovery.get("recovery_category"), is("unsupported_target"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertConvertBannedSQLWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPBannedSQLStatementException()).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("banned_sql_statement"));
        assertThat(actualRecovery.get("recovery_category"), is("terminal_operator_action"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("type"), is("ask_user"));
    }
    
    @Test
    void assertConvertUnsupportedMessageWithoutRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPUnsupportedException(
                "database_gateway_execute_query only supports classifier-approved QUERY statements.")).toPayload();
        assertFalse(actual.containsKey("recovery"));
    }
    
    @Test
    void assertConvertSQLToolMismatchWithRecovery() {
        ClassificationResult classificationResult = new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'PAID'", "orders", "");
        Map<String, Object> suggestedArguments = Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID'", "execution_mode", "preview");
        Map<String, Object> actual = MCPErrorConverter.convert(new SQLToolMismatchException(
                "database_gateway_execute_query only supports classifier-approved QUERY statements. "
                        + "Use database_gateway_execute_explain_query for EXPLAIN diagnostics or database_gateway_execute_update for side-effecting SQL.",
                "database_gateway_execute_query", "database_gateway_execute_update", classificationResult, suggestedArguments)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("unsafe_sql_attempted"));
        assertThat(actualRecovery.get("recovery_category"), is("unsafe_sql"));
        assertThat(actualRecovery.get("source_tool"), is("database_gateway_execute_query"));
        assertThat(actualRecovery.get("normalized_sql"), is("UPDATE orders SET status = 'PAID'"));
        assertThat(actualRecovery.get("suggested_arguments"), is(suggestedArguments));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("arguments"), is(suggestedArguments));
    }
    
    @Test
    void assertConvertExplainSQLSyntaxWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new ExplainSQLSyntaxException("logic_db", "public", "SELECT * FROM orders", "EXPLAIN BROKEN SELECT * FROM orders",
                new MCPInvalidRequestException("bad explain", new SQLSyntaxErrorException("bad explain")))).toPayload();
        assertThat(actual.get("message"), is("Generated explain_sql is not valid for the target database."));
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_explain_sql"));
        assertThat(actualRecovery.get("rejected_explain_sql"), is("EXPLAIN BROKEN SELECT * FROM orders"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders")));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("reason"),
                is("Read the target database type before regenerating database-native explain_sql."));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("tool_name"), is("database_gateway_execute_explain_query"));
        assertFalse((Boolean) actualRecovery.get("ask_user_when_uncertain"));
    }
    
    @Test
    void assertConvertReadOnlySQLToolMismatchWithRecovery() {
        ClassificationResult classificationResult = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM orders", "orders", "");
        Map<String, Object> suggestedArguments = Map.of("database", "logic_db", "sql", "SELECT * FROM orders");
        Map<String, Object> actual = MCPErrorConverter.convert(new SQLToolMismatchException(
                "database_gateway_execute_update does not accept read-only SQL. Use database_gateway_execute_query for read-only SQL.",
                "database_gateway_execute_update", "database_gateway_execute_query", classificationResult, suggestedArguments)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("read_only_sql_sent_to_update_tool"));
        assertThat(actualRecovery.get("recovery_category"), is("unsupported_target"));
        assertThat(actualRecovery.get("normalized_sql"), is("SELECT * FROM orders"));
        assertThat(actualRecovery.get("suggested_arguments"), is(suggestedArguments));
    }
    
    @Test
    void assertConvertRuleDistSQLExecutionWithRecovery() {
        ClassificationResult classificationResult = new ClassificationResult(SupportedMCPStatement.DDL, "CREATE",
                "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'))", "", "");
        Map<String, Object> actual = MCPErrorConverter.convert(new RuleDistSQLExecutionException(
                "sharding_db", classificationResult, new SQLSyntaxErrorException("syntax error"))).toPayload();
        assertThat(actual.get("message"),
                is("Rule DistSQL execution failed for database `sharding_db`; check MCP runtime capability and workflow guidance before asking for corrected SQL."));
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("rule_distsql_execution_failed"));
        assertThat(actualRecovery.get("recovery_category"), is("rule_metadata_execution"));
        assertThat(actualRecovery.get("database"), is("sharding_db"));
        assertThat(actualRecovery.get("side_effect_scope"), is(List.of("rule-metadata")));
        assertTrue((Boolean) actualRecovery.get("secret_safe"));
        assertFalse((Boolean) actualRecovery.get("ask_user_when_uncertain"));
        assertThat(getResourceToRead(actualRecovery, 1).get("uri"), is("shardingsphere://databases/sharding_db/capabilities"));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("resource_uri"), is("shardingsphere://guidance"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("type"), is("resource_read"));
        assertFalse(String.valueOf(actual).contains("syntax error"));
    }
    
    @Test
    void assertConvertMetadataIntrospectionSQLWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MetadataIntrospectionSQLStatementException("SHOW")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("metadata_introspection_sql"));
        assertThat(actualRecovery.get("statement_type"), is("SHOW"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://databases"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("object_types", List.of("database"))));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("resource_uri"), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("arguments"), is(Map.of("object_types", List.of("database"))));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("depends_on"), is(List.of(1)));
    }
    
    @Test
    void assertConvertStorageUnitMetadataIntrospectionSQLWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MetadataIntrospectionSQLStatementException("SHOW STORAGE UNITS")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("statement_type"), is("SHOW STORAGE UNITS"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://databases/{database}/storage-units"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("object_types", List.of("storage_unit"))));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("resource_uri"), is("shardingsphere://databases/{database}/storage-units"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("arguments"), is(Map.of("object_types", List.of("storage_unit"))));
    }
    
    @Test
    void assertConvertDefaultSingleTableStorageUnitMetadataIntrospectionSQLWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MetadataIntrospectionSQLStatementException("SHOW DEFAULT SINGLE TABLE STORAGE UNIT")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("statement_type"), is("SHOW DEFAULT SINGLE TABLE STORAGE UNIT"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://databases/{database}/single-table/default-storage-unit"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("resource_uri"),
                is("shardingsphere://databases/{database}/single-table/default-storage-unit"));
    }
    
    @Test
    void assertConvertWorkflowStateWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPWorkflowStateException(
                "Unknown or unavailable plan_id `plan-missing`. Call the planning tool again in the current MCP session.", "plan-missing")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("stale_workflow"));
        assertThat(actualRecovery.get("recovery_category"), is("stale_workflow"));
        assertThat(actualRecovery.get("plan_id"), is("plan-missing"));
        assertThat(actualRecovery.get("completion_first"), is(Map.of("argument", "plan_id", "scope", "current MCP session")));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        Map<?, ?> actualCompletionAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst();
        assertThat(actualCompletionAction.get("type"), is("completion"));
        assertThat(actualCompletionAction.get("ref"), is(Map.of("type", "ref/resource", "uri", "shardingsphere://workflows/{plan_id}")));
        assertThat(actualCompletionAction.get("resume_ref"), is(Map.of("type", "ref/resource", "uri", "shardingsphere://workflows/{plan_id}")));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(1)).get("type"), is("resource_read"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(1)).get("depends_on"), is(List.of(1)));
    }
    
    @Test
    void assertConvertRuntimeDatabaseConnectionWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new RuntimeDatabaseConnectionException("logic_db",
                RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED, new SQLException("Access denied."))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("authentication_failed"));
        assertThat(actualRecovery.get("recovery_category"), is("unavailable_runtime"));
        assertThat(actualRecovery.get("database"), is("logic_db"));
        Map<?, ?> actualResourceToRead = getFirstResourceToRead(actualRecovery);
        assertThat(actualResourceToRead.get("uri"), is("shardingsphere://runtime"));
        assertThat(actualResourceToRead.get("resource_kind"), is("runtime"));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("resource_uri"), is("shardingsphere://runtime"));
        assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("order"), is(1));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("depends_on"), is(List.of(1)));
    }
    
    @Test
    void assertConvertRuntimeDatabaseAuthorizationWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new RuntimeDatabaseConnectionException("logic_db",
                RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED, new SQLException("permission denied"))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("authorization_failed"));
        assertThat(actualRecovery.get("recovery_category"), is("unavailable_runtime"));
        assertThat(actualRecovery.get("database"), is("logic_db"));
        assertThat(actualRecovery.get("model_action"), is("Check runtime database account privileges outside MCP, then retry."));
    }
    
    @Test
    void assertConvertRuntimeDatabaseNotVisibleWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(RuntimeDatabaseConnectionException.databaseNotVisible("logic_db", new IllegalStateException("not visible"))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("database_not_visible"));
        assertThat(actualRecovery.get("recovery_category"), is("validation"));
        assertThat(actualRecovery.get("database"), is("logic_db"));
        assertThat(actualRecovery.get("model_action"), is("Connect to the intended logical database or update the expected database name before retrying."));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://runtime"));
        Map<?, ?> actualDatabasesResourceToRead = getResourceToRead(actualRecovery, 1);
        assertThat(actualDatabasesResourceToRead.get("uri"), is("shardingsphere://databases"));
        assertThat(actualDatabasesResourceToRead.get("resource_kind"), is("logical-database"));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.getFirst()).get("resource_uri"), is("shardingsphere://runtime"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("resource_uri"), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("depends_on"), is(List.of(1)));
        assertThat(((Map<?, ?>) actualNextActions.get(2)).get("depends_on"), is(List.of(2)));
    }
    
    @Test
    void assertConvertToolCallLimitExceededWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPToolCallLimitExceededException("session-1", "database_gateway_search_metadata", 1)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("tool_call_limit_exceeded"));
        assertThat(actualRecovery.get("identity_scope"), is("mcp_session"));
        assertThat(actualRecovery.get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(actualRecovery.get("max_tool_calls_per_session"), is(1));
        Map<?, ?> actualResourceToRead = getFirstResourceToRead(actualRecovery);
        assertThat(actualResourceToRead.get("uri"), is("shardingsphere://guidance"));
        assertThat(actualResourceToRead.get("resource_kind"), is("guidance"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("resource_uri"), is("shardingsphere://guidance"));
    }
    
    @Test
    void assertConvertInvalidRuntimeConfigurationWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(RuntimeDatabaseConnectionException.invalidConfiguration("logic_db", new IllegalStateException("bad config"))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("message"), is("Runtime database `logic_db` connection failed: invalid_configuration."));
        assertThat(actualRecovery.get("category"), is("invalid_configuration"));
        assertThat(actualRecovery.get("recovery_category"), is("unavailable_runtime"));
        assertThat(actualRecovery.get("model_action"), is("Fix the MCP runtime database configuration outside MCP, then retry."));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://runtime"));
        assertTrue((Boolean) actualRecovery.get("ask_user_when_uncertain"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertConvertQueryFailureWithRecoveryCases")
    void assertConvertQueryFailureWithRecovery(final String name, final Throwable cause, final String expectedMessage, final String expectedCategory, final String expectedActionType) {
        Map<String, Object> actual = MCPErrorConverter.convert(cause).toPayload();
        assertThat(actual.get("message"), is(expectedMessage));
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is(expectedCategory));
        assertTrue((Boolean) actualRecovery.get("secret_safe"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).getFirst()).get("type"), is(expectedActionType));
        assertFalse(String.valueOf(actualRecovery).contains("SQLException"));
    }
    
    @Test
    void assertConvertQueryFailureRecoveryOmitsSensitiveFields() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPQueryFailedException("jdbc:mysql://127.0.0.1:3306/logic_db password=secret token=abc")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("message"), is("MCP query failed."));
        assertThat(actualRecovery.get("category"), is("query_failed"));
        assertFalse(String.valueOf(actual).contains("jdbc:mysql"));
        assertFalse(String.valueOf(actual).contains("password=secret"));
        assertFalse(String.valueOf(actual).contains("token=abc"));
    }
    
    static Stream<Arguments> assertConvertCases() {
        return Stream.of(
                Arguments.of("invalid request exception", new MCPInvalidRequestException("Invalid request."), "Invalid request."),
                Arguments.of("not found exception", new DatabaseCapabilityNotFoundException(), "Database capability does not exist."),
                Arguments.of("unsupported exception", new MCPUnsupportedException("Unsupported."), "Unsupported."),
                Arguments.of("transaction state exception", new MCPTransactionStateException("Transaction already active.", new IllegalStateException()), "Transaction already active."),
                Arguments.of("unavailable exception", new MCPUnavailableException("Unavailable."), "Unavailable."),
                Arguments.of("unsupported operation exception", new UnsupportedOperationException("Unsupported operation."), "Unsupported operation."),
                Arguments.of("illegal argument exception", new IllegalArgumentException("Illegal argument."), "Illegal argument."),
                Arguments.of("illegal state exception", new IllegalStateException(), "MCP operation failed."),
                Arguments.of("illegal state exception message", new IllegalStateException(" Operation state mismatch. "), "Operation state mismatch."),
                Arguments.of("unknown exception", new RuntimeException(), "Service is temporarily unavailable."));
    }
    
    static Stream<Arguments> assertConvertQueryFailureWithRecoveryCases() {
        return Stream.of(
                Arguments.of("sql syntax", new SQLSyntaxErrorException("Bad SQL.", "42601"), "Invalid request.", "sql_syntax_error", "ask_user"),
                Arguments.of("object not visible", new MCPQueryFailedException("Query failed.", new SQLException("Missing table.", "42P01")), "MCP query failed.", "object_not_visible",
                        "resource_read"),
                Arguments.of("insufficient privileges", new SQLException("Permission denied.", "42501"), "MCP query failed.", "insufficient_privileges", "ask_user"),
                Arguments.of("execution timeout", new MCPTimeoutException("Timed out.", new SQLTimeoutException("Timed out.")), "MCP operation timeout.", "execution_timeout",
                        "resource_read"),
                Arguments.of("connection interrupted", new MCPQueryFailedException("Query failed.", new SQLTransientConnectionException("Connection lost.", "08006")), "MCP query failed.",
                        "connection_interrupted", "resource_read"),
                Arguments.of("unsupported database capability", new SQLFeatureNotSupportedException("Unsupported feature."), "Unsupported MCP operation.",
                        "unsupported_database_capability", "resource_read"),
                Arguments.of("query failed", new MCPQueryFailedException("Query failed."), "MCP query failed.", "query_failed", "resource_read"));
    }
    
    private String getFirstResourceToReadUri(final Map<?, ?> recovery) {
        return (String) getFirstResourceToRead(recovery).get("uri");
    }
    
    private Map<?, ?> getFirstResourceToRead(final Map<?, ?> recovery) {
        return getResourceToRead(recovery, 0);
    }
    
    private Map<?, ?> getResourceToRead(final Map<?, ?> recovery, final int index) {
        return (Map<?, ?>) ((List<?>) recovery.get("resources_to_read")).get(index);
    }
}
