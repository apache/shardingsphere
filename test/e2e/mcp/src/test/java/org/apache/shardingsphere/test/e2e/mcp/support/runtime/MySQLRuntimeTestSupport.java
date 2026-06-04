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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * E2E-local MySQL-backed runtime test support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLRuntimeTestSupport {
    
    private static final Duration DEFAULT_JDBC_READY_TIMEOUT = Duration.ofSeconds(90);
    
    private static final String MYSQL_READY_LOG_PATTERN = ".*ready for connections.*port: 3306.*\\n";
    
    private static final long JDBC_READY_INITIAL_INTERVAL_MILLIS = 250L;
    
    private static final long JDBC_READY_MAX_INTERVAL_MILLIS = 1000L;
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM %s.orders";
    
    private static final String DATABASE_NAME = "orders";
    
    private static final String USERNAME = "mcp";
    
    private static final String PASSWORD = "mcp";
    
    private static final String LOGIC_DATABASE_USERNAME = "mcp_logic";
    
    private static final String ANALYTICS_DATABASE_USERNAME = "mcp_analytics";
    
    private static final String WAREHOUSE_USERNAME = "mcp_warehouse";
    
    private static final String ROOT_PASSWORD = "root";
    
    /**
     * Create one MySQL runtime container.
     *
     * @return MySQL runtime container
     */
    public static GenericContainer<?> createContainer() {
        return new GenericContainer<>(DockerImageName.parse(getMySQLImage()))
                .withEnv("MYSQL_ROOT_PASSWORD", ROOT_PASSWORD)
                .withEnv("MYSQL_DATABASE", DATABASE_NAME)
                .withEnv("MYSQL_USER", USERNAME)
                .withEnv("MYSQL_PASSWORD", PASSWORD)
                .withExposedPorts(3306)
                .waitingFor(Wait.forLogMessage(MYSQL_READY_LOG_PATTERN, 1))
                .withStartupTimeout(Duration.ofMinutes(2));
    }
    
    private static String getMySQLImage() {
        return getMySQLImage(EnvironmentPropertiesLoader.loadProperties());
    }
    
    static String getMySQLImage(final Properties props) {
        String result = props.getProperty("mcp.e2e.mysql.image", "").trim();
        if (result.isEmpty()) {
            throw new IllegalStateException("MCP E2E MySQL image property `mcp.e2e.mysql.image` is required.");
        }
        return result;
    }
    
    /**
     * Check whether Docker is available for Testcontainers-backed tests.
     *
     * @return whether Docker is available
     */
    public static boolean isDockerAvailable() {
        return getDockerUnavailableReason().isEmpty();
    }
    
    /**
     * Get Docker readiness diagnostic when Testcontainers cannot use Docker.
     *
     * @return Docker unavailable reason
     */
    public static Optional<String> getDockerUnavailableReason() {
        try {
            return DockerClientFactory.instance().isDockerAvailable()
                    ? Optional.empty()
                    : Optional.of("Testcontainers Docker client reported Docker unavailable.");
        } catch (final IllegalStateException ex) {
            return Optional.of(createDockerUnavailableReason(ex));
        }
    }
    
    /**
     * Create Docker-required message with bounded readiness diagnostics.
     *
     * @param scenarioMessage scenario message
     * @return Docker-required message
     */
    public static String createDockerRequiredMessage(final String scenarioMessage) {
        return createDockerRequiredMessage(scenarioMessage, getDockerUnavailableReason().orElse(""));
    }
    
    static String createDockerRequiredMessage(final String scenarioMessage, final String unavailableReason) {
        return unavailableReason.isEmpty() ? scenarioMessage : scenarioMessage + " Docker readiness diagnostic: " + unavailableReason;
    }
    
    private static String createDockerUnavailableReason(final IllegalStateException ex) {
        return null == ex.getMessage() || ex.getMessage().isBlank()
                ? "Testcontainers Docker availability check failed without a message."
                : ex.getMessage();
    }
    
    /**
     * Create runtime databases for the MySQL-backed runtime.
     *
     * @param container running container
     * @param logicalDatabase logical database name
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final GenericContainer<?> container, final String logicalDatabase) {
        return Map.of(logicalDatabase, createRuntimeDatabaseConfiguration(container, DATABASE_NAME));
    }
    
    /**
     * Create runtime databases for a Dockerized MCP runtime that connects back to the host-mapped MySQL port.
     *
     * @param container running container
     * @param logicalDatabase logical database name
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createDockerHostRuntimeDatabases(final GenericContainer<?> container, final String logicalDatabase) {
        return Map.of(logicalDatabase, createRuntimeDatabaseConfiguration("host.docker.internal", container.getMappedPort(3306), DATABASE_NAME));
    }
    
    /**
     * Create prepared runtime databases for MySQL-backed programmatic E2E tests.
     *
     * @param container running container
     * @return prepared runtime databases
     * @throws SQLException SQL exception
     */
    public static Map<String, RuntimeDatabaseConfiguration> createPreparedProgrammaticRuntimeDatabases(final GenericContainer<?> container) throws SQLException {
        initializeProgrammaticDatabases(container);
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(3, 1F);
        result.put("logic_db", createRuntimeDatabaseConfiguration(container, "logic_db", LOGIC_DATABASE_USERNAME, PASSWORD));
        result.put("analytics_db", createRuntimeDatabaseConfiguration(container, "analytics_db", ANALYTICS_DATABASE_USERNAME, PASSWORD));
        result.put("warehouse", createRuntimeDatabaseConfiguration(container, "warehouse", WAREHOUSE_USERNAME, PASSWORD));
        return result;
    }
    
    /**
     * Create LLM runtime fixture.
     *
     * @param logicalDatabase logical database
     * @return fixture
     * @throws SQLException SQL exception
     */
    public static LLMMySQLRuntimeFixture createLLMRuntimeFixture(final String logicalDatabase) throws SQLException {
        GenericContainer<?> container = createContainer();
        container.start();
        initializeDatabase(container);
        String schemaName = detectSchema(container);
        String physicalSchemaName = schemaName.isEmpty() ? DATABASE_NAME : schemaName;
        int totalOrders = querySingleInt(container, String.format(COUNT_ORDERS_SQL, physicalSchemaName));
        return new LLMMySQLRuntimeFixture(container, logicalDatabase, totalOrders, createRuntimeDatabases(container, logicalDatabase));
    }
    
    /**
     * Initialize the shared MySQL runtime schema.
     *
     * @param container running container
     * @throws SQLException SQL exception
     */
    public static void initializeDatabase(final GenericContainer<?> container) throws SQLException {
        executeStatements(container, DATABASE_NAME,
                "CREATE TABLE IF NOT EXISTS orders (order_id INT PRIMARY KEY, status VARCHAR(32), amount INT)",
                "CREATE TABLE IF NOT EXISTS order_items (item_id INT PRIMARY KEY, order_id INT, sku VARCHAR(64))",
                "INSERT INTO orders (order_id, status, amount) VALUES (1, 'NEW', 10) ON DUPLICATE KEY UPDATE status = VALUES(status), amount = VALUES(amount)",
                "INSERT INTO orders (order_id, status, amount) VALUES (2, 'DONE', 20) ON DUPLICATE KEY UPDATE status = VALUES(status), amount = VALUES(amount)",
                "INSERT INTO order_items (item_id, order_id, sku) VALUES (1, 1, 'sku-1') ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), sku = VALUES(sku)",
                "CREATE OR REPLACE VIEW active_orders AS SELECT order_id, status FROM orders WHERE status <> 'DONE'",
                "CREATE INDEX idx_orders_status ON orders(status)");
    }
    
    private static void initializeProgrammaticDatabases(final GenericContainer<?> container) throws SQLException {
        executeRootStatements(container,
                "CREATE DATABASE IF NOT EXISTS logic_db",
                "CREATE DATABASE IF NOT EXISTS analytics_db",
                "CREATE DATABASE IF NOT EXISTS warehouse",
                "CREATE USER IF NOT EXISTS 'mcp_logic'@'%' IDENTIFIED BY 'mcp'",
                "CREATE USER IF NOT EXISTS 'mcp_analytics'@'%' IDENTIFIED BY 'mcp'",
                "CREATE USER IF NOT EXISTS 'mcp_warehouse'@'%' IDENTIFIED BY 'mcp'",
                "GRANT ALL PRIVILEGES ON logic_db.* TO 'mcp'@'%'",
                "GRANT ALL PRIVILEGES ON analytics_db.* TO 'mcp'@'%'",
                "GRANT ALL PRIVILEGES ON warehouse.* TO 'mcp'@'%'",
                "GRANT ALL PRIVILEGES ON logic_db.* TO 'mcp_logic'@'%'",
                "GRANT ALL PRIVILEGES ON analytics_db.* TO 'mcp_analytics'@'%'",
                "GRANT ALL PRIVILEGES ON warehouse.* TO 'mcp_warehouse'@'%'",
                "FLUSH PRIVILEGES");
        executeStatements(container, "logic_db",
                "CREATE TABLE IF NOT EXISTS orders (order_id INT PRIMARY KEY, status VARCHAR(32), amount INT)",
                "CREATE TABLE IF NOT EXISTS order_items (item_id INT PRIMARY KEY, order_id INT, sku VARCHAR(64))",
                "INSERT INTO orders (order_id, status, amount) VALUES (1, 'NEW', 10) ON DUPLICATE KEY UPDATE status = VALUES(status), amount = VALUES(amount)",
                "INSERT INTO orders (order_id, status, amount) VALUES (2, 'DONE', 20) ON DUPLICATE KEY UPDATE status = VALUES(status), amount = VALUES(amount)",
                "INSERT INTO order_items (item_id, order_id, sku) VALUES (1, 1, 'sku-1') ON DUPLICATE KEY UPDATE order_id = VALUES(order_id), sku = VALUES(sku)",
                "CREATE OR REPLACE VIEW active_orders AS SELECT order_id, status FROM orders WHERE status <> 'DONE'",
                "CREATE INDEX idx_orders_status ON orders(status)");
        executeStatements(container, "analytics_db",
                "CREATE TABLE IF NOT EXISTS metrics (metric_id INT PRIMARY KEY, metric_name VARCHAR(32))",
                "INSERT INTO metrics (metric_id, metric_name) VALUES (10, 'cpu') ON DUPLICATE KEY UPDATE metric_name = VALUES(metric_name)",
                "INSERT INTO metrics (metric_id, metric_name) VALUES (20, 'memory') ON DUPLICATE KEY UPDATE metric_name = VALUES(metric_name)");
        executeStatements(container, "warehouse",
                "CREATE TABLE IF NOT EXISTS facts (fact_id INT PRIMARY KEY, total INT)",
                "INSERT INTO facts (fact_id, total) VALUES (100, 1) ON DUPLICATE KEY UPDATE total = VALUES(total)",
                "INSERT INTO facts (fact_id, total) VALUES (200, 2) ON DUPLICATE KEY UPDATE total = VALUES(total)");
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
                Connection connection = getConnection(container, DATABASE_NAME)) {
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
                Connection connection = getConnection(container, DATABASE_NAME);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
    
    /**
     * Query one single string value.
     *
     * @param container running container
     * @param sql SQL
     * @return queried value
     * @throws SQLException SQL exception
     */
    public static String querySingleString(final GenericContainer<?> container, final String sql) throws SQLException {
        try (
                Connection connection = getConnection(container, DATABASE_NAME);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getString(1);
        }
    }
    
    private static void executeStatements(final GenericContainer<?> container, final String databaseName, final String... sqls) throws SQLException {
        try (
                Connection connection = getConnection(container, databaseName);
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
    
    private static Connection getConnection(final GenericContainer<?> container, final String databaseName) throws SQLException {
        String jdbcUrl = createJdbcUrl(container.getHost(), container.getMappedPort(3306), databaseName);
        String configuredJdbcReadyTimeoutSeconds = EnvironmentPropertiesLoader.loadProperties().getProperty("mcp.e2e.mysql.ready-timeout-seconds", "").trim();
        long jdbcReadyTimeoutMillis;
        try {
            if (configuredJdbcReadyTimeoutSeconds.isEmpty()) {
                jdbcReadyTimeoutMillis = DEFAULT_JDBC_READY_TIMEOUT.toMillis();
            } else {
                int parsedJdbcReadyTimeoutSeconds = Integer.parseInt(configuredJdbcReadyTimeoutSeconds);
                if (0 >= parsedJdbcReadyTimeoutSeconds) {
                    throw new IllegalArgumentException("MCP E2E MySQL JDBC readiness timeout must be positive.");
                }
                jdbcReadyTimeoutMillis = Duration.ofSeconds(parsedJdbcReadyTimeoutSeconds).toMillis();
            }
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("MCP E2E MySQL JDBC readiness timeout must be an integer.", ex);
        }
        try {
            return new ReadinessProbe(jdbcReadyTimeoutMillis, JDBC_READY_INITIAL_INTERVAL_MILLIS, JDBC_READY_MAX_INTERVAL_MILLIS)
                    .waitUntilReady(() -> getConnectionIfReady(jdbcUrl),
                            (cause, attemptCount, elapsedMillis) -> createJdbcReadyException(cause, attemptCount, elapsedMillis, jdbcReadyTimeoutMillis));
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for MySQL JDBC readiness.", ex);
        }
    }
    
    private static ReadinessProbe.ReadinessResult<Connection> getConnectionIfReady(final String jdbcUrl) {
        try {
            return ReadinessProbe.ReadinessResult.ready(DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD));
        } catch (final SQLException ex) {
            return ReadinessProbe.ReadinessResult.retry(ex);
        }
    }
    
    private static SQLException createJdbcReadyException(final Exception cause, final int attemptCount, final long elapsedMillis, final long jdbcReadyTimeoutMillis) {
        String result = String.format("MySQL JDBC connection did not become ready after %d attempt(s), elapsedMillis=%d, timeoutMillis=%d.",
                attemptCount, elapsedMillis, jdbcReadyTimeoutMillis);
        return null == cause || null == cause.getMessage() || cause.getMessage().isBlank()
                ? new SQLException(result)
                : new SQLException(result + " Last readiness failure: " + cause.getMessage(), cause);
    }
    
    private static String createJdbcUrl(final String host, final int port, final String databaseName) {
        String databasePath = databaseName.isEmpty() ? "/" : "/" + databaseName;
        return String.format("jdbc:mysql://%s:%d%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectTimeout=3000&socketTimeout=3000",
                host, port, databasePath);
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final GenericContainer<?> container, final String databaseName) {
        return createRuntimeDatabaseConfiguration(container.getHost(), container.getMappedPort(3306), databaseName);
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String host, final int port, final String databaseName) {
        return new RuntimeDatabaseConfiguration("MySQL", createJdbcUrl(host, port, databaseName), USERNAME, PASSWORD, "com.mysql.cj.jdbc.Driver");
    }
    
    private static RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final GenericContainer<?> container, final String databaseName, final String username, final String password) {
        return new RuntimeDatabaseConfiguration("MySQL", createJdbcUrl(container.getHost(), container.getMappedPort(3306), databaseName), username, password, "com.mysql.cj.jdbc.Driver");
    }
    
    private static void executeRootStatements(final GenericContainer<?> container, final String... sqls) throws SQLException {
        try (
                Connection connection = DriverManager.getConnection(createJdbcUrl(container.getHost(), container.getMappedPort(3306), ""), "root", ROOT_PASSWORD);
                Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.execute(each);
            }
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    public static final class LLMMySQLRuntimeFixture implements AutoCloseable {
        
        private final GenericContainer<?> container;
        
        private final String schemaName;
        
        private final int totalOrders;
        
        private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
        
        @Override
        public void close() {
            container.stop();
        }
    }
}
