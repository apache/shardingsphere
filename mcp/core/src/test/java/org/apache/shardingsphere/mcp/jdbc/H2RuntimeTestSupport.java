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

package org.apache.shardingsphere.mcp.jdbc;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared H2-backed runtime test support.
 */
public final class H2RuntimeTestSupport {
    
    private static final String COUNT_ORDERS_JDBC_SQL = "SELECT COUNT(*) AS total_orders FROM public.orders";
    
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
        return createJdbcUrl(tempDir, databaseName, H2AccessMode.SINGLE_PROCESS);
    }
    
    /**
     * Create one file-backed H2 JDBC URL in MySQL compatibility mode.
     *
     * @param tempDir temp directory
     * @param databaseName database name
     * @param accessMode access mode
     * @return JDBC URL
     */
    public static String createJdbcUrl(final Path tempDir, final String databaseName, final H2AccessMode accessMode) {
        return String.format("jdbc:h2:file:%s;MODE=MySQL%s;DATABASE_TO_UPPER=false", tempDir.resolve(databaseName).toAbsolutePath(), accessMode.getJdbcParameter());
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
                "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status)",
                "CREATE SEQUENCE IF NOT EXISTS order_seq START WITH 1000");
    }
    
    /**
     * Create prepared runtime databases for programmatic E2E tests.
     *
     * @param tempDir temp directory
     * @param accessMode H2 access mode
     * @return prepared runtime databases
     * @throws SQLException SQL exception
     */
    public static Map<String, RuntimeDatabaseConfiguration> createPreparedProgrammaticRuntimeDatabases(final Path tempDir, final H2AccessMode accessMode) throws SQLException {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(3, 1F);
        result.put("logic_db", createRuntimeDatabaseConfiguration(tempDir, "abstract-mcp-e2e-logic", "public", accessMode));
        result.put("analytics_db", createRuntimeDatabaseConfiguration(tempDir, "abstract-mcp-e2e-analytics", "public", accessMode));
        result.put("warehouse", createRuntimeDatabaseConfiguration(tempDir, "abstract-mcp-e2e-warehouse", "warehouse", accessMode));
        initializeDatabase(result.get("logic_db").getJdbcUrl());
        executeStatements(result.get("analytics_db").getJdbcUrl(),
                "CREATE SCHEMA IF NOT EXISTS public",
                "SET SCHEMA public",
                "CREATE TABLE IF NOT EXISTS metrics (metric_id INT PRIMARY KEY, metric_name VARCHAR(32))",
                "MERGE INTO metrics (metric_id, metric_name) KEY (metric_id) VALUES (10, 'cpu')",
                "MERGE INTO metrics (metric_id, metric_name) KEY (metric_id) VALUES (20, 'memory')");
        executeStatements(result.get("warehouse").getJdbcUrl(),
                "CREATE SCHEMA IF NOT EXISTS warehouse",
                "SET SCHEMA warehouse",
                "CREATE TABLE IF NOT EXISTS facts (fact_id INT PRIMARY KEY, total INT)",
                "MERGE INTO facts (fact_id, total) KEY (fact_id) VALUES (100, 1)",
                "MERGE INTO facts (fact_id, total) KEY (fact_id) VALUES (200, 2)");
        return result;
    }
    
    /**
     * Create one single-database H2 fixture for LLM E2E tests.
     *
     * @param tempDir temp directory
     * @param databaseName database name
     * @param logicalDatabase logical database
     * @param accessMode H2 access mode
     * @return H2 fixture
     * @throws SQLException SQL exception
     */
    public static LLMH2RuntimeFixture createLLMRuntimeFixture(final Path tempDir, final String databaseName,
                                                              final String logicalDatabase, final H2AccessMode accessMode) throws SQLException {
        String jdbcUrl = createJdbcUrl(tempDir, databaseName, accessMode);
        initializeDatabase(jdbcUrl);
        return new LLMH2RuntimeFixture(querySingleInt(jdbcUrl, COUNT_ORDERS_JDBC_SQL), createRuntimeDatabases(logicalDatabase, jdbcUrl));
    }
    
    /**
     * Create one multi-database H2 fixture for LLM E2E tests.
     *
     * @param tempDir temp directory
     * @param logicalDatabase logical database
     * @param analyticsDatabase analytics database
     * @param accessMode H2 access mode
     * @return H2 fixture
     * @throws SQLException SQL exception
     */
    public static LLMH2RuntimeFixture createMultiDatabaseLLMRuntimeFixture(final Path tempDir, final String logicalDatabase,
                                                                           final String analyticsDatabase, final H2AccessMode accessMode) throws SQLException {
        String logicalJdbcUrl = createJdbcUrl(tempDir, logicalDatabase + "-llm", accessMode);
        String analyticsJdbcUrl = createJdbcUrl(tempDir, analyticsDatabase + "-llm", accessMode);
        initializeDatabase(logicalJdbcUrl);
        initializeDatabase(analyticsJdbcUrl);
        executeStatements(analyticsJdbcUrl,
                "MERGE INTO orders (order_id, status, amount) KEY (order_id) VALUES (3, 'ARCHIVED', 30)",
                "MERGE INTO orders (order_id, status, amount) KEY (order_id) VALUES (4, 'ARCHIVED', 40)");
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = new LinkedHashMap<>(2, 1F);
        runtimeDatabases.put(logicalDatabase, new RuntimeDatabaseConfiguration("H2", logicalJdbcUrl, "", "", "org.h2.Driver"));
        runtimeDatabases.put(analyticsDatabase, new RuntimeDatabaseConfiguration("H2", analyticsJdbcUrl, "", "", "org.h2.Driver"));
        return new LLMH2RuntimeFixture(querySingleInt(logicalJdbcUrl, COUNT_ORDERS_JDBC_SQL), runtimeDatabases);
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
    
    /**
     * Query one single integer value.
     *
     * @param jdbcUrl JDBC URL
     * @param sql SQL
     * @return queried value
     * @throws SQLException SQL exception
     */
    public static int querySingleInt(final String jdbcUrl, final String sql) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final Path tempDir, final String databaseName,
                                                                                   final String defaultSchema, final H2AccessMode accessMode) {
        String jdbcUrl = String.format("%s;INIT=CREATE SCHEMA IF NOT EXISTS %s\\;SET SCHEMA %s",
                createJdbcUrl(tempDir, databaseName, accessMode), defaultSchema, defaultSchema);
        return new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver");
    }
    
    public record LLMH2RuntimeFixture(int totalOrders, Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
    }
    
    public enum H2AccessMode {
        
        SINGLE_PROCESS(";DB_CLOSE_DELAY=-1"),
        
        MULTI_PROCESS(";AUTO_SERVER=TRUE");
        
        private final String jdbcParameter;
        
        H2AccessMode(final String jdbcParameter) {
            this.jdbcParameter = jdbcParameter;
        }
        
        private String getJdbcParameter() {
            return jdbcParameter;
        }
    }
}
