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

import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResultKind;
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
import static org.junit.jupiter.api.Assertions.assertFalse;

class ShardingSphereExecutionAdapterTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertExecuteQuery() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-query");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        ExecuteQueryResponse actual = adapter.execute(createExecutionRequest("logic_db", "SELECT status FROM orders ORDER BY order_id"),
                new StatementClassifier().classify("SELECT status FROM orders ORDER BY order_id"));
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getRows().size(), is(2));
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-update");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        ExecuteQueryResponse actual = adapter.execute(createExecutionRequest("logic_db", "UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"),
                new StatementClassifier().classify("UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"));
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
        assertThat(actual.getAffectedRows(), is(1));
        assertThat(querySingleString(jdbcUrl), is("PROCESSING"));
    }
    
    @Test
    void assertBeginTransaction() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-begin");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        assertDoesNotThrow(() -> adapter.beginTransaction("session-1", "logic_db"));
        adapter.closeSession("session-1");
    }
    
    @Test
    void assertCommitTransaction() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-commit");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        adapter.beginTransaction("session-1", "logic_db");
        adapter.execute(createExecutionRequest("logic_db", "UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"),
                new StatementClassifier().classify("UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"));
        adapter.commitTransaction("session-1");
        assertThat(querySingleString(jdbcUrl), is("PROCESSING"));
    }
    
    @Test
    void assertRollbackTransaction() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-rollback");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        adapter.beginTransaction("session-1", "logic_db");
        adapter.execute(createExecutionRequest("logic_db", "UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"),
                new StatementClassifier().classify("UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"));
        adapter.rollbackTransaction("session-1");
        assertThat(querySingleString(jdbcUrl), is("NEW"));
    }
    
    @Test
    void assertCreateSavepoint() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-savepoint");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        adapter.beginTransaction("session-1", "logic_db");
        assertDoesNotThrow(() -> adapter.createSavepoint("session-1", "sp_1"));
        adapter.closeSession("session-1");
    }
    
    @Test
    void assertRollbackToSavepoint() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-rollback-savepoint");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        adapter.beginTransaction("session-1", "logic_db");
        adapter.execute(createExecutionRequest("logic_db", "UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"),
                new StatementClassifier().classify("UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"));
        adapter.createSavepoint("session-1", "sp_1");
        adapter.execute(createExecutionRequest("logic_db", "UPDATE orders SET status = 'DONE' WHERE order_id = 1"),
                new StatementClassifier().classify("UPDATE orders SET status = 'DONE' WHERE order_id = 1"));
        adapter.rollbackToSavepoint("session-1", "sp_1");
        adapter.commitTransaction("session-1");
        assertThat(querySingleString(jdbcUrl), is("PROCESSING"));
    }
    
    @Test
    void assertReleaseSavepoint() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-release-savepoint");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        adapter.beginTransaction("session-1", "logic_db");
        adapter.createSavepoint("session-1", "sp_1");
        assertDoesNotThrow(() -> adapter.releaseSavepoint("session-1", "sp_1"));
        adapter.closeSession("session-1");
    }
    
    @Test
    void assertCloseSession() throws SQLException {
        String jdbcUrl = createJdbcUrl("adapter-close");
        initializeDatabase(jdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", jdbcUrl));
        adapter.beginTransaction("session-1", "logic_db");
        adapter.execute(createExecutionRequest("logic_db", "UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"),
                new StatementClassifier().classify("UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"));
        adapter.closeSession("session-1");
        assertThat(querySingleString(jdbcUrl), is("NEW"));
    }
    
    @Test
    void assertExecuteRejectsCrossDatabaseTransactionSwitch() throws SQLException {
        String firstJdbcUrl = createJdbcUrl("adapter-cross-first");
        String secondJdbcUrl = createJdbcUrl("adapter-cross-second");
        initializeDatabase(firstJdbcUrl);
        initializeDatabase(secondJdbcUrl);
        ShardingSphereExecutionAdapter adapter = createAdapter(Map.of("logic_db", firstJdbcUrl, "analytics_db", secondJdbcUrl));
        adapter.beginTransaction("session-1", "logic_db");
        ExecuteQueryResponse actual = adapter.execute(createExecutionRequest("analytics_db", "SELECT status FROM orders ORDER BY order_id"),
                new StatementClassifier().classify("SELECT status FROM orders ORDER BY order_id"));
        assertFalse(actual.isSuccessful());
        assertThat(actual.getError().orElseThrow().getErrorCode().name(), is("TRANSACTION_STATE_ERROR"));
        adapter.closeSession("session-1");
    }
    
    private ShardingSphereExecutionAdapter createAdapter(final Map<String, String> jdbcUrls) {
        Map<String, ConnectionProvider> connectionProviders = new LinkedHashMap<>();
        for (Entry<String, String> entry : jdbcUrls.entrySet()) {
            connectionProviders.put(entry.getKey(), () -> DriverManager.getConnection(entry.getValue()));
        }
        return new ShardingSphereExecutionAdapter(connectionProviders);
    }
    
    private ExecutionRequest createExecutionRequest(final String databaseName, final String sql) {
        return new ExecutionRequest("session-1", databaseName, "public", sql, 10, 1000, new DatabaseExecutionBackend(Map.of(), Map.of()));
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
