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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyMetadataStatementTypeCases")
    void assertClassifyMetadataStatementType(final String name, final String sql, final String expectedStatementType) {
        MetadataIntrospectionSQLStatementException actual = assertThrows(MetadataIntrospectionSQLStatementException.class, () -> statementClassifier.classify(sql));
        assertThat(actual.getStatementType(), is(expectedStatementType));
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
    
    @Test
    void assertClassifyRuleDistSQLSideEffectScope() {
        ClassificationResult actualResult = statementClassifier.classify(
                "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'), KEY_GENERATE_STRATEGY(COLUMN=id, TYPE(NAME='snowflake')))");
        assertTrue(actualResult.isRuleDistSQL());
        assertThat(actualResult.getSideEffectScope(), is("rule-metadata"));
    }
    
    @Test
    void assertClassifyPhysicalDDLSideEffectScope() {
        ClassificationResult actualResult = statementClassifier.classify("CREATE TABLE foo_orders(order_id BIGINT)");
        assertFalse(actualResult.isRuleDistSQL());
        assertThat(actualResult.getSideEffectScope(), is("physical-structure"));
    }
    
    @Test
    void assertClassifySubqueryReferencedObjectNames() {
        ClassificationResult actualResult = statementClassifier.classify(
                "SELECT * FROM logic_db.foo_orders WHERE EXISTS (SELECT 1 FROM other_db.foo_order_items)");
        assertThat(actualResult.getReferencedObjectNames(), contains("logic_db.foo_orders", "other_db.foo_order_items"));
    }
    
    @Test
    void assertClassifyDerivedTableReferencedObjectNames() {
        ClassificationResult actualResult = statementClassifier.classify("SELECT * FROM (SELECT * FROM other_db.foo_order_items) foo_items");
        assertThat(actualResult.getReferencedObjectNames(), contains("other_db.foo_order_items"));
    }
    
    @Test
    void assertClassifyDMLSubqueryReferencedObjectNames() {
        ClassificationResult actualResult = statementClassifier.classify(
                "UPDATE logic_db.foo_orders SET status = 'DONE' WHERE EXISTS (SELECT 1 FROM other_db.foo_order_items)");
        assertThat(actualResult.getReferencedObjectNames(), contains("logic_db.foo_orders", "other_db.foo_order_items"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertClassifyReferencedObjectNamesWithObjectListsCases")
    void assertClassifyReferencedObjectNamesWithObjectLists(final String name, final String sql, final String[] expectedReferencedObjectNames) {
        ClassificationResult actualResult = statementClassifier.classify(sql);
        assertThat(actualResult.getReferencedObjectNames(), contains(expectedReferencedObjectNames));
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
                Arguments.of("rollback to savepoint name without optional keyword", "ROLLBACK TO foo_sp_1", SupportedMCPStatement.SAVEPOINT, "ROLLBACK TO",
                        "ROLLBACK TO foo_sp_1", "", "foo_sp_1"));
    }
    
    private static Stream<Arguments> assertClassifyReferencedObjectNamesWithObjectListsCases() {
        return Stream.of(
                Arguments.of("query from object list", "SELECT * FROM logic_db.foo_orders, other_db.foo_order_items",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("query from aliased object list", "SELECT * FROM logic_db.foo_orders o, other_db.foo_order_items i",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("query from partitioned object list", "SELECT * FROM logic_db.foo_orders PARTITION (p0) o, other_db.foo_order_items i",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("query from index hinted object list", "SELECT * FROM logic_db.foo_orders FORCE INDEX (idx_status), other_db.foo_order_items",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("cte query from object list", "WITH foo_result AS (SELECT * FROM logic_db.foo_orders, other_db.foo_order_items) SELECT * FROM foo_result",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("unused cte query reference", "WITH unused_result AS (SELECT * FROM other_db.foo_unused_orders) SELECT * FROM logic_db.foo_orders",
                        new String[]{"other_db.foo_unused_orders", "logic_db.foo_orders"}),
                Arguments.of("insert select from object list", "INSERT INTO logic_db.foo_orders_archive SELECT * FROM logic_db.foo_orders, other_db.foo_order_items",
                        new String[]{"logic_db.foo_orders_archive", "logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("update target object list", "UPDATE logic_db.foo_orders, other_db.foo_order_items SET status = 'DONE'",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("update target aliased object list", "UPDATE logic_db.foo_orders o, other_db.foo_order_items i SET o.status = 'DONE'",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("delete target before from", "DELETE other_db.foo_orders FROM logic_db.foo_orders JOIN other_db.foo_order_items ON 1 = 1",
                        new String[]{"other_db.foo_orders", "logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("delete target object list before from", "DELETE logic_db.foo_orders, other_db.foo_order_items FROM logic_db.foo_orders JOIN other_db.foo_order_items ON 1 = 1",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("delete using object list", "DELETE FROM logic_db.foo_orders USING other_db.foo_order_items, other_db.foo_order_payments",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items", "other_db.foo_order_payments"}),
                Arguments.of("create view from object list", "CREATE VIEW logic_db.foo_active_orders AS SELECT * FROM logic_db.foo_orders, other_db.foo_order_items",
                        new String[]{"logic_db.foo_active_orders", "logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("create table like source", "CREATE TABLE logic_db.foo_orders_archive LIKE other_db.foo_orders",
                        new String[]{"logic_db.foo_orders_archive", "other_db.foo_orders"}),
                Arguments.of("create index on source", "CREATE INDEX foo_orders_idx ON other_db.foo_orders (status)",
                        new String[]{"foo_orders_idx", "other_db.foo_orders"}),
                Arguments.of("create index using method", "CREATE INDEX foo_orders_idx ON logic_db.foo_orders USING btree (status)",
                        new String[]{"foo_orders_idx", "logic_db.foo_orders"}),
                Arguments.of("create index with modifiers", "CREATE INDEX CONCURRENTLY IF NOT EXISTS other_db.foo_orders_idx ON logic_db.foo_orders (status)",
                        new String[]{"other_db.foo_orders_idx", "logic_db.foo_orders"}),
                Arguments.of("drop index on source", "DROP INDEX foo_orders_idx ON other_db.foo_orders",
                        new String[]{"foo_orders_idx", "other_db.foo_orders"}),
                Arguments.of("drop index with modifiers", "DROP INDEX CONCURRENTLY IF EXISTS other_db.foo_orders_idx",
                        new String[]{"other_db.foo_orders_idx"}),
                Arguments.of("drop table object list", "DROP TABLE IF EXISTS logic_db.foo_orders, other_db.foo_order_items",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("alter table with modifiers", "ALTER TABLE IF EXISTS other_db.foo_orders ADD COLUMN status VARCHAR(10)",
                        new String[]{"other_db.foo_orders"}),
                Arguments.of("alter table rename destination", "ALTER TABLE logic_db.foo_orders RENAME TO other_db.foo_orders_archive",
                        new String[]{"logic_db.foo_orders", "other_db.foo_orders_archive"}),
                Arguments.of("alter table set schema destination", "ALTER TABLE logic_db.foo_orders SET SCHEMA other_db",
                        new String[]{"logic_db.foo_orders", "other_db"}),
                Arguments.of("create database target", "CREATE DATABASE other_db",
                        new String[]{"other_db"}),
                Arguments.of("drop schema target", "DROP SCHEMA IF EXISTS other_db",
                        new String[]{"other_db"}),
                Arguments.of("grant global wildcard", "GRANT SELECT ON *.* TO PUBLIC",
                        new String[]{"*.*"}),
                Arguments.of("grant database target", "GRANT CONNECT ON DATABASE other_db TO PUBLIC",
                        new String[]{"other_db"}),
                Arguments.of("truncate table object list", "TRUNCATE TABLE logic_db.foo_orders, other_db.foo_order_items",
                        new String[]{"logic_db.foo_orders", "other_db.foo_order_items"}),
                Arguments.of("qualified function query", "SELECT other_db.foo_refresh_orders()",
                        new String[]{"other_db.foo_refresh_orders"}));
    }
    
    private static Stream<Arguments> assertClassifyMetadataStatementTypeCases() {
        return Stream.of(
                Arguments.of("show storage units", "SHOW STORAGE UNITS FROM logic_db", "SHOW STORAGE UNITS"),
                Arguments.of("show rules used storage unit", "SHOW RULES USED STORAGE UNIT write_ds FROM logic_db", "SHOW RULES USED STORAGE UNIT"),
                Arguments.of("show single tables", "SHOW SINGLE TABLES FROM logic_db", "SHOW SINGLE TABLES"),
                Arguments.of("show single table", "SHOW SINGLE TABLE t_user FROM logic_db", "SHOW SINGLE TABLE"),
                Arguments.of("show default single table storage unit", "SHOW DEFAULT SINGLE TABLE STORAGE UNIT FROM logic_db", "SHOW DEFAULT SINGLE TABLE STORAGE UNIT"));
    }
    
    private static Stream<Arguments> assertClassifyWithInvalidStatementCases() {
        return Stream.of(
                Arguments.of("blank sql", "   ", IllegalArgumentException.class, "sql cannot be empty."),
                Arguments.of("multiple statements", "SELECT 1; SELECT 2", MCPMultipleSQLStatementsException.class, "Only one SQL statement is allowed."),
                Arguments.of("savepoint without name", "SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("savepoint with extra token", "SAVEPOINT foo_sp_1 extra", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("release savepoint without name", "RELEASE SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("release savepoint with extra token", "RELEASE SAVEPOINT foo_sp_1 extra", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("rollback to without name", "ROLLBACK TO", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("rollback to savepoint without name", "ROLLBACK TO SAVEPOINT", IllegalArgumentException.class, "Savepoint name is required."),
                Arguments.of("rollback to savepoint with extra token", "ROLLBACK TO SAVEPOINT foo_sp_1 extra", IllegalArgumentException.class, "Savepoint name is required."),
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
                Arguments.of("banned mysql executable comment", "SELECT * FROM logic_db.foo_orders /*!50000 JOIN other_db.foo_order_items ON 1 = 1 */",
                        MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned mariadb executable comment", "SELECT * FROM logic_db.foo_orders /*M!100000 JOIN other_db.foo_order_items ON 1 = 1 */",
                        MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned nextval function", "SELECT nextval('foo_seq')", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned setval function", "SELECT pg_catalog.setval('foo_seq', 1)", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned next value for sequence", "SELECT NEXT VALUE FOR foo_seq", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned sequence nextval pseudocolumn", "SELECT foo_seq.NEXTVAL FROM dual", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned get lock function", "SELECT GET_LOCK('foo_lock', 1)", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned release lock function", "SELECT RELEASE_LOCK('foo_lock')", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned advisory lock function", "SELECT pg_advisory_lock(1)", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned set config function", "SELECT set_config('search_path', 'public', false)", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned replication slot advance function", "SELECT pg_replication_slot_advance('foo_slot', '0/1')", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned wal switch function", "SELECT pg_switch_wal()", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned backend cancel function", "SELECT pg_cancel_backend(1)", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned mysql user variable assignment", "SELECT @foo_status := status FROM foo_orders", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
                Arguments.of("banned mysql last insert id mutation", "SELECT LAST_INSERT_ID(1)", MCPBannedSQLStatementException.class, "Statement is banned by the MCP contract."),
                Arguments.of("banned metadata lookup function", "SELECT to_regclass('other_db.foo_orders')", MCPBannedSQLStatementException.class,
                        "Statement is banned by the MCP contract."),
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
                Arguments.of("explain query", "EXPLAIN SELECT * FROM foo_orders", MCPUnsupportedSQLStatementException.class, "Statement is not supported by the MCP contract."),
                Arguments.of("explain locking read", "EXPLAIN SELECT * FROM foo_orders FOR SHARE", MCPUnsupportedSQLStatementException.class, "Statement is not supported by the MCP contract."),
                Arguments.of("explain analyze", "EXPLAIN ANALYZE SELECT * FROM foo_orders", MCPUnsupportedSQLStatementException.class, "Statement is not supported by the MCP contract."),
                Arguments.of("metadata show", "SHOW", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata show tables", "SHOW TABLES", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata describe", "DESCRIBE", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata describe table", "DESCRIBE foo_orders", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata desc", "DESC", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("metadata desc table", "DESC foo_orders", MetadataIntrospectionSQLStatementException.class, "Metadata introspection SQL should use MCP metadata resources."),
                Arguments.of("unsupported statement", "ANALYZE TABLE foo_orders", MCPUnsupportedSQLStatementException.class, "Statement is not supported by the MCP contract."));
    }
}
