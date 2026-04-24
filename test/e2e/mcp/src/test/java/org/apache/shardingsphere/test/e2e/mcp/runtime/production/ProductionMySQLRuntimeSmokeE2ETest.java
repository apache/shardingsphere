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
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class ProductionMySQLRuntimeSmokeE2ETest extends AbstractTransportParameterizedProductionRuntimeE2ETest {
    
    private static final String LOGICAL_DATABASE_NAME = "logic_db";
    
    private static final String PHYSICAL_DATABASE_NAME = "orders";
    
    private GenericContainer<?> container;
    
    private String physicalSchemaName;
    
    @AfterEach
    void tearDownContainer() {
        if (null != container) {
            container.stop();
            container = null;
        }
        physicalSchemaName = null;
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(), "Docker is required for the MySQL-backed production runtime smoke test.");
        container = MySQLRuntimeTestSupport.createContainer();
        container.start();
        try {
            MySQLRuntimeTestSupport.initializeDatabase(container);
            String detectedSchemaName = MySQLRuntimeTestSupport.detectSchema(container);
            physicalSchemaName = detectedSchemaName.isEmpty() ? PHYSICAL_DATABASE_NAME : detectedSchemaName;
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return MySQLRuntimeTestSupport.createRuntimeDatabases(container, LOGICAL_DATABASE_NAME);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadCapabilitiesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertThat(String.valueOf(interactionClient.readResource("shardingsphere://databases/logic_db/capabilities").get("databaseType")), is("MySQL"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertListResourcesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertTrue(getResources(interactionClient.listResources()).stream().anyMatch(each -> "shardingsphere://capabilities".equals(each.get("uri"))));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadTableDetailWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/tables/orders", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME)));
            assertThat(items.size(), is(1));
            Map<String, Object> actualItem = items.get(0);
            assertThat(String.valueOf(actualItem.get("table")), is("orders"));
            assertThat(getNestedNames(actualItem, "columns", "column"), is(List.of("amount", "order_id", "status")));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertSearchMetadataTablesAndViewsWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.call("search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "order", "object_types", List.of("TABLE", "VIEW"))));
            assertThat(items.stream().map(each -> String.valueOf(each.get("name"))).toList(), is(List.of("order_items", "orders", "active_orders")));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadViewsWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/views", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME)));
            assertThat(items.size(), is(1));
            assertThat(String.valueOf(items.get(0).get("view")), is("active_orders"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadIndexesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<String> actualIndexNames = getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/tables/orders/indexes", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME)))
                    .stream().map(each -> String.valueOf(each.get("index"))).toList();
            assertThat(actualIndexNames, hasItems("PRIMARY", "idx_orders_status"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteSelectWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteUpdateWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            assertThat(String.valueOf(actual.get("result_kind")), is("update_count"));
            assertThat(String.valueOf(actual.get("affected_rows")), is("1"));
            assertThat(MySQLRuntimeTestSupport.querySingleString(container, String.format("SELECT status FROM %s.orders WHERE order_id = 1", physicalSchemaName)), is("PENDING"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectSequenceResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/sequences", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME));
            assertThat(String.valueOf(actual.get("error_code")), is("unsupported"));
            assertThat(String.valueOf(actual.get("message")), is("Sequence resources are not supported for the current database."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteRollbackWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> beginResponse = interactionClient.call("execute_query", Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "BEGIN"));
            interactionClient.call("execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            Map<String, Object> rollbackResponse = interactionClient.call("execute_query", Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "ROLLBACK"));
            assertThat(String.valueOf(beginResponse.get("message")), is("Transaction started."));
            assertThat(String.valueOf(rollbackResponse.get("message")), is("Transaction rolled back."));
            assertThat(MySQLRuntimeTestSupport.querySingleString(container, String.format("SELECT status FROM %s.orders WHERE order_id = 1", physicalSchemaName)), is("NEW"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertCloseRollsBackPendingTransactionWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("execute_query", Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "BEGIN"));
            interactionClient.call("execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            interactionClient.close();
            assertThat(MySQLRuntimeTestSupport.querySingleString(container, String.format("SELECT status FROM %s.orders WHERE order_id = 1", physicalSchemaName)), is("NEW"));
        }
    }
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isProductionMySQLEnabled() || MCPE2ECondition.isProductionMySQLStdioEnabled();
    }
    
    private static Stream<Arguments> transports() {
        Stream.Builder<Arguments> result = Stream.builder();
        if (MCPE2ECondition.isProductionMySQLEnabled()) {
            result.add(Arguments.of("http", RuntimeTransport.HTTP));
        }
        if (MCPE2ECondition.isProductionMySQLStdioEnabled()) {
            result.add(Arguments.of("stdio", RuntimeTransport.STDIO));
        }
        return result.build();
    }
}
