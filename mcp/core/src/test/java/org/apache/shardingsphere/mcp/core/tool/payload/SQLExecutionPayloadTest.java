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

package org.apache.shardingsphere.mcp.core.tool.payload;

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionColumnDefinition;
import org.apache.shardingsphere.mcp.support.database.tool.result.SQLExecutionResult;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SQLExecutionPayloadTest {
    
    @Test
    void assertQueryResultSet() {
        SQLExecutionColumnDefinition column = new SQLExecutionColumnDefinition("order_id", "INT", "INT", false);
        Map<String, Object> actual = SQLExecutionPayload.query(SQLExecutionResult.resultSet(
                SupportedMCPStatement.QUERY, "SELECT", List.of(column), List.of(List.of(1)), false, 10, 5000, "SELECT order_id FROM orders")).toPayload();
        assertThat(actual, is(Map.ofEntries(
                Map.entry("response_mode", "query"), Map.entry("result_kind", "result_set"), Map.entry("statement_class", "query"), Map.entry("statement_type", "SELECT"),
                Map.entry("status", "OK"), Map.entry("summary", "Executed SELECT statement and returned 1 row(s)."),
                Map.entry("normalized_sql", "SELECT order_id FROM orders"), Map.entry("columns", List.of(column)), Map.entry("rows", List.of(List.of(1))),
                Map.entry("row_object_status", "available"), Map.entry("row_objects", List.of(Map.of("order_id", 1))), Map.entry("returned_row_count", 1),
                Map.entry("applied_max_rows", 10), Map.entry("applied_timeout_ms", 5000), Map.entry("truncated", false),
                Map.entry("next_actions", List.of(MCPNextActionUtils.stop(
                        "Return the result rows to the user or ask a follow-up question if the user requested more analysis."))))));
    }
    
    @Test
    void assertTruncatedQueryResultSet() {
        Map<String, Object> actual = SQLExecutionPayload.query(SQLExecutionResult.resultSet(
                SupportedMCPStatement.QUERY, "SELECT", List.of(), List.of(), true, 10, 0, "SELECT * FROM orders")).toPayload();
        assertThat(actual.get("summary"), is("Executed SELECT statement and returned 0 row(s). Result was truncated."));
        assertThat(actual.get("next_actions"), is(List.of(MCPNextActionUtils.askUser(
                "The result was truncated by max_rows. Ask for a narrower SELECT, stronger WHERE clause, or smaller projection before retrying.", List.of("sql")))));
    }
    
    @Test
    void assertExecutedUpdateCount() {
        Map<String, Object> actual = SQLExecutionPayload.executed(SQLExecutionResult.updateCount(
                SupportedMCPStatement.DML, "UPDATE", 2, 100, 0, "UPDATE orders SET status = 'PAID'")).toPayload();
        assertThat(actual.get("response_mode"), is("executed"));
        assertThat(actual.get("execution_mode"), is("execute"));
        assertThat(actual.get("result_kind"), is("update_count"));
        assertThat(actual.get("affected_rows"), is(2));
        assertThat(actual.get("summary"), is("Executed UPDATE statement and affected 2 row(s)."));
    }
    
    @Test
    void assertExecutedResultSet() {
        Map<String, Object> actual = SQLExecutionPayload.executed(SQLExecutionResult.resultSet(
                SupportedMCPStatement.DML, "SELECT", List.of(), List.of(List.of(1)), false, 100, 0, "WITH changed AS (...) SELECT * FROM changed")).toPayload();
        assertThat(actual.get("summary"), is("Executed side-effecting SQL (statement type SELECT) and returned 1 row(s)."));
        assertThat(actual.get("response_mode"), is("executed"));
        assertThat(actual.get("execution_mode"), is("execute"));
    }
    
    @Test
    void assertTruncatedExecutedResultSet() {
        Map<String, Object> actual = SQLExecutionPayload.executed(SQLExecutionResult.resultSet(
                SupportedMCPStatement.DML, "SELECT", List.of(), List.of(List.of(1)), true, 1, 0, "WITH changed AS (...) SELECT * FROM changed")).toPayload();
        assertThat(actual.get("summary"), is("Executed side-effecting SQL (statement type SELECT) and returned 1 row(s). "
                + "Returned rows were truncated; do not replay the statement automatically."));
        assertThat(actual.get("next_actions"), is(List.of(MCPNextActionUtils.stop(
                "The side-effecting statement already executed and returned truncated rows; do not replay it automatically. "
                        + "Use a separate read-only query if more data is needed."))));
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "TRANSACTION_CONTROL, BEGIN, Transaction started.",
            "TRANSACTION_CONTROL, START TRANSACTION, Transaction started.",
            "TRANSACTION_CONTROL, COMMIT, Transaction committed.",
            "TRANSACTION_CONTROL, ROLLBACK, Transaction rolled back.",
            "SAVEPOINT, SAVEPOINT, Savepoint created.",
            "SAVEPOINT, ROLLBACK TO SAVEPOINT, Savepoint rolled back.",
            "SAVEPOINT, RELEASE SAVEPOINT, Savepoint released.",
            "DDL, CREATE, Statement executed."
    })
    void assertStatementAcknowledgement(final SupportedMCPStatement statementClass, final String statementType, final String expectedSummary) {
        Map<String, Object> actual = SQLExecutionPayload.executed(SQLExecutionResult.statementAck(
                statementClass, statementType, 100, 0, statementType)).toPayload();
        assertThat(actual.get("summary"), is(expectedSummary));
        assertFalse(actual.containsKey("message"));
    }
    
    @Test
    void assertUnnamedColumns() {
        Map<String, Object> actual = SQLExecutionPayload.query(SQLExecutionResult.resultSet(
                SupportedMCPStatement.QUERY, "SELECT", List.of(), List.of(List.of(1)), false, 100, 0, "SELECT 1")).toPayload();
        assertThat(actual.get("row_object_status"), is("unnamed_columns"));
        assertFalse(actual.containsKey("row_objects"));
    }
    
    @Test
    void assertBlankColumnName() {
        Map<String, Object> actual = SQLExecutionPayload.query(SQLExecutionResult.resultSet(SupportedMCPStatement.QUERY, "SELECT",
                List.of(new SQLExecutionColumnDefinition("", "INT", "INT", false)), List.of(List.of(1)), false, 100, 0, "SELECT 1")).toPayload();
        assertThat(actual.get("row_object_status"), is("unnamed_columns"));
        assertFalse(actual.containsKey("row_objects"));
    }
    
    @Test
    void assertDuplicateColumnLabels() {
        List<SQLExecutionColumnDefinition> columns = List.of(
                new SQLExecutionColumnDefinition("order_id", "INT", "INT", false), new SQLExecutionColumnDefinition("order_id", "INT", "INT", false));
        Map<String, Object> actual = SQLExecutionPayload.query(SQLExecutionResult.resultSet(
                SupportedMCPStatement.QUERY, "SELECT", columns, List.of(List.of(1, 2)), false, 100, 0, "SELECT order_id, order_id FROM orders")).toPayload();
        assertThat(actual.get("row_object_status"), is("duplicate_column_labels"));
        assertFalse(actual.containsKey("row_objects"));
    }
    
    @Test
    void assertLargeRowObjectsOmitted() {
        List<List<Object>> rows = new LinkedList<>();
        for (int i = 0; i < 101; i++) {
            rows.add(List.of(i));
        }
        Map<String, Object> actual = SQLExecutionPayload.query(SQLExecutionResult.resultSet(SupportedMCPStatement.QUERY, "SELECT",
                List.of(new SQLExecutionColumnDefinition("order_id", "INT", "INT", false)), rows, false, 100, 0, "SELECT order_id FROM orders")).toPayload();
        assertThat(actual.get("row_object_status"), is("omitted_large_result"));
        assertFalse(actual.containsKey("row_objects"));
    }
}
