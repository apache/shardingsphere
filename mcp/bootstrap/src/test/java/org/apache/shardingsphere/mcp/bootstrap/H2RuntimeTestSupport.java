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

package org.apache.shardingsphere.mcp.bootstrap;

import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Bootstrap-local H2-backed runtime test support.
 */
public final class H2RuntimeTestSupport {
    
    private H2RuntimeTestSupport() {
    }
    
    /**
     * Create one file-backed H2 JDBC URL in MySQL compatibility mode.
     *
     * @param tempDir temp directory
     * @param databaseName database name
     * @return JDBC URL
     */
    public static String createJdbcUrl(final Path tempDir, final String databaseName) {
        return String.format("jdbc:h2:file:%s;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", tempDir.resolve(databaseName).toAbsolutePath());
    }
    
    /**
     * Create runtime databases for the H2-backed runtime.
     *
     * @param logicalDatabase logical database name
     * @param jdbcUrl JDBC URL
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final String logicalDatabase, final String jdbcUrl) {
        return Map.of(logicalDatabase, new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver"));
    }
    
    /**
     * Initialize the shared H2 runtime schema.
     *
     * @param jdbcUrl JDBC URL
     * @throws SQLException SQL exception
     */
    public static void initializeDatabase(final String jdbcUrl) throws SQLException {
        executeStatements(jdbcUrl,
                "CREATE SCHEMA IF NOT EXISTS public",
                "SET SCHEMA public",
                "CREATE TABLE IF NOT EXISTS orders (order_id INT PRIMARY KEY, status VARCHAR(32), amount INT)",
                "CREATE TABLE IF NOT EXISTS order_items (item_id INT PRIMARY KEY, order_id INT, sku VARCHAR(64))",
                "MERGE INTO orders (order_id, status, amount) KEY (order_id) VALUES (1, 'NEW', 10)",
                "MERGE INTO orders (order_id, status, amount) KEY (order_id) VALUES (2, 'DONE', 20)",
                "MERGE INTO order_items (item_id, order_id, sku) KEY (item_id) VALUES (1, 1, 'sku-1')",
                "CREATE VIEW IF NOT EXISTS active_orders AS SELECT order_id, status FROM orders WHERE status <> 'DONE'",
                "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)");
    }
    
    /**
     * Execute one or more SQL statements.
     *
     * @param jdbcUrl JDBC URL
     * @param sqls SQL statements
     * @throws SQLException SQL exception
     */
    public static void executeStatements(final String jdbcUrl, final String... sqls) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.execute(each);
            }
        }
    }
    
    /**
     * Query one single string value.
     *
     * @param jdbcUrl JDBC URL
     * @param sql SQL
     * @return queried value
     * @throws SQLException SQL exception
     */
    public static String querySingleString(final String jdbcUrl, final String sql) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }
}
