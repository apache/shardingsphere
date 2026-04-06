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

package org.apache.shardingsphere.mcp.execute;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPJdbcTransactionResourceManagerTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertFindTransactionConnection() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-find");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        manager.beginTransaction("session-1", "logic_db");
        Connection actual = manager.findTransactionConnection("session-1", "logic_db").orElseThrow(IllegalStateException::new);
        assertNotNull(actual);
        manager.closeSession("session-1");
    }
    
    @Test
    void assertBeginTransaction() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-begin");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        assertDoesNotThrow(() -> manager.beginTransaction("session-1", "logic_db"));
        manager.closeSession("session-1");
    }
    
    @Test
    void assertCommitTransaction() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-commit");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        manager.beginTransaction("session-1", "logic_db");
        executeStatement(manager.findTransactionConnection("session-1", "logic_db").orElseThrow(IllegalStateException::new),
                "UPDATE public.orders SET status = 'PROCESSING' WHERE order_id = 1");
        manager.commitTransaction("session-1");
        assertThat(querySingleString(jdbcUrl), is("PROCESSING"));
    }
    
    @Test
    void assertRollbackTransaction() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-rollback");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        manager.beginTransaction("session-1", "logic_db");
        executeStatement(manager.findTransactionConnection("session-1", "logic_db").orElseThrow(IllegalStateException::new),
                "UPDATE public.orders SET status = 'PROCESSING' WHERE order_id = 1");
        manager.rollbackTransaction("session-1");
        assertThat(querySingleString(jdbcUrl), is("NEW"));
    }
    
    @Test
    void assertCreateSavepoint() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-savepoint");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        manager.beginTransaction("session-1", "logic_db");
        assertDoesNotThrow(() -> manager.createSavepoint("session-1", "sp_1"));
        manager.closeSession("session-1");
    }
    
    @Test
    void assertRollbackToSavepoint() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-rollback-savepoint");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        manager.beginTransaction("session-1", "logic_db");
        executeStatement(manager.findTransactionConnection("session-1", "logic_db").orElseThrow(IllegalStateException::new),
                "UPDATE public.orders SET status = 'PROCESSING' WHERE order_id = 1");
        manager.createSavepoint("session-1", "sp_1");
        executeStatement(manager.findTransactionConnection("session-1", "logic_db").orElseThrow(IllegalStateException::new),
                "UPDATE public.orders SET status = 'DONE' WHERE order_id = 1");
        manager.rollbackToSavepoint("session-1", "sp_1");
        manager.commitTransaction("session-1");
        assertThat(querySingleString(jdbcUrl), is("PROCESSING"));
    }
    
    @Test
    void assertReleaseSavepoint() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-release-savepoint");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        manager.beginTransaction("session-1", "logic_db");
        manager.createSavepoint("session-1", "sp_1");
        assertDoesNotThrow(() -> manager.releaseSavepoint("session-1", "sp_1"));
        manager.closeSession("session-1");
    }
    
    @Test
    void assertCloseSession() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-close");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        manager.beginTransaction("session-1", "logic_db");
        executeStatement(manager.findTransactionConnection("session-1", "logic_db").orElseThrow(IllegalStateException::new),
                "UPDATE public.orders SET status = 'PROCESSING' WHERE order_id = 1");
        manager.closeSession("session-1");
        assertThat(querySingleString(jdbcUrl), is("NEW"));
    }
    
    @Test
    void assertFindTransactionConnectionWithCrossDatabaseTransaction() throws SQLException {
        String firstJdbcUrl = createJdbcUrl("resource-cross-first");
        String secondJdbcUrl = createJdbcUrl("resource-cross-second");
        initializeDatabase(firstJdbcUrl);
        initializeDatabase(secondJdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", firstJdbcUrl, "analytics_db", secondJdbcUrl));
        manager.beginTransaction("session-1", "logic_db");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> manager.findTransactionConnection("session-1", "analytics_db"));
        assertThat(actual.getMessage(), is("Cross-database transaction switching is not supported."));
        manager.closeSession("session-1");
    }
    
    @Test
    void assertFindTransactionConnectionWithoutTransaction() throws SQLException {
        String jdbcUrl = createJdbcUrl("resource-find-missing");
        initializeDatabase(jdbcUrl);
        MCPJdbcTransactionResourceManager manager = createResourceManager(Map.of("logic_db", jdbcUrl));
        assertTrue(manager.findTransactionConnection("session-1", "logic_db").isEmpty());
    }
    
    private MCPJdbcTransactionResourceManager createResourceManager(final Map<String, String> jdbcUrls) {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = new LinkedHashMap<>();
        for (Entry<String, String> entry : jdbcUrls.entrySet()) {
            runtimeDatabases.put(entry.getKey(), new RuntimeDatabaseConfiguration("H2", entry.getValue(), "", "", "org.h2.Driver"));
        }
        return new MCPJdbcTransactionResourceManager(runtimeDatabases);
    }
    
    private String createJdbcUrl(final String databaseName) {
        return String.format("jdbc:h2:file:%s;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                tempDir.resolve(databaseName).toAbsolutePath());
    }
    
    private void initializeDatabase(final String jdbcUrl) throws SQLException {
        executeStatements(jdbcUrl,
                "CREATE SCHEMA IF NOT EXISTS public",
                "SET SCHEMA public",
                "CREATE TABLE IF NOT EXISTS orders (order_id INT PRIMARY KEY, status VARCHAR(32), amount INT)",
                "MERGE INTO orders (order_id, status, amount) KEY (order_id) VALUES (1, 'NEW', 10)",
                "MERGE INTO orders (order_id, status, amount) KEY (order_id) VALUES (2, 'DONE', 20)");
    }
    
    private void executeStatement(final Connection connection, final String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    private void executeStatements(final String jdbcUrl, final String... sqls) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.execute(each);
            }
        }
    }
    
    private String querySingleString(final String jdbcUrl) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT status FROM public.orders WHERE order_id = 1")) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }
}
