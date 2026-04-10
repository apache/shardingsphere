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

package org.apache.shardingsphere.mcp.protocol;

import org.apache.shardingsphere.mcp.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLExecutionResponseTest {
    
    @Test
    void assertResultSet() {
        SQLExecutionResponse actual = SQLExecutionResponse.resultSet(List.of(new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false)), List.of(List.of(1)), true);
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getColumns().size(), is(1));
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getAffectedRows(), is(0));
        assertThat(actual.getStatementType(), is("QUERY"));
        assertThat(actual.getStatus(), is("OK"));
        assertThat(actual.getMessage(), is(""));
        assertTrue(actual.isTruncated());
    }
    
    @Test
    void assertUpdateCount() {
        SQLExecutionResponse actual = SQLExecutionResponse.updateCount("UPDATE", 2);
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
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
        SQLExecutionResponse actual = SQLExecutionResponse.statementAck("COMMIT", "Transaction committed.");
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
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
        assertNull(SQLExecutionResponse.resultSet(List.of(), null, false).getRows());
    }
    
    private static Stream<Arguments> assertToPayloadCases() {
        ExecuteQueryColumnDefinition orderIdColumn = new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false);
        List<ExecuteQueryColumnDefinition> columns = List.of(orderIdColumn);
        List<List<Object>> rows = List.of(List.of(1));
        return Stream.of(
                Arguments.of("result set with rows", (Supplier<SQLExecutionResponse>) () -> SQLExecutionResponse.resultSet(columns, rows, true),
                        Map.of("result_kind", "result_set", "statement_type", "QUERY", "status", "OK", "columns", columns, "rows", rows, "truncated", true)),
                Arguments.of("result set with null rows", (Supplier<SQLExecutionResponse>) () -> SQLExecutionResponse.resultSet(List.of(), null, false),
                        Map.of("result_kind", "result_set", "statement_type", "QUERY", "status", "OK", "truncated", false)),
                Arguments.of("update count", (Supplier<SQLExecutionResponse>) () -> SQLExecutionResponse.updateCount("UPDATE", 2),
                        Map.of("result_kind", "update_count", "statement_type", "UPDATE", "status", "OK", "affected_rows", 2, "truncated", false)),
                Arguments.of("statement acknowledgement", (Supplier<SQLExecutionResponse>) () -> SQLExecutionResponse.statementAck("COMMIT", "Transaction committed."),
                        Map.of("result_kind", "statement_ack", "statement_type", "COMMIT", "status", "OK", "message", "Transaction committed.", "truncated", false)));
    }
}
