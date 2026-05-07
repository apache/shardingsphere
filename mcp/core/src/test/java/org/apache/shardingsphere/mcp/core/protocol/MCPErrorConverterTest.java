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

package org.apache.shardingsphere.mcp.core.protocol;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidToolArgumentException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArgumentConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
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
    void assertConvert(final String name, final Throwable cause, final String expectedErrorCode, final String expectedMessage) {
        MCPErrorResponse actual = MCPErrorConverter.convert(cause);
        assertThat(actual.toPayload(), is(Map.of("error_code", expectedErrorCode, "message", expectedMessage)));
    }

    @Test
    void assertConvertUnsupportedToolWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new UnsupportedToolException("missing_tool")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actualRecovery.get("category"), is("unsupported_tool"));
        assertThat(actualRecovery.get("tool_name"), is("missing_tool"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("target_resource"), is("shardingsphere://capabilities"));
        assertTrue(((Collection<?>) actualRecovery.get("supported_tools")).contains("execute_query"));
    }

    @Test
    void assertConvertUnsupportedResourceWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new UnsupportedResourceUriException("shardingsphere://unknown")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actualRecovery.get("category"), is("unsupported_resource"));
        assertThat(((Map<?, ?>) actualRecovery.get("resource")).get("uri"), is("shardingsphere://unknown"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("action_kind"), is("read_resource"));
        assertTrue(((Collection<?>) actualRecovery.get("matching_resource_templates")).contains("shardingsphere://capabilities"));
    }

    @Test
    void assertConvertMissingArgumentWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException("database is required.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("missing_database"));
        assertThat(actualRecovery.get("missing_fields"), is(List.of("database")));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("target_resource"), is("shardingsphere://databases"));
        assertTrue((Boolean) actualRecovery.get("ask_user_when_uncertain"));
    }

    @Test
    void assertConvertMissingExecutionModeWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException("execution_mode is required.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("missing_execution_mode"));
        assertThat(actualRecovery.get("missing_fields"), is(List.of("execution_mode")));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("action_kind"), is("retry_tool"));
        assertThat(actualNextAction.get("target_tool"), is("execute_update"));
        assertTrue((Boolean) actualRecovery.get("requires_user_approval"));
    }

    @Test
    void assertConvertMissingWorkflowExecutionModeWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException("apply_workflow execution_mode is required.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("missing_execution_mode"));
        assertThat(actualRecovery.get("field"), is("execution_mode"));
        assertThat(actualRecovery.get("source_tool"), is("apply_workflow"));
        assertThat(actualRecovery.get("target_tool"), is("apply_workflow"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("preview", "review-then-execute", "manual-only")));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("action_kind"), is("retry_tool"));
        assertThat(actualNextAction.get("target_tool"), is("apply_workflow"));
        assertTrue((Boolean) actualRecovery.get("requires_user_approval"));
    }

    @Test
    void assertConvertInvalidWorkflowExecutionModeWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(
                new MCPInvalidRequestException("execution_mode must be one of `preview`, `review-then-execute`, or `manual-only`.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("preview", "review-then-execute", "manual-only")));
        assertFalse(actualRecovery.containsKey("suggested_next_tool"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("target_tool"), is("apply_workflow"));
    }

    @Test
    void assertConvertInvalidApprovedStepsWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(
                new MCPInvalidRequestException("approved_steps must contain only `ddl`, `index_ddl`, or `rule_distsql`.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("field"), is("approved_steps"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("ddl", "index_ddl", "rule_distsql")));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("target_tool"), is("apply_workflow"));
    }

    @Test
    void assertConvertWorkflowArgumentConflictWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new WorkflowArgumentConflictException(
                List.of("algorithm_type conflicts with user_overrides.algorithm_type"))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("workflow_argument_conflict"));
        assertThat(actualRecovery.get("conflicting_arguments"), is(List.of("algorithm_type conflicts with user_overrides.algorithm_type")));
        Map<?, ?> actualClarificationQuestion = (Map<?, ?>) ((List<?>) actualRecovery.get("clarification_questions")).get(0);
        assertThat(actualClarificationQuestion.get("field"), is("algorithm_type"));
        assertThat(actualClarificationQuestion.get("conflict"), is("algorithm_type conflicts with user_overrides.algorithm_type"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("action_kind"), is("ask_user"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("required_inputs"), is(List.of("algorithm_type")));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
    }

    @Test
    void assertConvertInvalidObjectTypesWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException("Unsupported object_types value `unknown`.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("field"), is("object_types"));
        assertTrue(((Collection<?>) actualRecovery.get("allowed_values")).contains("table"));
        assertTrue(((Collection<?>) ((Map<?, ?>) actualRecovery.get("suggested_arguments")).get("object_types")).contains("table"));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("action_kind"), is("retry_tool"));
        assertThat(actualNextAction.get("target_tool"), is("search_metadata"));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
    }

    @Test
    void assertConvertInvalidPageTokenWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException("Invalid page token.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("response_mode"), is("recovery"));
        assertThat(actualRecovery.get("category"), is("invalid_page_token"));
        assertThat(actualRecovery.get("field"), is("page_token"));
        assertThat(actualRecovery.get("argument_path"), is("page_token"));
        assertThat(actualRecovery.get("source_tool"), is("search_metadata"));
        assertThat(actualRecovery.get("target_tool"), is("search_metadata"));
        assertThat(actualRecovery.get("minimum_value"), is(0));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("page_token", "")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("action_kind"), is("retry_tool"));
        assertThat(actualNextAction.get("target_tool"), is("search_metadata"));
        assertFalse((Boolean) actualRecovery.get("ask_user_when_uncertain"));
    }

    @Test
    void assertConvertInvalidIntegerArgumentWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException("page_size must be an integer between 1 and 500.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_integer_argument"));
        assertThat(actualRecovery.get("field"), is("page_size"));
        assertThat(actualRecovery.get("argument_path"), is("page_size"));
        assertThat(actualRecovery.get("source_tool"), is("search_metadata"));
        assertThat(actualRecovery.get("target_tool"), is("search_metadata"));
        assertThat(actualRecovery.get("minimum_value"), is(1));
        assertThat(actualRecovery.get("maximum_value"), is(500));
        assertThat(actualRecovery.get("suggested_value"), is(100));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("page_size", 100)));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("action_kind"), is("retry_tool"));
        assertThat(actualNextAction.get("target_tool"), is("search_metadata"));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
    }

    @Test
    void assertConvertInvalidSQLIntegerArgumentWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidToolArgumentException("execute_query", "execute_query", "max_rows", 0, 5000, 100,
                new MCPInvalidRequestException("max_rows must be an integer."))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_integer_argument"));
        assertThat(actualRecovery.get("argument_path"), is("max_rows"));
        assertThat(actualRecovery.get("source_tool"), is("execute_query"));
        assertThat(actualRecovery.get("target_tool"), is("execute_query"));
        assertThat(actualRecovery.get("minimum_value"), is(0));
        assertThat(actualRecovery.get("maximum_value"), is(5000));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("max_rows", 100)));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("target_tool"), is("execute_query"));
    }

    @Test
    void assertConvertInvalidIntegerArgumentWithoutTargetAsksUser() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException("timeout_ms must be an integer between 0 and 300000.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualRecovery.get("argument_path"), is("timeout_ms"));
        assertThat(actualNextAction.get("action_kind"), is("ask_user"));
        assertFalse(actualNextAction.containsKey("target_tool"));
    }

    @Test
    void assertConvertMultipleStatementsWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException("Only one SQL statement is allowed.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("multiple_sql_statements"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        assertTrue((Boolean) actualRecovery.get("ask_user_when_uncertain"));
    }

    @Test
    void assertConvertUnsafeQueryWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPUnsupportedException(
                "execute_query only supports read-only QUERY and EXPLAIN_ANALYZE statements. Use execute_update for side-effecting SQL.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("unsafe_sql_attempted"));
        assertFalse(actualRecovery.containsKey("suggested_next_tool"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("target_tool"), is("execute_update"));
        assertTrue((Boolean) actualRecovery.get("requires_user_approval"));
    }

    @Test
    void assertConvertSQLToolMismatchWithRecovery() {
        ClassificationResult classificationResult = new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'PAID'", "orders", "");
        Map<String, Object> suggestedArguments = Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID'", "execution_mode", "preview");
        Map<String, Object> actual = MCPErrorConverter.convert(new SQLToolMismatchException(
                "execute_query only supports read-only QUERY and EXPLAIN_ANALYZE statements. Use execute_update for side-effecting SQL.",
                "execute_query", "execute_update", classificationResult, suggestedArguments)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("unsafe_sql_attempted"));
        assertThat(actualRecovery.get("source_tool"), is("execute_query"));
        assertFalse(actualRecovery.containsKey("suggested_next_tool"));
        assertThat(actualRecovery.get("normalized_sql"), is("UPDATE orders SET status = 'PAID'"));
        assertThat(actualRecovery.get("suggested_arguments"), is(suggestedArguments));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("required_arguments"), is(suggestedArguments));
        assertTrue((Boolean) actualRecovery.get("requires_user_approval"));
    }

    @Test
    void assertConvertReadOnlySQLToolMismatchWithRecovery() {
        ClassificationResult classificationResult = new ClassificationResult(SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM orders", "orders", "");
        Map<String, Object> suggestedArguments = Map.of("database", "logic_db", "sql", "SELECT * FROM orders");
        Map<String, Object> actual = MCPErrorConverter.convert(new SQLToolMismatchException(
                "execute_update does not accept read-only SQL. Use execute_query for read-only SQL.",
                "execute_update", "execute_query", classificationResult, suggestedArguments)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("read_only_sql_sent_to_update_tool"));
        assertFalse(actualRecovery.containsKey("suggested_next_tool"));
        assertThat(actualRecovery.get("normalized_sql"), is("SELECT * FROM orders"));
        assertThat(actualRecovery.get("suggested_arguments"), is(suggestedArguments));
        assertFalse((Boolean) ((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("requires_user_approval"));
    }

    @Test
    void assertConvertMetadataIntrospectionSQLWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MetadataIntrospectionSQLStatementException("SHOW")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("metadata_introspection_sql"));
        assertThat(actualRecovery.get("statement_type"), is("SHOW"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://databases"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("page_size", 100)));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("target_resource"), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("target_tool"), is("search_metadata"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("depends_on"), is(List.of(1)));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
    }

    @Test
    void assertConvertWorkflowStateWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidRequestException(
                "Unknown or unavailable plan_id `plan-missing`. Call the planning tool again in the current MCP session.")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("workflow_state_error"));
        assertFalse(actualRecovery.containsKey("suggested_next_tools"));
        assertThat(actualRecovery.get("completion_first"), is(Map.of("argument", "plan_id", "scope", "current MCP session")));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("action_kind"), is("complete_argument"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(1)).get("action_kind"), is("read_resource"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(1)).get("depends_on"), is(List.of(1)));
    }

    @Test
    void assertConvertRuntimeDatabaseConnectionWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new RuntimeDatabaseConnectionException("logic_db", "authentication_failed", new SQLException("Access denied."))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("unavailable"));
        assertThat(actualRecovery.get("category"), is("authentication_failed"));
        assertThat(actualRecovery.get("database"), is("logic_db"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("order"), is(1));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(1)).get("depends_on"), is(List.of(1)));
    }

    static Stream<Arguments> assertConvertCases() {
        return Stream.of(
                Arguments.of("invalid request exception", new MCPInvalidRequestException("Invalid request."), "invalid_request", "Invalid request."),
                Arguments.of("not found exception", new DatabaseCapabilityNotFoundException(), "not_found", "Database capability does not exist."),
                Arguments.of("unsupported exception", new MCPUnsupportedException("Unsupported."), "unsupported", "Unsupported."),
                Arguments.of("timeout exception", new MCPTimeoutException("Timed out.", new SQLTimeoutException("Timed out.")), "timeout", "Timed out."),
                Arguments.of("transaction state exception", new MCPTransactionStateException("Transaction already active.", new IllegalStateException()), "transaction_state_error",
                        "Transaction already active."),
                Arguments.of("query failed exception", new MCPQueryFailedException("Query failed."), "query_failed", "Query failed."),
                Arguments.of("unavailable exception", new MCPUnavailableException("Unavailable."), "unavailable", "Unavailable."),
                Arguments.of("sql syntax exception", new SQLSyntaxErrorException("Bad SQL."), "invalid_request", "Bad SQL."),
                Arguments.of("sql timeout exception", new SQLTimeoutException("Timed out."), "timeout", "Timed out."),
                Arguments.of("sql unsupported feature exception", new SQLFeatureNotSupportedException("Unsupported feature."), "unsupported", "Unsupported feature."),
                Arguments.of("unsupported operation exception", new UnsupportedOperationException("Unsupported operation."), "unsupported", "Unsupported operation."),
                Arguments.of("sql exception", new SQLException("Query failed."), "query_failed", "Query failed."),
                Arguments.of("illegal argument exception", new IllegalArgumentException("Illegal argument."), "invalid_request", "Illegal argument."),
                Arguments.of("illegal state exception", new IllegalStateException(" Transaction already active. "), "transaction_state_error", "Transaction already active."),
                Arguments.of("unknown exception", new RuntimeException(), "unavailable", "Service is temporarily unavailable."));
    }

    private String getFirstResourceToReadUri(final Map<?, ?> recovery) {
        return (String) ((Map<?, ?>) ((List<?>) recovery.get("resources_to_read")).get(0)).get("uri");
    }
}
