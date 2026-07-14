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

package org.apache.shardingsphere.mcp.support.database.tool.result;

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLExecutionResultTest {
    
    @Test
    void assertResultSet() {
        SQLExecutionColumnDefinition column = new SQLExecutionColumnDefinition("order_id", "INTEGER", "int4", false);
        SQLExecutionResult actual = SQLExecutionResult.resultSet(SupportedMCPStatement.QUERY, "SELECT", List.of(column), List.of(List.of(1)), true,
                10, 5000, "SELECT order_id FROM orders");
        assertThat(actual.getResultKind(), is(SQLExecutionResultKind.RESULT_SET));
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.QUERY));
        assertThat(actual.getStatementType(), is("SELECT"));
        assertThat(actual.getColumns(), is(List.of(column)));
        assertThat(actual.getRows(), is(List.of(List.of(1))));
        assertThat(actual.getAffectedRows(), is(0));
        assertTrue(actual.isTruncated());
        assertThat(actual.getAppliedMaxRows(), is(10));
        assertThat(actual.getAppliedTimeoutMs(), is(5000));
        assertThat(actual.getNormalizedSql(), is("SELECT order_id FROM orders"));
    }
    
    @Test
    void assertUpdateCount() {
        SQLExecutionResult actual = SQLExecutionResult.updateCount(SupportedMCPStatement.DML, "UPDATE", 2, 100, 3000,
                "UPDATE orders SET status = 'DONE'");
        assertThat(actual.getResultKind(), is(SQLExecutionResultKind.UPDATE_COUNT));
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.DML));
        assertThat(actual.getStatementType(), is("UPDATE"));
        assertThat(actual.getColumns(), is(List.of()));
        assertThat(actual.getRows(), is(List.of()));
        assertThat(actual.getAffectedRows(), is(2));
        assertFalse(actual.isTruncated());
        assertThat(actual.getAppliedMaxRows(), is(100));
        assertThat(actual.getAppliedTimeoutMs(), is(3000));
        assertThat(actual.getNormalizedSql(), is("UPDATE orders SET status = 'DONE'"));
    }
    
    @Test
    void assertStatementAck() {
        SQLExecutionResult actual = SQLExecutionResult.statementAck(SupportedMCPStatement.DDL, "CREATE", 100, 0, "CREATE TABLE orders (id INT)");
        assertThat(actual.getResultKind(), is(SQLExecutionResultKind.STATEMENT_ACK));
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.DDL));
        assertThat(actual.getStatementType(), is("CREATE"));
        assertThat(actual.getColumns(), is(List.of()));
        assertThat(actual.getRows(), is(List.of()));
        assertThat(actual.getAffectedRows(), is(0));
        assertFalse(actual.isTruncated());
        assertThat(actual.getAppliedMaxRows(), is(100));
        assertThat(actual.getAppliedTimeoutMs(), is(0));
        assertThat(actual.getNormalizedSql(), is("CREATE TABLE orders (id INT)"));
    }
}
