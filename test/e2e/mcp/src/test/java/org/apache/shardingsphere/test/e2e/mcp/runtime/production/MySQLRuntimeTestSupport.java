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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * E2E-local MySQL-backed runtime test support.
 */
public final class MySQLRuntimeTestSupport {
    
    private static final Duration JDBC_READY_TIMEOUT = Duration.ofSeconds(30);
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM %s.orders";
    
    private static final String DATABASE_NAME = "orders";
    
    private static final String USERNAME = "mcp";
    
    private static final String PASSWORD = "mcp";
    
    private static final String ROOT_PASSWORD = "root";
    
    private MySQLRuntimeTestSupport() {
    }
    
    /**
     * Create one MySQL runtime container.
     *
     * @return MySQL runtime container
     */
    public static GenericContainer<?> createContainer() {
        return new GenericContainer<>(DockerImageName.parse("mysql:8.0.36"))
                .withEnv("MYSQL_ROOT_PASSWORD", ROOT_PASSWORD)
                .withEnv("MYSQL_DATABASE", DATABASE_NAME)
                .withEnv("MYSQL_USER", USERNAME)
                .withEnv("MYSQL_PASSWORD", PASSWORD)
                .withExposedPorts(3306)
                .waitingFor(Wait.forLogMessage(".*ready for connections.*\\n", 2))
                .withStartupTimeout(Duration.ofMinutes(2));
    }
    
    /**
     * Check whether Docker is available for Testcontainers-backed tests.
     *
     * @return whether Docker is available
     */
    public static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (final IllegalStateException ignored) {
            return false;
        }
    }
    
    /**
     * Create runtime databases for the MySQL-backed runtime.
     *
     * @param container running container
     * @param logicalDatabase logical database name
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final GenericContainer<?> container, final String logicalDatabase) {
        return Map.of(logicalDatabase, new RuntimeDatabaseConfiguration("MySQL", createJdbcUrl(container), USERNAME, PASSWORD, "com.mysql.cj.jdbc.Driver"));
    }
    
    public static LLMMySQLRuntimeFixture createLLMRuntimeFixture(final String logicalDatabase) throws SQLException {
        final GenericContainer<?> container = createContainer();
        container.start();
        initializeDatabase(container);
        final String schemaName = detectSchema(container);
        final String actualSchemaName = schemaName.isEmpty() ? DATABASE_NAME : schemaName;
        final int totalOrders = querySingleInt(container, String.format(COUNT_ORDERS_SQL, actualSchemaName));
        return new LLMMySQLRuntimeFixture(container, actualSchemaName, totalOrders, createRuntimeDatabases(container, logicalDatabase));
    }
    
    /**
     * Initialize the shared MySQL runtime schema.
     *
     * @param container running container
     * @throws SQLException SQL exception
     */
    public static void initializeDatabase(final GenericContainer<?> container) throws SQLException {
        executeStatements(container,
                "CREATE TABLE IF NOT EXISTS orders (order_id INT PRIMARY KEY, status VARCHAR(32), amount INT)",
                "CREATE TABLE IF NOT EXISTS order_items (item_id INT PRIMARY KEY, order_id INT, sku VARCHAR(64))",
                "INSERT INTO orders (order_id, status, amount) VALUES (1, 'NEW', 10) ON DUPLICATE KEY UPDATE status = VALUES(status), amount = VALUES(amount)",
                "INSERT INTO orders (order_id, status, amount) VALUES (2, 'DONE', 20) ON DUPLICATE KEY UPDATE status = VALUES(status), amount = VALUES(amount)",
                "INSERT INTO order_items (item_id, order_id, sku) VALUES (1, 1, 'sku-1') ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), sku = VALUES(sku)",
                "CREATE OR REPLACE VIEW active_orders AS SELECT order_id, status FROM orders WHERE status <> 'DONE'",
                "CREATE INDEX idx_orders_status ON orders(status)");
    }
    
    /**
     * Detect the schema value surfaced by JDBC metadata for the orders table.
     *
     * @param container running container
     * @return schema value
     * @throws SQLException SQL exception
     */
    public static String detectSchema(final GenericContainer<?> container) throws SQLException {
        try (
                Connection connection = getConnection(container)) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, "orders", new String[]{"TABLE"})) {
                while (resultSet.next()) {
                    String result = Objects.toString(resultSet.getString("TABLE_SCHEM"), "").trim();
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
        }
        return "";
    }
    
    /**
     * Query one single integer value.
     *
     * @param container running container
     * @param sql SQL
     * @return queried value
     * @throws SQLException SQL exception
     */
    public static int querySingleInt(final GenericContainer<?> container, final String sql) throws SQLException {
        try (
                Connection connection = getConnection(container);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
    
    private static void executeStatements(final GenericContainer<?> container, final String... sqls) throws SQLException {
        try (
                Connection connection = getConnection(container);
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                try {
                    statement.execute(each);
                } catch (final SQLException ex) {
                    if (!isDuplicateIndex(ex)) {
                        throw ex;
                    }
                }
            }
        }
    }
    
    private static boolean isDuplicateIndex(final SQLException ex) {
        return 1061 == ex.getErrorCode();
    }
    
    private static Connection getConnection(final GenericContainer<?> container) throws SQLException {
        String jdbcUrl = createJdbcUrl(container);
        long deadline = System.currentTimeMillis() + JDBC_READY_TIMEOUT.toMillis();
        SQLException lastException = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                return DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD);
            } catch (final SQLException ex) {
                lastException = ex;
                sleepBeforeRetry();
            }
        }
        throw null == lastException ? new SQLException("MySQL JDBC connection did not become ready in time.") : lastException;
    }
    
    private static void sleepBeforeRetry() throws SQLException {
        try {
            Thread.sleep(1000L);
        } catch (final InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for MySQL JDBC readiness.", interruptedException);
        }
    }
    
    private static String createJdbcUrl(final GenericContainer<?> container) {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                container.getHost(), container.getMappedPort(3306), DATABASE_NAME);
    }
    
    public static final class LLMMySQLRuntimeFixture implements AutoCloseable {
        
        private final GenericContainer<?> container;
        
        private final String schemaName;
        
        private final int totalOrders;
        
        private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
        
        LLMMySQLRuntimeFixture(final GenericContainer<?> container, final String schemaName, final int totalOrders,
                               final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
            this.container = container;
            this.schemaName = schemaName;
            this.totalOrders = totalOrders;
            this.runtimeDatabases = runtimeDatabases;
        }
        
        public String schemaName() {
            return schemaName;
        }
        
        public int totalOrders() {
            return totalOrders;
        }
        
        public Map<String, RuntimeDatabaseConfiguration> runtimeDatabases() {
            return runtimeDatabases;
        }
        
        @Override
        public void close() {
            container.stop();
        }
    }
}
