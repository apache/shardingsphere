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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPLockingReadStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPMultipleSQLStatementsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPStatementAnalyzerTest {
    
    private final MCPStatementAnalyzer analyzer = new MCPStatementAnalyzer();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedDatabaseTypes")
    void assertAnalyzeWithSupportedDatabaseType(final String databaseType, final String sql) {
        ClassificationResult actual = analyzer.analyze(sql, createCapability(databaseType));
        assertThat(actual.getStatementClass(), is(SupportedMCPStatement.QUERY));
        assertThat(actual.getStatementType(), is("SELECT"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("statementCases")
    void assertAnalyze(final String name, final String databaseType, final String sql, final SupportedMCPStatement expectedStatementClass,
                       final String expectedStatementType, final String expectedNormalizedSql, final String expectedTargetObjectName, final String expectedSavepointName) {
        ClassificationResult actual = analyzer.analyze(sql, createCapability(databaseType));
        assertThat(actual.getStatementClass(), is(expectedStatementClass));
        assertThat(actual.getStatementType(), is(expectedStatementType));
        assertThat(actual.getNormalizedSql(), is(expectedNormalizedSql));
        assertThat(actual.getTargetObjectName().orElse(""), is(expectedTargetObjectName));
        assertThat(actual.getSavepointName().orElse(""), is(expectedSavepointName));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStatementCases")
    void assertAnalyzeWithInvalidStatement(final String name, final String databaseType, final String sql, final Class<? extends RuntimeException> expectedExceptionClass,
                                           final String expectedMessage) {
        RuntimeException actual = assertThrows(expectedExceptionClass, () -> analyzer.analyze(sql, createCapability(databaseType)));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("metadataStatementCases")
    void assertAnalyzeMetadataStatement(final String name, final String sql, final String expectedStatementType) {
        MetadataIntrospectionSQLStatementException actual = assertThrows(
                MetadataIntrospectionSQLStatementException.class, () -> analyzer.analyze(sql, createCapability("MySQL")));
        assertThat(actual.getStatementType(), is(expectedStatementType));
    }
    
    @Test
    void assertAnalyzeReferencedObjects() {
        ClassificationResult actual = analyzer.analyze(
                "SELECT * FROM logic_db.orders JOIN other_db.order_items ON orders.order_id = order_items.order_id", createCapability("MySQL"));
        assertThat(actual.getReferencedObjectNames(), contains("logic_db.orders", "other_db.order_items"));
    }
    
    @Test
    void assertAnalyzeCommonTableExpressionReferences() {
        ClassificationResult actual = analyzer.analyze(
                "WITH order_result AS (SELECT * FROM other_db.orders) SELECT * FROM order_result", createCapability("PostgreSQL"));
        assertThat(actual.getReferencedObjectNames(), contains("other_db.orders"));
    }
    
    @Test
    void assertAnalyzeDMLReferences() {
        ClassificationResult actual = analyzer.analyze(
                "UPDATE logic_db.orders SET status = 'DONE' FROM other_db.order_items", createCapability("PostgreSQL"));
        assertThat(actual.getReferencedObjectNames(), contains("logic_db.orders", "other_db.order_items"));
    }
    
    @Test
    void assertAnalyzeAlterViewReferences() {
        ClassificationResult actual = analyzer.analyze(
                "ALTER VIEW logic_db.active_orders AS SELECT * FROM other_db.orders", createCapability("MySQL"));
        assertThat(actual.getReferencedObjectNames(), contains("logic_db.active_orders", "other_db.orders"));
    }
    
    @Test
    void assertAnalyzeSameNamedDMLReferences() {
        ClassificationResult actual = analyzer.analyze(
                "UPDATE orders SET status = source.status FROM other_db.orders source", createCapability("PostgreSQL"));
        assertThat(actual.getReferencedObjectNames(), contains("orders", "other_db.orders"));
        assertThat(actual.getTargetObjectName().orElse(""), is("orders"));
    }
    
    @Test
    void assertAnalyzeDCLReference() {
        ClassificationResult actual = analyzer.analyze("GRANT SELECT ON other_db.orders TO PUBLIC", createCapability("MySQL"));
        assertThat(actual.getReferencedObjectNames(), contains("other_db.orders"));
    }
    
    @Test
    void assertAnalyzeGlobalDCLReference() {
        ClassificationResult actual = analyzer.analyze("GRANT SELECT ON *.* TO PUBLIC", createCapability("MySQL"));
        assertThat(actual.getReferencedObjectNames(), contains("*.*"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dclNamespaceCases")
    void assertAnalyzeDCLNamespaceReference(final String name, final String sql) {
        ClassificationResult actual = analyzer.analyze(sql, createCapability("PostgreSQL"));
        assertThat(actual.getReferencedObjectNames(), contains("other_db"));
        assertTrue(actual.getReferencedObjects().iterator().next().isNamespaceTarget());
    }
    
    @Test
    void assertAnalyzeQualifiedFunctionReference() {
        ClassificationResult actual = analyzer.analyze("SELECT other_db.foo_refresh_orders()", createCapability("MySQL"));
        assertThat(actual.getReferencedObjectNames(), contains("other_db.foo_refresh_orders"));
    }
    
    @Test
    void assertAnalyzeRuleDistSQL() {
        ClassificationResult actual = analyzer.analyze(
                "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'))", createCapability("MySQL"));
        assertTrue(actual.isRuleDistSQL());
        assertThat(actual.getSideEffectScope(), is("rule-metadata"));
    }
    
    @Test
    void assertAnalyzePhysicalDDL() {
        ClassificationResult actual = analyzer.analyze("CREATE TABLE orders(order_id BIGINT)", createCapability("MySQL"));
        assertFalse(actual.isRuleDistSQL());
        assertThat(actual.getSideEffectScope(), is("physical-structure"));
    }
    
    private MCPDatabaseCapability createCapability(final String databaseType) {
        MCPDatabaseCapability result = mock(MCPDatabaseCapability.class);
        when(result.getDatabaseType()).thenReturn(databaseType);
        return result;
    }
    
    private static Stream<Arguments> supportedDatabaseTypes() {
        return Stream.of(
                Arguments.of("MySQL", "SELECT 1"),
                Arguments.of("MariaDB", "SELECT 1"),
                Arguments.of("PostgreSQL", "SELECT 1"),
                Arguments.of("openGauss", "SELECT 1"),
                Arguments.of("Oracle", "SELECT 1 FROM DUAL"),
                Arguments.of("SQLServer", "SELECT 1"),
                Arguments.of("Firebird", "SELECT 1 FROM RDB$DATABASE"),
                Arguments.of("ClickHouse", "SELECT 1"),
                Arguments.of("Hive", "SELECT 1"),
                Arguments.of("Presto", "SELECT 1"));
    }
    
    private static Stream<Arguments> dclNamespaceCases() {
        return Stream.of(
                Arguments.of("database", "GRANT CONNECT ON DATABASE other_db TO PUBLIC"),
                Arguments.of("schema", "GRANT USAGE ON SCHEMA other_db TO PUBLIC"));
    }
    
    private static Stream<Arguments> statementCases() {
        return Stream.of(
                Arguments.of("trim trailing semicolon", "MySQL", "  SELECT * FROM orders ;  ", SupportedMCPStatement.QUERY, "SELECT", "SELECT * FROM orders", "orders", ""),
                Arguments.of("semicolon literal", "MySQL", "SELECT ';' AS literal_value", SupportedMCPStatement.QUERY, "SELECT", "SELECT ';' AS literal_value", "", ""),
                Arguments.of("with query", "PostgreSQL", "WITH order_result AS (SELECT * FROM orders) SELECT * FROM order_result",
                        SupportedMCPStatement.QUERY, "SELECT", "WITH order_result AS (SELECT * FROM orders) SELECT * FROM order_result", "orders", ""),
                Arguments.of("with update", "PostgreSQL", "WITH order_result AS (SELECT * FROM orders) UPDATE order_archive SET status = 'DONE' FROM order_result",
                        SupportedMCPStatement.DML, "UPDATE", "WITH order_result AS (SELECT * FROM orders) UPDATE order_archive SET status = 'DONE' FROM order_result", "order_archive", ""),
                Arguments.of("insert", "MySQL", "INSERT INTO orders VALUES (1)", SupportedMCPStatement.DML, "INSERT", "INSERT INTO orders VALUES (1)", "orders", ""),
                Arguments.of("update", "MySQL", "UPDATE orders SET status = 'DONE'", SupportedMCPStatement.DML, "UPDATE", "UPDATE orders SET status = 'DONE'", "orders", ""),
                Arguments.of("delete", "MySQL", "DELETE FROM orders WHERE order_id = 1", SupportedMCPStatement.DML, "DELETE",
                        "DELETE FROM orders WHERE order_id = 1", "orders", ""),
                Arguments.of("merge", "Oracle", "MERGE INTO order_archive USING orders ON (order_archive.order_id = orders.order_id)", SupportedMCPStatement.DML, "MERGE",
                        "MERGE INTO order_archive USING orders ON (order_archive.order_id = orders.order_id)", "order_archive", ""),
                Arguments.of("create table", "MySQL", "CREATE TABLE order_archive(order_id BIGINT)", SupportedMCPStatement.DDL, "CREATE",
                        "CREATE TABLE order_archive(order_id BIGINT)", "order_archive", ""),
                Arguments.of("create index", "PostgreSQL", "CREATE INDEX order_idx ON orders (status)", SupportedMCPStatement.DDL, "CREATE",
                        "CREATE INDEX order_idx ON orders (status)", "order_idx", ""),
                Arguments.of("alter table", "MySQL", "ALTER TABLE orders ADD COLUMN status VARCHAR(10)", SupportedMCPStatement.DDL, "ALTER",
                        "ALTER TABLE orders ADD COLUMN status VARCHAR(10)", "orders", ""),
                Arguments.of("drop table", "MySQL", "DROP TABLE orders", SupportedMCPStatement.DDL, "DROP", "DROP TABLE orders", "orders", ""),
                Arguments.of("truncate table", "MySQL", "TRUNCATE TABLE orders", SupportedMCPStatement.DDL, "TRUNCATE", "TRUNCATE TABLE orders", "orders", ""),
                Arguments.of("grant", "MySQL", "GRANT SELECT ON orders TO PUBLIC", SupportedMCPStatement.DCL, "GRANT", "GRANT SELECT ON orders TO PUBLIC", "orders", ""),
                Arguments.of("revoke", "MySQL", "REVOKE SELECT ON orders FROM PUBLIC", SupportedMCPStatement.DCL, "REVOKE", "REVOKE SELECT ON orders FROM PUBLIC", "orders", ""),
                Arguments.of("begin", "MySQL", "BEGIN", SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", "BEGIN", "", ""),
                Arguments.of("start transaction", "MySQL", "START TRANSACTION", SupportedMCPStatement.TRANSACTION_CONTROL, "START TRANSACTION", "START TRANSACTION", "", ""),
                Arguments.of("commit", "MySQL", "COMMIT", SupportedMCPStatement.TRANSACTION_CONTROL, "COMMIT", "COMMIT", "", ""),
                Arguments.of("rollback", "MySQL", "ROLLBACK", SupportedMCPStatement.TRANSACTION_CONTROL, "ROLLBACK", "ROLLBACK", "", ""),
                Arguments.of("savepoint", "MySQL", "SAVEPOINT order_sp", SupportedMCPStatement.SAVEPOINT, "SAVEPOINT", "SAVEPOINT order_sp", "", "order_sp"),
                Arguments.of("rollback to without optional keyword", "MySQL", "ROLLBACK TO order_sp", SupportedMCPStatement.SAVEPOINT, "ROLLBACK TO SAVEPOINT",
                        "ROLLBACK TO order_sp", "", "order_sp"),
                Arguments.of("rollback to", "MySQL", "ROLLBACK TO SAVEPOINT order_sp", SupportedMCPStatement.SAVEPOINT, "ROLLBACK TO SAVEPOINT",
                        "ROLLBACK TO SAVEPOINT order_sp", "", "order_sp"),
                Arguments.of("release savepoint", "MySQL", "RELEASE SAVEPOINT order_sp", SupportedMCPStatement.SAVEPOINT, "RELEASE SAVEPOINT",
                        "RELEASE SAVEPOINT order_sp", "", "order_sp"));
    }
    
    private static Stream<Arguments> invalidStatementCases() {
        return Stream.of(
                Arguments.of("blank", "MySQL", "   ", MCPInvalidRequestException.class, "sql cannot be empty."),
                Arguments.of("delimiter only", "MySQL", ";", MCPInvalidRequestException.class, "sql cannot be empty."),
                Arguments.of("multiple statements", "MySQL", "SELECT 1; SELECT 2", MCPMultipleSQLStatementsException.class, "Only one SQL statement is allowed."),
                Arguments.of("parser unavailable", "H2", "SELECT 1", MCPUnsupportedException.class, "SQL parser is not available for database type `H2`."),
                Arguments.of("savepoint without name", "MySQL", "SAVEPOINT", MCPInvalidRequestException.class, "Savepoint name is required."),
                Arguments.of("savepoint with extra token", "MySQL", "SAVEPOINT order_sp extra", MCPInvalidRequestException.class, "Savepoint name is required."),
                Arguments.of("release savepoint without name", "MySQL", "RELEASE SAVEPOINT", MCPInvalidRequestException.class, "Savepoint name is required."),
                Arguments.of("release savepoint with extra token", "MySQL", "RELEASE SAVEPOINT order_sp extra", MCPInvalidRequestException.class,
                        "Savepoint name is required."),
                Arguments.of("rollback to without name", "MySQL", "ROLLBACK TO", MCPInvalidRequestException.class, "Savepoint name is required."),
                Arguments.of("rollback to savepoint without name", "MySQL", "ROLLBACK TO SAVEPOINT", MCPInvalidRequestException.class, "Savepoint name is required."),
                Arguments.of("rollback to savepoint with extra token", "MySQL", "ROLLBACK TO SAVEPOINT order_sp extra", MCPInvalidRequestException.class,
                        "Savepoint name is required."),
                Arguments.of("banned use", "MySQL", "USE other_db", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned set", "PostgreSQL", "SET search_path public", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned copy", "PostgreSQL", "COPY orders FROM '/tmp/orders.csv'", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned load", "MySQL", "LOAD DATA INFILE '/tmp/orders.csv' INTO TABLE orders", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned call", "MySQL", "CALL refresh_orders()", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned select into outfile", "MySQL", "SELECT * FROM orders INTO OUTFILE '/tmp/orders.csv'", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned executable comment", "MySQL", "SELECT 1 /*!50000 UNION SELECT 2 */", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned MariaDB executable comment", "MariaDB", "SELECT 1 /*M!100000 UNION SELECT 2 */", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned nextval", "PostgreSQL", "SELECT nextval('order_seq')", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned setval", "PostgreSQL", "SELECT pg_catalog.setval('order_seq', 1)", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned next value for", "SQLServer", "SELECT NEXT VALUE FOR order_seq", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned nextval pseudocolumn", "Oracle", "SELECT order_seq.NEXTVAL FROM dual", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned named lock", "MySQL", "SELECT GET_LOCK('order_lock', 1)", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned named lock release", "MySQL", "SELECT RELEASE_LOCK('order_lock')", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned advisory lock", "PostgreSQL", "SELECT pg_advisory_lock(1)", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned configuration mutation", "PostgreSQL", "SELECT set_config('search_path', 'public', false)", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned replication slot advance", "PostgreSQL", "SELECT pg_replication_slot_advance('order_slot', '0/1')",
                        MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned wal switch", "PostgreSQL", "SELECT pg_switch_wal()", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned backend cancellation", "PostgreSQL", "SELECT pg_cancel_backend(1)", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned user variable assignment", "MySQL", "SELECT @order_status := status FROM orders", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned last insert id mutation", "MySQL", "SELECT LAST_INSERT_ID(1)", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned metadata lookup", "PostgreSQL", "SELECT to_regclass('orders')", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned alter system", "PostgreSQL", "ALTER SYSTEM SET shared_buffers = '128MB'", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned create user", "MySQL", "CREATE USER app_user IDENTIFIED BY 'pwd'", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned alter role", "PostgreSQL", "ALTER ROLE app_role SET search_path = public", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("select into", "PostgreSQL", "SELECT * INTO order_archive FROM orders", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("select into variable", "PostgreSQL", "SELECT COUNT(*) INTO order_count FROM orders", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("with select into", "PostgreSQL", "WITH order_result AS (SELECT * FROM orders) SELECT * INTO order_archive FROM order_result",
                        MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("locking read", "PostgreSQL", "SELECT * FROM orders FOR UPDATE", MCPLockingReadStatementException.class,
                        "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."),
                Arguments.of("locking read for no key update", "PostgreSQL", "SELECT * FROM orders FOR NO KEY UPDATE", MCPLockingReadStatementException.class,
                        "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."),
                Arguments.of("locking read in share mode", "MySQL", "SELECT * FROM orders LOCK IN SHARE MODE", MCPLockingReadStatementException.class,
                        "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."),
                Arguments.of("nested locking read", "MySQL", "SELECT * FROM (SELECT * FROM orders FOR UPDATE) locked_orders", MCPLockingReadStatementException.class,
                        "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."),
                Arguments.of("metadata show", "MySQL", "SHOW TABLES", MetadataIntrospectionSQLStatementException.class,
                        "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("explain", "MySQL", "EXPLAIN SELECT * FROM orders", MCPUnsupportedSQLStatementException.class,
                        "Statement is not supported by the MCP contract."),
                Arguments.of("start transaction options", "PostgreSQL", "START TRANSACTION READ ONLY", MCPUnsupportedSQLStatementException.class,
                        "Statement is not supported by the MCP contract."),
                Arguments.of("wrong dialect", "PostgreSQL", "SELECT * FROM orders LIMIT 1, 2", MCPUnsupportedSQLStatementException.class,
                        "Statement is not supported by the MCP contract."),
                Arguments.of("data modifying cte", "PostgreSQL", "WITH updated AS (UPDATE orders SET status = 'DONE' RETURNING *) SELECT * FROM updated",
                        MCPUnsupportedSQLStatementException.class, "Statement is not supported by the MCP contract."),
                Arguments.of("unsupported dal", "MySQL", "ANALYZE TABLE orders", MCPUnsupportedSQLStatementException.class,
                        "Statement is not supported by the MCP contract."));
    }
    
    private static Stream<Arguments> metadataStatementCases() {
        return Stream.of(
                Arguments.of("show storage units", "SHOW STORAGE UNITS FROM logic_db", "SHOW STORAGE UNITS"),
                Arguments.of("show rules used storage unit", "SHOW RULES USED STORAGE UNIT write_ds FROM logic_db", "SHOW RULES USED STORAGE UNIT"),
                Arguments.of("show single tables", "SHOW SINGLE TABLES FROM logic_db", "SHOW SINGLE TABLES"),
                Arguments.of("show single table", "SHOW SINGLE TABLE orders FROM logic_db", "SHOW SINGLE TABLE"),
                Arguments.of("show default single table storage unit", "SHOW DEFAULT SINGLE TABLE STORAGE UNIT FROM logic_db",
                        "SHOW DEFAULT SINGLE TABLE STORAGE UNIT"),
                Arguments.of("describe", "DESCRIBE orders", "DESCRIBE"),
                Arguments.of("desc", "DESC orders", "DESC"));
    }
}
