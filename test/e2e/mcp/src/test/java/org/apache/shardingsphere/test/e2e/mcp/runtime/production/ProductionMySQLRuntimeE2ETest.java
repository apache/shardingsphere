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

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.ProtocolVersions;
import org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPStdioLogbackConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class ProductionMySQLRuntimeE2ETest extends AbstractTransportParameterizedProductionRuntimeE2ETest {
    
    private static final String LOGICAL_DATABASE_NAME = "logic_db";
    
    private static final String PHYSICAL_DATABASE_NAME = "orders";
    
    private static final String MASK_PLAN_TOOL_NAME = "database_gateway_plan_mask_rule";
    
    private static final String STDIO_LOGBACK_CONFIG_FILE_NAME = "mcp-e2e-sdk-stdio-logback.xml";
    
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
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(),
                () -> MySQLRuntimeTestSupport.createDockerRequiredMessage("Docker is required for the MySQL-backed production runtime E2E test."));
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
            List<Map<String, Object>> items = getPayloadItems(interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "order", "object_types", List.of("table", "view"))));
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
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteExplainAnalyzeSelectWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "EXPLAIN ANALYZE SELECT * FROM orders WHERE order_id = 1", "max_rows", 10));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(String.valueOf(actual.get("statement_type")), is("EXPLAIN ANALYZE"));
            assertFalse(((List<?>) actual.get("rows")).isEmpty());
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectExplainAnalyzeUpdateFromReadOnlyToolWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql",
                            "EXPLAIN ANALYZE UPDATE orders SET status = 'DONE' WHERE order_id = 1"));
            assertThat(String.valueOf(actual.get("error_code")), is("unsupported"));
            assertThat(String.valueOf(actual.get("message")),
                    is("database_gateway_execute_query only supports read-only QUERY and EXPLAIN_ANALYZE statements. Use database_gateway_execute_update for side-effecting SQL."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectLockingReadFromReadOnlyToolWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT * FROM orders FOR UPDATE"));
            assertThat(String.valueOf(actual.get("error_code")), is("unsupported"));
            assertThat(String.valueOf(actual.get("message")), is("Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectLockingReadFromUpdateToolWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("SELECT * FROM orders FOR UPDATE"));
            assertThat(String.valueOf(actual.get("error_code")), is("unsupported"));
            assertThat(String.valueOf(actual.get("message")), is("Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertElicitMaskPlanningWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException {
        useTransport(transport);
        List<McpSchema.ElicitRequest> actualElicitationRequests = new CopyOnWriteArrayList<>();
        try (McpSyncClient client = createElicitationClient(transport, actualElicitationRequests)) {
            client.initialize();
            McpSchema.CallToolResult actual = client.callTool(new McpSchema.CallToolRequest(MASK_PLAN_TOOL_NAME, Map.of(
                    "database", LOGICAL_DATABASE_NAME,
                    "schema", LOGICAL_DATABASE_NAME,
                    "table", "orders",
                    "column", "status",
                    "operation_type", "create",
                    "algorithm_type", "MASK_FROM_X_TO_Y")));
            Map<String, Object> actualPayload = castStructuredContent(actual.structuredContent());
            assertThat(String.valueOf(actualPayload.get("status")), is("planned"));
            assertThat(String.valueOf(actualPayload.get("current_step")), is("review"));
            assertThat(String.valueOf(castToMap(castToMap(actualPayload.get("masked_property_preview")).get("primary")).get("from-x")), is("1"));
            assertThat(String.valueOf(castToMap(castToMap(actualPayload.get("masked_property_preview")).get("primary")).get("to-y")), is("3"));
            assertElicitationRequest(actualElicitationRequests);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteUpdateWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
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
            Map<String, Object> beginResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            Map<String, Object> rollbackResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("ROLLBACK"));
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
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
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
    
    private static Map<String, Object> createExecuteUpdateArguments(final String sql) {
        return Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", sql, "execution_mode", "execute");
    }
    
    private McpSyncClient createElicitationClient(final RuntimeTransport transport, final List<McpSchema.ElicitRequest> elicitationRequests) throws IOException {
        return McpClient.sync(createClientTransport(transport))
                .clientInfo(new McpSchema.Implementation("mcp-e2e-elicitation", "MCP E2E Elicitation", "1.0.0"))
                .capabilities(McpSchema.ClientCapabilities.builder().elicitation().build())
                .requestTimeout(Duration.ofSeconds(30L))
                .initializationTimeout(Duration.ofSeconds(30L))
                .elicitation(request -> createElicitationResult(elicitationRequests, request))
                .build();
    }
    
    private McpClientTransport createClientTransport(final RuntimeTransport transport) throws IOException {
        return RuntimeTransport.HTTP == transport
                ? createHttpClientTransport()
                : createStdioClientTransport();
    }
    
    private McpClientTransport createHttpClientTransport() throws IOException {
        URI endpointUri = getHttpEndpointUri();
        return HttpClientStreamableHttpTransport.builder(String.format("%s://%s:%d", endpointUri.getScheme(), endpointUri.getHost(), endpointUri.getPort()))
                .endpoint(endpointUri.getPath()).build();
    }
    
    private StdioClientTransport createStdioClientTransport() throws IOException {
        Path configFile = getConfigFile();
        return new ProtocolAwareStdioClientTransport(ServerParameters.builder(Paths.get(System.getProperty("java.home"), "bin", "java").toString())
                .args("-Dlogback.configurationFile=" + MCPStdioLogbackConfiguration.createForConfig(configFile, STDIO_LOGBACK_CONFIG_FILE_NAME),
                        "-cp", System.getProperty("java.class.path"), MCPBootstrap.class.getName(), configFile.toString())
                .build());
    }
    
    private McpSchema.ElicitResult createElicitationResult(final List<McpSchema.ElicitRequest> elicitationRequests,
                                                           final McpSchema.ElicitRequest request) {
        elicitationRequests.add(request);
        return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of(
                "primary_algorithm_properties.from-x", "1",
                "primary_algorithm_properties.to-y", "3"));
    }
    
    private void assertElicitationRequest(final List<McpSchema.ElicitRequest> actualRequests) {
        assertThat(actualRequests.size(), is(1));
        McpSchema.ElicitRequest actual = actualRequests.get(0);
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.TOOL), is(MASK_PLAN_TOOL_NAME));
        assertFalse(String.valueOf(actual.meta().get(MCPShardingSphereMetadataKeys.PLAN_ID)).isBlank());
        Map<String, Object> actualRequestedSchema = actual.requestedSchema();
        assertThat(actualRequestedSchema.get("type"), is("object"));
        assertFalse((Boolean) actualRequestedSchema.get("additionalProperties"));
        Map<String, Object> actualProperties = castToMap(actualRequestedSchema.get("properties"));
        assertTrue(actualProperties.containsKey("primary_algorithm_properties.from-x"));
        assertTrue(actualProperties.containsKey("primary_algorithm_properties.to-y"));
        assertFalse(actualProperties.keySet().stream().map(String::valueOf).anyMatch(each -> each.contains("secret") || each.contains("password") || each.contains("token")));
        assertThat(getStringList(actualRequestedSchema.get("required")), hasItems("primary_algorithm_properties.from-x", "primary_algorithm_properties.to-y"));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castStructuredContent(final Object value) {
        return (Map<String, Object>) value;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
    
    private List<String> getStringList(final Object value) {
        return ((List<?>) value).stream().map(String::valueOf).toList();
    }
    
    private static final class ProtocolAwareStdioClientTransport extends StdioClientTransport {
        
        private ProtocolAwareStdioClientTransport(final ServerParameters params) {
            super(params, MCPTransportJsonMapperFactory.create());
        }
        
        @Override
        public List<String> protocolVersions() {
            return List.of(ProtocolVersions.MCP_2025_06_18, ProtocolVersions.MCP_2025_11_25);
        }
    }
}
