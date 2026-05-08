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

package org.apache.shardingsphere.mcp.support.database.tool.response;

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.protocol.ExecuteQueryColumnDefinition;
import org.apache.shardingsphere.mcp.support.database.protocol.ExecuteQueryResultKind;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLExecutionResponseTest {
    
    @Test
    void assertResultSet() {
        SQLExecutionResponse actual = SQLExecutionResponse.resultSet(List.of(new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false)), List.of(List.of(1)), true);
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.QUERY));
        assertThat(actual.getColumns().size(), is(1));
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getAffectedRows(), is(0));
        assertThat(actual.getStatementType(), is("SELECT"));
        assertThat(actual.getStatus(), is("OK"));
        assertThat(actual.getMessage(), is(""));
        assertTrue(actual.isTruncated());
        assertThat(actual.getAppliedMaxRows(), is(0));
        assertThat(actual.getAppliedTimeoutMs(), is(0));
    }
    
    @Test
    void assertUpdateCount() {
        SQLExecutionResponse actual = SQLExecutionResponse.updateCount("UPDATE", 2);
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.DML));
        assertThat(actual.getStatementType(), is("UPDATE"));
        assertThat(actual.getAffectedRows(), is(2));
        assertThat(actual.getStatus(), is("OK"));
        assertThat(actual.getColumns(), is(List.of()));
        assertThat(actual.getRows(), is(List.of()));
        assertThat(actual.getMessage(), is(""));
        assertFalse(actual.isTruncated());
    }
    
    @Test
    void assertStatementAck() {
        SQLExecutionResponse actual = SQLExecutionResponse.statementAck(SupportedMCPStatement.TRANSACTION_CONTROL, "COMMIT", "Transaction committed.");
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.TRANSACTION_CONTROL));
        assertThat(actual.getStatementType(), is("COMMIT"));
        assertThat(actual.getAffectedRows(), is(0));
        assertThat(actual.getStatus(), is("OK"));
        assertThat(actual.getMessage(), is("Transaction committed."));
        assertFalse(actual.isTruncated());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertToPayloadCases")
    void assertToPayload(final String name, final Supplier<SQLExecutionResponse> responseSupplier, final Map<String, Object> expectedPayload) {
        SQLExecutionResponse actual = responseSupplier.get();
        assertThat(actual.toPayload(), is(expectedPayload));
    }
    
    @Test
    void assertResultSetWithNullRows() {
        assertThat(SQLExecutionResponse.resultSet(List.of(), null, false).getRows(), is(List.of()));
    }
    
    @Test
    void assertToPayloadWithUnnamedColumns() {
        Map<String, Object> actual = SQLExecutionResponse.resultSet(List.of(), List.of(List.of(1)), false).toPayload();
        assertThat(actual.get("row_object_status"), is("unnamed_columns"));
        assertFalse(actual.containsKey("row_objects"));
    }
    
    @Test
    void assertToPayloadWithBlankColumnName() {
        Map<String, Object> actual = SQLExecutionResponse.resultSet(List.of(new ExecuteQueryColumnDefinition("", "INT", "INT", false)), List.of(List.of(1)), false).toPayload();
        assertThat(actual.get("row_object_status"), is("unnamed_columns"));
        assertFalse(actual.containsKey("row_objects"));
    }
    
    @Test
    void assertToPayloadWithDuplicateColumnLabels() {
        List<ExecuteQueryColumnDefinition> columns = List.of(
                new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false), new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false));
        Map<String, Object> actual = SQLExecutionResponse.resultSet(columns, List.of(List.of(1, 2)), false).toPayload();
        assertThat(actual.get("row_object_status"), is("duplicate_column_labels"));
        assertFalse(actual.containsKey("row_objects"));
    }
    
    @Test
    void assertToPayloadOmitsLargeRowObjects() {
        List<ExecuteQueryColumnDefinition> columns = List.of(new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false));
        List<List<Object>> rows = new LinkedList<>();
        for (int i = 0; i < 101; i++) {
            rows.add(List.of(i));
        }
        Map<String, Object> actual = SQLExecutionResponse.resultSet(columns, rows, false).toPayload();
        assertThat(actual.get("row_object_status"), is("omitted_large_result"));
        assertFalse(actual.containsKey("row_objects"));
    }
    
    @Test
    void assertResultSetWithDmlStatementClass() {
        SQLExecutionResponse actual = SQLExecutionResponse.resultSet(SupportedMCPStatement.DML, "SELECT", List.of(), List.of(List.of(1)), false);
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.DML));
        assertThat(actual.getStatementType(), is("SELECT"));
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
    }
    
    @Test
    void assertUpdateCountWithZeroAffectedRows() {
        SQLExecutionResponse actual = SQLExecutionResponse.updateCount("UPDATE", 0);
        assertThat(actual.toPayload().get("affected_rows"), is(0));
    }
    
    @Test
    void assertWithNormalizedSql() {
        SQLExecutionResponse actual = SQLExecutionResponse.updateCount("UPDATE", 1).withNormalizedSql("UPDATE orders SET status = 'DONE'");
        assertThat(actual.getNormalizedSql(), is("UPDATE orders SET status = 'DONE'"));
        assertThat(actual.toPayload().get("normalized_sql"), is("UPDATE orders SET status = 'DONE'"));
    }
    
    @Test
    void assertWithExecutionMode() {
        SQLExecutionResponse actual = SQLExecutionResponse.updateCount("UPDATE", 1).withExecutionMode("execute");
        assertThat(actual.getResponseMode(), is("executed"));
        assertThat(actual.getExecutionMode(), is("execute"));
        assertThat(actual.toPayload().get("response_mode"), is("executed"));
        assertThat(actual.toPayload().get("execution_mode"), is("execute"));
    }
    
    private static Stream<Arguments> assertToPayloadCases() {
        ExecuteQueryColumnDefinition orderIdColumn = new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false);
        List<ExecuteQueryColumnDefinition> columns = List.of(orderIdColumn);
        List<List<Object>> rows = List.of(List.of(1));
        List<Map<String, Object>> rowObjects = List.of(Map.of("order_id", 1));
        List<Map<String, Object>> truncatedResultSetNextActions = createTruncatedResultSetNextActions();
        List<Map<String, Object>> resultSetNextActions = createNextActions("Return the result rows to the user or ask a follow-up question if the user requested more analysis.");
        List<Map<String, Object>> executionNextActions = createNextActions("Report the execution status to the user and stop unless the user asks for another operation.");
        return Stream.of(
                Arguments.of("result set with rows", (Supplier<SQLExecutionResponse>) () -> SQLExecutionResponse.resultSet(columns, rows, true).withExecutionHints(10, 5000),
                        Map.ofEntries(
                                Map.entry("result_kind", "result_set"), Map.entry("statement_class", "query"), Map.entry("statement_type", "SELECT"), Map.entry("status", "OK"),
                                Map.entry("response_mode", "query"),
                                Map.entry("columns", columns), Map.entry("rows", rows), Map.entry("row_object_status", "available"), Map.entry("row_objects", rowObjects),
                                Map.entry("returned_row_count", 1), Map.entry("applied_max_rows", 10), Map.entry("applied_timeout_ms", 5000), Map.entry("truncated", true),
                                Map.entry("next_actions", truncatedResultSetNextActions))),
                Arguments.of("result set with null rows", (Supplier<SQLExecutionResponse>) () -> SQLExecutionResponse.resultSet(List.of(), null, false),
                        Map.ofEntries(
                                Map.entry("result_kind", "result_set"), Map.entry("statement_class", "query"), Map.entry("statement_type", "SELECT"), Map.entry("status", "OK"),
                                Map.entry("response_mode", "query"),
                                Map.entry("columns", List.of()), Map.entry("rows", List.of()), Map.entry("row_object_status", "available"), Map.entry("row_objects", List.of()),
                                Map.entry("returned_row_count", 0), Map.entry("applied_max_rows", 0), Map.entry("applied_timeout_ms", 0), Map.entry("truncated", false),
                                Map.entry("next_actions", resultSetNextActions))),
                Arguments.of("update count", (Supplier<SQLExecutionResponse>) () -> SQLExecutionResponse.updateCount("UPDATE", 2),
                        Map.ofEntries(Map.entry("response_mode", "executed"), Map.entry("result_kind", "update_count"), Map.entry("statement_class", "dml"),
                                Map.entry("statement_type", "UPDATE"), Map.entry("status", "OK"), Map.entry("affected_rows", 2), Map.entry("truncated", false),
                                Map.entry("applied_max_rows", 0), Map.entry("applied_timeout_ms", 0), Map.entry("next_actions", executionNextActions))),
                Arguments.of("statement acknowledgement",
                        (Supplier<SQLExecutionResponse>) () -> SQLExecutionResponse.statementAck(SupportedMCPStatement.TRANSACTION_CONTROL, "COMMIT", "Transaction committed."),
                        Map.ofEntries(Map.entry("response_mode", "executed"), Map.entry("result_kind", "statement_ack"), Map.entry("statement_class", "transaction_control"),
                                Map.entry("statement_type", "COMMIT"), Map.entry("status", "OK"), Map.entry("message", "Transaction committed."), Map.entry("truncated", false),
                                Map.entry("applied_max_rows", 0), Map.entry("applied_timeout_ms", 0), Map.entry("next_actions", executionNextActions))));
    }
    
    private static List<Map<String, Object>> createNextActions(final String reason) {
        return List.of(MCPNextActionUtils.stop(reason));
    }
    
    private static List<Map<String, Object>> createTruncatedResultSetNextActions() {
        return List.of(MCPNextActionUtils.askUser("The result was truncated by max_rows. Ask for a narrower SELECT, stronger WHERE clause, or smaller projection before retrying.",
                List.of("sql"), false));
    }
}
