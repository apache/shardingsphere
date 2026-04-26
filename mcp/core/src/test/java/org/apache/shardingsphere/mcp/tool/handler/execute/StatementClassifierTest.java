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

package org.apache.shardingsphere.mcp.tool.handler.execute;

import org.apache.shardingsphere.mcp.capability.SupportedMCPStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatementClassifierTest {
    
    private final StatementClassifier statementClassifier = new StatementClassifier();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyCases")
    void assertClassify(final String name, final String sql, final SupportedMCPStatement expectedStatementClass, final String expectedStatementType,
                        final String expectedNormalizedSql, final String expectedTargetObjectName, final String expectedSavepointName) {
        ClassificationResult actualResult = statementClassifier.classify(sql);
        assertThat(actualResult.getStatementClass(), is(expectedStatementClass));
        assertThat(actualResult.getStatementType(), is(expectedStatementType));
        assertThat(actualResult.getNormalizedSql(), is(expectedNormalizedSql));
        assertThat(actualResult.getTargetObjectName().orElse(""), is(expectedTargetObjectName));
        assertThat(actualResult.getSavepointName().orElse(""), is(expectedSavepointName));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyWithInvalidStatementCases")
    void assertClassifyWithInvalidStatement(final String name, final String sql, final Class<? extends RuntimeException> expectedExceptionClass, final String expectedMessage) {
        RuntimeException actualException = assertThrows(expectedExceptionClass, () -> statementClassifier.classify(sql));
        assertThat(actualException.getMessage(), is(expectedMessage));
    }
    
    private static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("trim trailing semicolon query", "  SELECT * FROM foo_orders ;  ", SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM foo_orders", "foo_orders", ""),
                Arguments.of("query with semicolon literal", "SELECT ';' AS foo_literal", SupportedMCPStatement.QUERY, "SELECT", "SELECT ';' AS foo_literal", "", ""),
                Arguments.of("query with semicolon block comment", "SELECT * FROM foo_orders /* source; */", SupportedMCPStatement.QUERY, "SELECT",
                        "SELECT * FROM foo_orders /* source; */", "foo_orders", ""),
                Arguments.of("trim trailing semicolon query with comment", "SELECT * FROM foo_orders; -- trailing;", SupportedMCPStatement.QUERY, "SELECT",
                        "SELECT * FROM foo_orders", "foo_orders", ""),
                Arguments.of("with query", "WITH foo_result AS (SELECT * FROM foo_orders) SELECT * FROM foo_result", SupportedMCPStatement.QUERY, "SELECT",
                        "WITH foo_result AS (SELECT * FROM foo_orders) SELECT * FROM foo_result", "foo_orders", ""),
                Arguments.of("with recursive query", "WITH RECURSIVE foo_result AS (SELECT * FROM foo_orders) SELECT * FROM foo_result", SupportedMCPStatement.QUERY, "SELECT",
                        "WITH RECURSIVE foo_result AS (SELECT * FROM foo_orders) SELECT * FROM foo_result", "foo_orders", ""),
                Arguments.of("with quoted alias query", "WITH \"foo_result\" AS (SELECT * FROM \"foo_orders\") SELECT * FROM \"foo_result\"", SupportedMCPStatement.QUERY, "SELECT",
                        "WITH \"foo_result\" AS (SELECT * FROM \"foo_orders\") SELECT * FROM \"foo_result\"", "foo_orders", ""),
                Arguments.of("with comment query", "WITH foo_result AS (SELECT * FROM foo_orders /* source */) SELECT * FROM foo_result", SupportedMCPStatement.QUERY, "SELECT",
                        "WITH foo_result AS (SELECT * FROM foo_orders /* source */) SELECT * FROM foo_result", "foo_orders", ""),
                Arguments.of("with update dml", "WITH foo_result AS (SELECT * FROM foo_orders) UPDATE bar_orders SET status = 'DONE' FROM foo_result WHERE bar_orders.order_id = foo_result.order_id",
                        SupportedMCPStatement.DML, "UPDATE",
                        "WITH foo_result AS (SELECT * FROM foo_orders) UPDATE bar_orders SET status = 'DONE' FROM foo_result WHERE bar_orders.order_id = foo_result.order_id",
                        "bar_orders", ""),
                Arguments.of("with merge dml", "WITH foo_result AS (SELECT * FROM foo_orders) MERGE INTO bar_orders_archive USING foo_result ON 1 = 1",
                        SupportedMCPStatement.DML, "MERGE",
                        "WITH foo_result AS (SELECT * FROM foo_orders) MERGE INTO bar_orders_archive USING foo_result ON 1 = 1", "bar_orders_archive", ""),
                Arguments.of("data modifying cte select", "WITH updated_orders AS (UPDATE foo_orders SET status = 'DONE' RETURNING *) SELECT * FROM updated_orders",
                        SupportedMCPStatement.DML, "SELECT",
                        "WITH updated_orders AS (UPDATE foo_orders SET status = 'DONE' RETURNING *) SELECT * FROM updated_orders", "foo_orders", ""),
                Arguments.of("nested data modifying cte select",
                        "WITH foo_result AS (WITH updated_orders AS (UPDATE foo_orders SET status = 'DONE' RETURNING *) SELECT * FROM updated_orders) SELECT * FROM foo_result",
                        SupportedMCPStatement.DML, "SELECT",
                        "WITH foo_result AS (WITH updated_orders AS (UPDATE foo_orders SET status = 'DONE' RETURNING *) SELECT * FROM updated_orders) SELECT * FROM foo_result",
                        "foo_orders", ""),
                Arguments.of("insert dml", "INSERT INTO foo_orders VALUES (1)", SupportedMCPStatement.DML, "INSERT",
                        "INSERT INTO foo_orders VALUES (1)", "foo_orders", ""),
                Arguments.of("update dml", "UPDATE foo_orders SET status = 'DONE'", SupportedMCPStatement.DML, "UPDATE", "UPDATE foo_orders SET status = 'DONE'", "foo_orders", ""),
                Arguments.of("delete only dml", "DELETE FROM ONLY foo_orders WHERE order_id = 1", SupportedMCPStatement.DML, "DELETE",
                        "DELETE FROM ONLY foo_orders WHERE order_id = 1", "foo_orders", ""),
                Arguments.of("delete dml", "DELETE FROM foo_orders WHERE order_id = 1", SupportedMCPStatement.DML, "DELETE",
                        "DELETE FROM foo_orders WHERE order_id = 1", "foo_orders", ""),
                Arguments.of("merge dml", "MERGE INTO bar_orders_archive USING foo_orders ON 1 = 1", SupportedMCPStatement.DML, "MERGE",
                        "MERGE INTO bar_orders_archive USING foo_orders ON 1 = 1", "bar_orders_archive", ""),
                Arguments.of("create table ddl", "CREATE TABLE bar_orders_archive", SupportedMCPStatement.DDL, "CREATE", "CREATE TABLE bar_orders_archive", "bar_orders_archive", ""),
                Arguments.of("create table if not exists ddl", "CREATE TABLE IF NOT EXISTS bar_orders_archive", SupportedMCPStatement.DDL, "CREATE",
                        "CREATE TABLE IF NOT EXISTS bar_orders_archive", "bar_orders_archive", ""),
                Arguments.of("create view ddl", "CREATE VIEW bar_active_orders AS SELECT * FROM foo_orders", SupportedMCPStatement.DDL, "CREATE",
                        "CREATE VIEW bar_active_orders AS SELECT * FROM foo_orders", "bar_active_orders", ""),
                Arguments.of("create or replace view ddl", "CREATE OR REPLACE VIEW bar_active_orders AS SELECT * FROM foo_orders", SupportedMCPStatement.DDL, "CREATE",
                        "CREATE OR REPLACE VIEW bar_active_orders AS SELECT * FROM foo_orders", "bar_active_orders", ""),
                Arguments.of("alter ddl", "ALTER TABLE foo_orders ADD COLUMN status VARCHAR(10)", SupportedMCPStatement.DDL, "ALTER",
                        "ALTER TABLE foo_orders ADD COLUMN status VARCHAR(10)", "foo_orders", ""),
                Arguments.of("alter table only ddl", "ALTER TABLE ONLY foo_orders ADD COLUMN status VARCHAR(10)", SupportedMCPStatement.DDL, "ALTER",
                        "ALTER TABLE ONLY foo_orders ADD COLUMN status VARCHAR(10)", "foo_orders", ""),
                Arguments.of("drop ddl", "DROP TABLE foo_orders", SupportedMCPStatement.DDL, "DROP", "DROP TABLE foo_orders", "foo_orders", ""),
                Arguments.of("drop table if exists ddl", "DROP TABLE IF EXISTS foo_orders", SupportedMCPStatement.DDL, "DROP", "DROP TABLE IF EXISTS foo_orders", "foo_orders", ""),
                Arguments.of("truncate ddl", "TRUNCATE TABLE foo_orders", SupportedMCPStatement.DDL, "TRUNCATE", "TRUNCATE TABLE foo_orders", "foo_orders", ""),
                Arguments.of("grant dcl", "GRANT SELECT ON foo_orders TO PUBLIC", SupportedMCPStatement.DCL, "GRANT", "GRANT SELECT ON foo_orders TO PUBLIC", "foo_orders", ""),
                Arguments.of("revoke dcl", "REVOKE SELECT ON foo_orders FROM PUBLIC", SupportedMCPStatement.DCL, "REVOKE", "REVOKE SELECT ON foo_orders FROM PUBLIC", "foo_orders", ""),
                Arguments.of("begin transaction", "BEGIN", SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "BEGIN", "", ""),
                Arguments.of("start transaction", "START TRANSACTION READ WRITE", SupportedMCPStatement.TRANSACTION_CONTROL, "START TRANSACTION",
                        "START TRANSACTION READ WRITE", "", ""),
                Arguments.of("commit transaction", "COMMIT", SupportedMCPStatement.TRANSACTION_CONTROL, "COMMIT", "COMMIT", "", ""),
                Arguments.of("rollback transaction", "ROLLBACK", SupportedMCPStatement.TRANSACTION_CONTROL, "ROLLBACK", "ROLLBACK", "", ""),
                Arguments.of("savepoint", "SAVEPOINT foo_sp_1", SupportedMCPStatement.SAVEPOINT, "SAVEPOINT", "SAVEPOINT foo_sp_1", "", "foo_sp_1"),
                Arguments.of("release savepoint", "RELEASE SAVEPOINT foo_sp_1", SupportedMCPStatement.SAVEPOINT, "RELEASE SAVEPOINT",
                        "RELEASE SAVEPOINT foo_sp_1", "", "foo_sp_1"),
                Arguments.of("rollback to savepoint", "ROLLBACK TO SAVEPOINT foo_sp_1", SupportedMCPStatement.SAVEPOINT, "ROLLBACK TO SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT foo_sp_1", "", "foo_sp_1"),
                Arguments.of("explain analyze", "EXPLAIN ANALYZE SELECT * FROM foo_orders", SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE",
                        "EXPLAIN ANALYZE SELECT * FROM foo_orders", "foo_orders", ""));
    }
    
    private static Stream<Arguments> assertClassifyWithInvalidStatementCases() {
        return Stream.of(
                Arguments.of("blank sql", "   ", IllegalArgumentException.class, "sql cannot be empty."),
                Arguments.of("multiple statements", "SELECT 1; SELECT 2", IllegalArgumentException.class, "Only one SQL statement is allowed."),
                Arguments.of("savepoint without name", "SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("release savepoint without name", "RELEASE SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("rollback to savepoint without name", "ROLLBACK TO SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("banned use", "USE foo_db", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned set", "SET search_path public", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned copy", "COPY foo_orders FROM '/tmp/foo.csv'", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned load", "LOAD DATA INFILE '/tmp/foo.csv' INTO TABLE foo_orders", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned call", "CALL foo_refresh_orders()", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned select into outfile", "SELECT * FROM foo_orders INTO OUTFILE '/tmp/foo.csv'", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned alter system", "ALTER SYSTEM SET shared_buffers = '128MB'", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned create user", "CREATE USER foo_user IDENTIFIED BY 'pwd'", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned alter role", "ALTER ROLE foo_role SET search_path = public", UnsupportedOperationException.class, "Statement is banned by the MCP contract."),
                Arguments.of("unsupported statement", "SHOW TABLES", IllegalArgumentException.class, "Statement is not supported by the MCP contract."));
    }
}
