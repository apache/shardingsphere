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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPLockingReadStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMultipleSQLStatementsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
    
    @Test
    void assertClassifyExplainAnalyzeInnerStatementClass() {
        ClassificationResult actualResult = statementClassifier.classify("EXPLAIN ANALYZE UPDATE foo_orders SET status = 'DONE'");
        assertThat(actualResult.getStatementClass(), is(SupportedMCPStatement.EXPLAIN_ANALYZE));
        assertThat(actualResult.getAnalyzedStatementClass().orElseThrow(), is(SupportedMCPStatement.DML));
        assertThat(actualResult.getTargetObjectName().orElse(""), is("foo_orders"));
    }
    
    @Test
    void assertClassifyReferencedObjectNames() {
        ClassificationResult actualResult = statementClassifier.classify(
                "SELECT * FROM logic_db.foo_orders JOIN other_db.foo_order_items ON foo_orders.order_id = foo_order_items.order_id");
        assertThat(actualResult.getReferencedObjectNames(), contains("logic_db.foo_orders", "other_db.foo_order_items"));
    }
    
    @Test
    void assertClassifyDMLReferencedObjectNames() {
        ClassificationResult actualResult = statementClassifier.classify("UPDATE logic_db.foo_orders SET status = 'DONE' FROM other_db.foo_order_items");
        assertThat(actualResult.getReferencedObjectNames(), contains("logic_db.foo_orders", "other_db.foo_order_items"));
    }
    
    private static Stream<Arguments> assertClassifyCases() {
        return Stream.of(
                Arguments.of("trim trailing semicolon query", "  SELECT * FROM foo_orders ;  ", SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM foo_orders", "foo_orders", ""),
                Arguments.of("query with semicolon literal", "SELECT ';' AS foo_literal", SupportedMCPStatement.QUERY, "SELECT", "SELECT ';' AS foo_literal", "", ""),
                Arguments.of("query with semicolon block comment", "SELECT * FROM foo_orders /* source; */", SupportedMCPStatement.QUERY, "SELECT",
                        "SELECT * FROM foo_orders /* source; */", "foo_orders", ""),
                Arguments.of("trim trailing semicolon query with comment", "SELECT * FROM foo_orders; -- trailing;", SupportedMCPStatement.QUERY, "SELECT",
                        "SELECT * FROM foo_orders", "foo_orders", ""),
                Arguments.of("query from values target omitted", "SELECT * FROM (VALUES (1)) foo_result", SupportedMCPStatement.QUERY, "SELECT",
                        "SELECT * FROM (VALUES (1)) foo_result", "", ""),
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
                Arguments.of("insert select dml", "INSERT INTO bar_orders_archive SELECT * FROM foo_orders", SupportedMCPStatement.DML, "INSERT",
                        "INSERT INTO bar_orders_archive SELECT * FROM foo_orders", "bar_orders_archive", ""),
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
                Arguments.of("start transaction", "START TRANSACTION", SupportedMCPStatement.TRANSACTION_CONTROL, "START TRANSACTION", "START TRANSACTION", "", ""),
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
                Arguments.of("multiple statements", "SELECT 1; SELECT 2", MCPMultipleSQLStatementsException.class, "Only one SQL statement is allowed."),
                Arguments.of("savepoint without name", "SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("release savepoint without name", "RELEASE SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("rollback to savepoint without name", "ROLLBACK TO SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("banned use", "USE foo_db", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned set", "SET search_path public", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned copy", "COPY foo_orders FROM '/tmp/foo.csv'", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned load", "LOAD DATA INFILE '/tmp/foo.csv' INTO TABLE foo_orders", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned call", "CALL foo_refresh_orders()", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned select into outfile", "SELECT * FROM foo_orders INTO OUTFILE '/tmp/foo.csv'", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned select into table", "SELECT * INTO bar_orders_archive FROM foo_orders", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned select into variable", "SELECT COUNT(*) INTO foo_count FROM foo_orders", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned with select into table", "WITH foo_result AS (SELECT * FROM foo_orders) SELECT * INTO bar_orders_archive FROM foo_result",
                        MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("start transaction read only", "START TRANSACTION READ ONLY", MCPUnsupportedSQLStatementException.class,
                        "Statement is not supported by the MCP contract."),
                Arguments.of("start transaction read write", "START TRANSACTION READ WRITE", MCPUnsupportedSQLStatementException.class,
                        "Statement is not supported by the MCP contract."),
                Arguments.of("start transaction isolation level", "START TRANSACTION ISOLATION LEVEL SERIALIZABLE", MCPUnsupportedSQLStatementException.class,
                        "Statement is not supported by the MCP contract."),
                Arguments.of("banned alter system", "ALTER SYSTEM SET shared_buffers = '128MB'", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned create user", "CREATE USER foo_user IDENTIFIED BY 'pwd'", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned alter role", "ALTER ROLE foo_role SET search_path = public", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("locking read for update", "SELECT * FROM foo_orders FOR UPDATE", MCPLockingReadStatementException.class,
                        "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."),
                Arguments.of("locking read for no key update", "SELECT * FROM foo_orders FOR NO KEY UPDATE", MCPLockingReadStatementException.class,
                        "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."),
                Arguments.of("locking read lock in share mode", "SELECT * FROM foo_orders LOCK IN SHARE MODE", MCPLockingReadStatementException.class,
                        "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."),
                Arguments.of("explain analyze locking read", "EXPLAIN ANALYZE SELECT * FROM foo_orders FOR SHARE", MCPLockingReadStatementException.class,
                        "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."),
                Arguments.of("metadata show", "SHOW", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata show tables", "SHOW TABLES", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata describe", "DESCRIBE", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata describe table", "DESCRIBE foo_orders", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata desc", "DESC", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata desc table", "DESC foo_orders", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("unsupported statement", "ANALYZE TABLE foo_orders", MCPUnsupportedSQLStatementException.class, "Statement is not supported by the MCP contract."));
    }
}
