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
import java.util.Optional;

/**
 * E2E-local MySQL-backed runtime test support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLRuntimeTestSupport {
    
    private static final Duration JDBC_READY_TIMEOUT = Duration.ofSeconds(30);
    
    private static final String MYSQL_READY_LOG_PATTERN = ".*ready for connections.*port: 3306.*\\n";
    
    private static final long JDBC_READY_INITIAL_INTERVAL_MILLIS = 250L;
    
    private static final long JDBC_READY_MAX_INTERVAL_MILLIS = 1000L;
    
    private static final String COUNT_ORDERS_SQL = "SELECT COUNT(*) AS total_orders FROM %s.orders";
    
    private static final String DATABASE_NAME = "orders";
    
    private static final String USERNAME = "mcp";
    
    private static final String PASSWORD = "mcp";
    
    private static final String ROOT_PASSWORD = "root";
    
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
                .waitingFor(Wait.forLogMessage(MYSQL_READY_LOG_PATTERN, 1))
                .withStartupTimeout(Duration.ofMinutes(2));
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
        return createDockerRequiredMessage(scenarioMessage, getDockerUnavailableReason());
    }
    
    static String createDockerRequiredMessage(final String scenarioMessage, final Optional<String> unavailableReason) {
        return unavailableReason.map(each -> scenarioMessage + " Docker readiness diagnostic: " + each).orElse(scenarioMessage);
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
        return Map.of(logicalDatabase, new RuntimeDatabaseConfiguration("MySQL", createJdbcUrl(container.getHost(), container.getMappedPort(3306)), USERNAME, PASSWORD, "com.mysql.cj.jdbc.Driver"));
    }
    
    /**
     * Create runtime databases for a Dockerized MCP runtime that connects back to the host-mapped MySQL port.
     *
     * @param container running container
     * @param logicalDatabase logical database name
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createDockerHostRuntimeDatabases(final GenericContainer<?> container, final String logicalDatabase) {
        return Map.of(logicalDatabase, new RuntimeDatabaseConfiguration("MySQL", createJdbcUrl("host.docker.internal", container.getMappedPort(3306)),
                USERNAME, PASSWORD, "com.mysql.cj.jdbc.Driver"));
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
                Connection connection = getConnection(container);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getString(1);
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
        String jdbcUrl = createJdbcUrl(container.getHost(), container.getMappedPort(3306));
        long startTime = System.currentTimeMillis();
        long deadline = System.currentTimeMillis() + JDBC_READY_TIMEOUT.toMillis();
        long intervalMillis = JDBC_READY_INITIAL_INTERVAL_MILLIS;
        int attemptCount = 0;
        SQLException lastException = null;
        while (System.currentTimeMillis() < deadline) {
            attemptCount++;
            try {
                return DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD);
            } catch (final SQLException ex) {
                lastException = ex;
                intervalMillis = sleepBeforeRetry(deadline, intervalMillis);
            }
        }
        throw createJdbcReadyException(lastException, attemptCount, startTime);
    }
    
    private static long sleepBeforeRetry(final long deadline, final long intervalMillis) throws SQLException {
        long remainingMillis = deadline - System.currentTimeMillis();
        if (0L >= remainingMillis) {
            return intervalMillis;
        }
        try {
            Thread.sleep(Math.min(intervalMillis, remainingMillis));
            return Math.min(JDBC_READY_MAX_INTERVAL_MILLIS, intervalMillis * 2L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for MySQL JDBC readiness.", ex);
        }
    }
    
    private static SQLException createJdbcReadyException(final SQLException cause, final int attemptCount, final long startTimeMillis) {
        String result = String.format("MySQL JDBC connection did not become ready after %d attempt(s), elapsedMillis=%d, timeoutMillis=%d.",
                attemptCount, System.currentTimeMillis() - startTimeMillis, JDBC_READY_TIMEOUT.toMillis());
        return null == cause || null == cause.getMessage() || cause.getMessage().isBlank()
                ? new SQLException(result)
                : new SQLException(result + " Last readiness failure: " + cause.getMessage(), cause);
    }
    
    private static String createJdbcUrl(final String host, final int port) {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectTimeout=3000&socketTimeout=3000",
                host, port, DATABASE_NAME);
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
