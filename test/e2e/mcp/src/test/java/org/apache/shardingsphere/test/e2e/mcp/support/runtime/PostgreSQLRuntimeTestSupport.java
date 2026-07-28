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
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Map;

/**
 * E2E-local PostgreSQL-backed runtime test support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLRuntimeTestSupport {
    
    private static final Duration JDBC_READY_TIMEOUT = Duration.ofSeconds(90L);
    
    private static final String DATABASE_NAME = "orders";
    
    private static final String USERNAME = "mcp";
    
    private static final String PASSWORD = "mcp";
    
    /**
     * Create one PostgreSQL runtime container.
     *
     * @return PostgreSQL runtime container
     * @throws IllegalStateException when the configured image is empty
     */
    public static GenericContainer<?> createContainer() {
        String image = EnvironmentPropertiesLoader.loadProperties().getProperty("mcp.e2e.postgresql.image", "").trim();
        if (image.isEmpty()) {
            throw new IllegalStateException("MCP E2E PostgreSQL image property `mcp.e2e.postgresql.image` is required.");
        }
        return new GenericContainer<>(DockerImageName.parse(image))
                .withEnv("POSTGRES_DB", DATABASE_NAME)
                .withEnv("POSTGRES_USER", USERNAME)
                .withEnv("POSTGRES_PASSWORD", PASSWORD)
                .withExposedPorts(5432)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2L));
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
     * Create runtime databases for the PostgreSQL-backed runtime.
     *
     * @param container running container
     * @param logicalDatabase logical database name
     * @return runtime databases
     */
    public static Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases(final GenericContainer<?> container, final String logicalDatabase) {
        return Map.of(logicalDatabase, new RuntimeDatabaseConfiguration(createJdbcUrl(container), USERNAME, PASSWORD, "org.postgresql.Driver"));
    }
    
    /**
     * Initialize PostgreSQL schemas that exercise native schema metadata.
     *
     * @param container running container
     * @throws SQLException SQL exception
     */
    public static void initializeDatabase(final GenericContainer<?> container) throws SQLException {
        try (
                Connection connection = getConnection(container);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS tenant");
            statement.execute("CREATE TABLE IF NOT EXISTS public.orders (order_id INTEGER PRIMARY KEY, status VARCHAR(32), tenant_code VARCHAR(16) NOT NULL)");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_tenant_order ON public.orders (tenant_code, order_id)");
            statement.execute("INSERT INTO public.orders (order_id, status, tenant_code) VALUES (1, 'NEW', 'tenant-a') ON CONFLICT (order_id) "
                    + "DO UPDATE SET status = EXCLUDED.status, tenant_code = EXCLUDED.tenant_code");
            statement.execute("CREATE OR REPLACE VIEW public.active_orders AS SELECT order_id, status FROM public.orders WHERE status <> 'DONE'");
            statement.execute("CREATE TABLE IF NOT EXISTS tenant.orders (order_id INTEGER PRIMARY KEY, tenant_note VARCHAR(32) NOT NULL)");
            statement.execute("INSERT INTO tenant.orders (order_id, tenant_note) VALUES (1, 'tenant-schema') ON CONFLICT (order_id) "
                    + "DO UPDATE SET tenant_note = EXCLUDED.tenant_note");
        }
    }
    
    private static Connection getConnection(final GenericContainer<?> container) throws SQLException {
        String jdbcUrl = createJdbcUrl(container);
        try {
            return new ReadinessProbe(JDBC_READY_TIMEOUT.toMillis(), 250L, 1000L).waitUntilReady(
                    () -> getConnectionIfReady(jdbcUrl),
                    (cause, attemptCount, elapsedMillis) -> new SQLException(String.format(
                            "PostgreSQL JDBC connection did not become ready after %d attempt(s), elapsedMillis=%d.", attemptCount, elapsedMillis), cause));
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for PostgreSQL JDBC readiness.", ex);
        }
    }
    
    private static ReadinessProbe.ReadinessResult<Connection> getConnectionIfReady(final String jdbcUrl) {
        try {
            return ReadinessProbe.ReadinessResult.ready(DriverManager.getConnection(jdbcUrl, USERNAME, PASSWORD));
        } catch (final SQLException ex) {
            return ReadinessProbe.ReadinessResult.retry(ex);
        }
    }
    
    private static String createJdbcUrl(final GenericContainer<?> container) {
        return String.format("jdbc:postgresql://%s:%d/%s", container.getHost(), container.getMappedPort(5432), DATABASE_NAME);
    }
}
