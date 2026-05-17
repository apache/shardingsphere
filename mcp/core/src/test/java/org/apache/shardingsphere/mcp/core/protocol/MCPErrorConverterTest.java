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
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUserApprovalRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ClassificationResult;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.MetadataIntrospectionSQLStatementException;
import org.apache.shardingsphere.mcp.core.tool.handler.execute.SQLToolMismatchException;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.database.exception.InvalidPageTokenException;
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
        Map<String, Object> actualPayload = actual.toPayload();
        assertThat(actualPayload.get("response_mode"), is("recovery"));
        assertThat(actualPayload.get("error_code"), is(expectedErrorCode));
        assertThat(actualPayload.get("message"), is(expectedMessage));
        assertTrue(String.valueOf(actualPayload.get("request_id")).matches("[0-9a-f\\-]{36}"));
        assertFalse(actualPayload.containsKey("recovery"));
    }
    
    @Test
    void assertConvertUnsupportedToolWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new UnsupportedToolException("missing_tool")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actualRecovery.get("category"), is("unsupported_tool"));
        assertThat(actualRecovery.get("recovery_category"), is("unsupported_target"));
        assertThat(actualRecovery.get("tool_name"), is("missing_tool"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("resource_uri"), is("shardingsphere://capabilities"));
        assertTrue(((Collection<?>) actualRecovery.get("supported_tools")).contains("database_gateway_execute_query"));
    }
    
    @Test
    void assertConvertUnsupportedResourceWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new UnsupportedResourceUriException("shardingsphere://unknown")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actualRecovery.get("category"), is("unsupported_resource"));
        assertThat(actualRecovery.get("recovery_category"), is("unsupported_target"));
        assertThat(((Map<?, ?>) actualRecovery.get("resource")).get("uri"), is("shardingsphere://unknown"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("type"), is("resource_read"));
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
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("resource_uri"), is("shardingsphere://databases"));
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
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_execute_update"));
        assertTrue((Boolean) actualRecovery.get("requires_user_approval"));
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
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_apply_workflow"));
        assertTrue((Boolean) actualRecovery.get("requires_user_approval"));
    }
    
    @Test
    void assertConvertInvalidWorkflowExecutionModeWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidExecutionModeException(
                "database_gateway_apply_workflow", List.of("preview", "review-then-execute", "manual-only"), Map.of())).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("recovery_category"), is("invalid_enum"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("preview", "review-then-execute", "manual-only")));
        assertFalse(actualRecovery.containsKey("suggested_next_tool"));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("execution_mode", "preview")));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("tool_name"), is("database_gateway_apply_workflow"));
    }
    
    @Test
    void assertConvertInvalidApprovedStepsWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidApprovedStepsException(List.of("ddl", "index_ddl", "rule_distsql"), Map.of())).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("recovery_category"), is("invalid_enum"));
        assertThat(actualRecovery.get("field"), is("approved_steps"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("ddl", "index_ddl", "rule_distsql")));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("tool_name"), is("database_gateway_apply_workflow"));
    }
    
    @Test
    void assertConvertUserApprovalRequiredWithRecovery() {
        Map<String, Object> suggestedArguments = Map.of("execution_mode", "execute", "approved_by_user", true);
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPUserApprovalRequiredException("database_gateway_execute_update", suggestedArguments)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("approval_required"));
        assertThat(actualRecovery.get("field"), is("approved_by_user"));
        assertThat(actualRecovery.get("missing_fields"), is(List.of("approved_by_user")));
        assertThat(actualRecovery.get("suggested_arguments"), is(suggestedArguments));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("type"), is("ask_user"));
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("required_inputs"), is(List.of("approved_by_user")));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("tool_name"), is("database_gateway_execute_update"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("depends_on"), is(List.of(1)));
        assertTrue((Boolean) actualRecovery.get("requires_user_approval"));
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
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("type"), is("ask_user"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("required_inputs"), is(List.of("algorithm_type")));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
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
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
    }
    
    @Test
    void assertConvertInvalidPageTokenWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new InvalidPageTokenException()).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("response_mode"), is("recovery"));
        assertThat(actualRecovery.get("category"), is("invalid_page_token"));
        assertThat(actualRecovery.get("field"), is("page_token"));
        assertThat(actualRecovery.get("argument_path"), is("page_token"));
        assertThat(actualRecovery.get("source_tool"), is("database_gateway_search_metadata"));
        assertThat(actualRecovery.get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(actualRecovery.get("minimum_value"), is(0));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("page_token", "")));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
        assertFalse((Boolean) actualRecovery.get("ask_user_when_uncertain"));
    }
    
    @Test
    void assertConvertInvalidIntegerArgumentWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidToolArgumentException("database_gateway_search_metadata", "database_gateway_search_metadata", "page_size", 1, 500, 50,
                new MCPInvalidRequestException("page_size must be an integer."))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("invalid_integer_argument"));
        assertThat(actualRecovery.get("field"), is("page_size"));
        assertThat(actualRecovery.get("argument_path"), is("page_size"));
        assertThat(actualRecovery.get("source_tool"), is("database_gateway_search_metadata"));
        assertThat(actualRecovery.get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(actualRecovery.get("minimum_value"), is(1));
        assertThat(actualRecovery.get("maximum_value"), is(500));
        assertThat(actualRecovery.get("suggested_value"), is(50));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("page_size", 50)));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
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
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_execute_query"));
    }
    
    @Test
    void assertConvertInvalidIntegerArgumentWithoutTargetAsksUser() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPInvalidToolArgumentException("", "", "timeout_ms", 0, 300000, 0,
                new MCPInvalidRequestException("timeout_ms must be an integer."))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
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
        assertThat(actual.get("error_code"), is("invalid_request"));
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("recovery_category"), is("invalid_enum"));
        assertThat(actualRecovery.get("field"), is("object_types[0]"));
        assertThat(actualRecovery.get("argument_path"), is("object_types[0]"));
        assertThat(actualRecovery.get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("database", "schema", "table")));
        assertThat(actualRecovery.get("suggested_arguments"), is(suggestedArguments));
        Map<?, ?> actualNextAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualNextAction.get("type"), is("tool_call"));
        assertThat(actualNextAction.get("tool_name"), is("database_gateway_search_metadata"));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
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
        assertThat(actual.get("error_code"), is("unsupported"));
        assertThat(actualRecovery.get("category"), is("unsupported_sql_statement"));
        assertThat(actualRecovery.get("recovery_category"), is("unsupported_target"));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertConvertBannedSQLWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPBannedSQLStatementException()).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("unsupported"));
        assertThat(actualRecovery.get("category"), is("banned_sql_statement"));
        assertThat(actualRecovery.get("recovery_category"), is("terminal_operator_action"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("type"), is("ask_user"));
    }
    
    @Test
    void assertConvertUnsupportedMessageWithoutRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPUnsupportedException(
                "database_gateway_execute_query only supports read-only QUERY and EXPLAIN_ANALYZE statements. Use database_gateway_execute_update for side-effecting SQL.")).toPayload();
        assertFalse(actual.containsKey("recovery"));
    }
    
    @Test
    void assertConvertSQLToolMismatchWithRecovery() {
        ClassificationResult classificationResult = new ClassificationResult(SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'PAID'", "orders", "");
        Map<String, Object> suggestedArguments = Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PAID'", "execution_mode", "preview");
        Map<String, Object> actual = MCPErrorConverter.convert(new SQLToolMismatchException(
                "database_gateway_execute_query only supports read-only QUERY and EXPLAIN_ANALYZE statements. Use database_gateway_execute_update for side-effecting SQL.",
                "database_gateway_execute_query", "database_gateway_execute_update", classificationResult, suggestedArguments)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("unsafe_sql_attempted"));
        assertThat(actualRecovery.get("recovery_category"), is("unsafe_sql"));
        assertThat(actualRecovery.get("source_tool"), is("database_gateway_execute_query"));
        assertFalse(actualRecovery.containsKey("suggested_next_tool"));
        assertThat(actualRecovery.get("normalized_sql"), is("UPDATE orders SET status = 'PAID'"));
        assertThat(actualRecovery.get("suggested_arguments"), is(suggestedArguments));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("arguments"), is(suggestedArguments));
        assertTrue((Boolean) actualRecovery.get("requires_user_approval"));
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
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("page_size", 50)));
        List<?> actualNextActions = (List<?>) actualRecovery.get("next_actions");
        assertThat(((Map<?, ?>) actualNextActions.get(0)).get("resource_uri"), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(((Map<?, ?>) actualNextActions.get(1)).get("depends_on"), is(List.of(1)));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
    }
    
    @Test
    void assertConvertWorkflowStateWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPWorkflowStateException(
                "Unknown or unavailable plan_id `plan-missing`. Call the planning tool again in the current MCP session.", "plan-missing")).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actualRecovery.get("category"), is("stale_workflow"));
        assertThat(actualRecovery.get("recovery_category"), is("stale_workflow"));
        assertThat(actualRecovery.get("plan_id"), is("plan-missing"));
        assertFalse(actualRecovery.containsKey("suggested_next_tools"));
        assertThat(actualRecovery.get("completion_first"), is(Map.of("argument", "plan_id", "scope", "current MCP session")));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        Map<?, ?> actualCompletionAction = (Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0);
        assertThat(actualCompletionAction.get("type"), is("completion"));
        assertThat(actualCompletionAction.get("reference_type"), is("ref/resource"));
        assertThat(actualCompletionAction.get("resume_target_type"), is("ref/resource"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(1)).get("type"), is("resource_read"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(1)).get("depends_on"), is(List.of(1)));
    }
    
    @Test
    void assertConvertRuntimeDatabaseConnectionWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new RuntimeDatabaseConnectionException("logic_db",
                RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED, new SQLException("Access denied."))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("unavailable"));
        assertThat(actualRecovery.get("category"), is("authentication_failed"));
        assertThat(actualRecovery.get("recovery_category"), is("unavailable_runtime"));
        assertThat(actualRecovery.get("database"), is("logic_db"));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(0)).get("order"), is(1));
        assertThat(((Map<?, ?>) ((List<?>) actualRecovery.get("next_actions")).get(1)).get("depends_on"), is(List.of(1)));
    }
    
    @Test
    void assertConvertToolCallLimitExceededWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(new MCPToolCallLimitExceededException("session-1", "database_gateway_search_metadata", 1)).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("rate_limited"));
        assertThat(actualRecovery.get("category"), is("tool_call_limit_exceeded"));
        assertThat(actualRecovery.get("identity_scope"), is("mcp_session"));
        assertThat(actualRecovery.get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(actualRecovery.get("max_tool_calls_per_session"), is(1));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertConvertInvalidRuntimeConfigurationWithRecovery() {
        Map<String, Object> actual = MCPErrorConverter.convert(RuntimeDatabaseConnectionException.invalidConfiguration("logic_db", new IllegalStateException("bad config"))).toPayload();
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertThat(actual.get("error_code"), is("unavailable"));
        assertThat(actual.get("message"), is("Runtime database `logic_db` connection failed: invalid_configuration."));
        assertThat(actualRecovery.get("category"), is("invalid_configuration"));
        assertThat(actualRecovery.get("recovery_category"), is("unavailable_runtime"));
        assertThat(actualRecovery.get("model_action"), is("Fix the MCP runtime database configuration outside MCP, then retry."));
        assertThat(getFirstResourceToReadUri(actualRecovery), is("shardingsphere://capabilities"));
        assertFalse((Boolean) actualRecovery.get("requires_user_approval"));
        assertTrue((Boolean) actualRecovery.get("ask_user_when_uncertain"));
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
