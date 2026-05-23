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
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.markdown.MCPMarkdownResourceLoader;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.OfficialMCPToolNames;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
            Map<String, Object> actual = interactionClient.readResource("shardingsphere://databases/logic_db/capabilities");
            assertThat(String.valueOf(actual.get("databaseType")), is("MySQL"));
            assertThat(String.valueOf(actual.get("supportsExplainAnalyze")), is("true"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertReadDatabasesResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource("shardingsphere://databases"), "database", LOGICAL_DATABASE_NAME);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertServiceCapabilitiesResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertOfficialToolNames(((List<?>) interactionClient.readResource("shardingsphere://capabilities").get("supportedTools")).stream().map(String::valueOf).toList());
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertListToolsWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> actual = interactionClient.listTools();
            assertOfficialToolNames(actual.stream().map(each -> String.valueOf(each.get("name"))).toList());
            assertToolDefinition(actual, "database_gateway_search_metadata", "Search Metadata", "", "object_types", "array");
            assertToolDefinition(actual, "database_gateway_execute_query", "Execute Query SQL", "sql", "timeout_ms", "integer");
            assertToolDefinition(actual, "database_gateway_execute_update", "Execute Update SQL", "sql", "timeout_ms", "integer");
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
    void assertListResourceTemplatesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<String> actualTemplates = getResourceTemplates(interactionClient.listResourceTemplates()).stream()
                    .map(each -> String.valueOf(each.get("uriTemplate"))).toList();
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}"));
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}/schemas/{schema}"));
            assertTrue(actualTemplates.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectUnsupportedResourceUriWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            String requestId = "resources-read-unsupported-1";
            Map<String, Object> actual = interactionClient.sendRawRequest(requestId, "resources/read", Map.of("uri", "unsupported://resource"));
            assertJsonRpcErrorWithoutResult(actual, requestId);
            assertFalse(getMap(actual.get("result")).containsKey("contents"));
            assertThat(String.valueOf(getMap(actual.get("error")).get("message")), is("Resource not found"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectUnsupportedToolNameWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            String requestId = "tools-call-unsupported-1";
            Map<String, Object> actual = interactionClient.sendRawRequest(requestId, "tools/call", Map.of("name", "unsupported_tool", "arguments", Map.of()));
            assertJsonRpcErrorWithoutResult(actual, requestId);
            assertFalse(getMap(actual.get("result")).containsKey("isError"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertInitializeExposesMarkdownInstructionsWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualResult = getMap(interactionClient.getInitializePayload().get("result"));
            String actualInstructions = String.valueOf(actualResult.get("instructions"));
            assertThat(actualInstructions, is(MCPMarkdownResourceLoader.loadRequired(MCPTransportConstants.SERVER_INSTRUCTIONS_RESOURCE, "server instruction")));
            assertThat(actualInstructions.lines().findFirst().orElse(""), is("Apache ShardingSphere MCP."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertServerInstructionsAreNotListedAsResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertFalse(getResources(interactionClient.listResources()).stream()
                    .anyMatch(each -> MCPTransportConstants.SERVER_INSTRUCTIONS_RESOURCE.equals(String.valueOf(each.get("uri")))));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadSingleMetadataResourceCases")
    void assertReadSingleMetadataResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport,
                                                                final String resourceUri, final String key, final String expectedValue) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(resourceUri), key, expectedValue);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadCollectionMetadataResourceCases")
    void assertReadCollectionMetadataResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport,
                                                                    final String resourceUri, final String key, final List<String> expectedNames) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertItemValues(interactionClient.readResource(resourceUri), key, expectedNames);
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
            assertThat(getNestedNames(actualItem, "indexes", "index"), containsInAnyOrder("PRIMARY", "idx_orders_status"));
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
    void assertExecuteSelectWithTruncationWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
            assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
            assertThat(((List<?>) actual.get("rows")).size(), is(1));
            assertThat(String.valueOf(actual.get("truncated")), is("true"));
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
    void assertExecuteQueryTimeoutWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT SLEEP(2)", "timeout_ms", 1));
            assertRecoveryResponse(actual);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectExecuteMultiStatementWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT 1; SELECT 2"));
            assertRecoveryResponse(actual, "Only one SQL statement is allowed.", "multiple_sql_statements");
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
            assertRecoveryResponse(actual,
                    "database_gateway_execute_query only supports classifier-approved QUERY and EXPLAIN_ANALYZE statements. Use database_gateway_execute_update for side-effecting SQL.",
                    "unsafe_sql_attempted");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectLockingReadFromReadOnlyToolWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT * FROM orders FOR UPDATE"));
            assertRecoveryResponse(actual, "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract.");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectLockingReadFromUpdateToolWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("SELECT * FROM orders FOR UPDATE"));
            assertRecoveryResponse(actual, "Locking read statements such as SELECT ... FOR UPDATE are not supported by the MCP read-only contract.");
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
            if (RuntimeTransport.HTTP == transport) {
                assertThat(String.valueOf(actualPayload.get("status")), is("clarifying"));
                assertThat(String.valueOf(actualPayload.get("fallback_reason")), is("remote_identity_required"));
                assertTrue(actualElicitationRequests.isEmpty());
                return;
            }
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
    void assertExecuteUpdateWithoutApprovalArgumentWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = status WHERE order_id = -1"));
            assertThat(String.valueOf(actual.get("result_kind")), is("update_count"));
            assertThat(String.valueOf(actual.get("affected_rows")), is("0"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectSequenceResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/sequences", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME));
            assertThat(String.valueOf(actual.get("error_code")), is("json_rpc_error"));
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
    void assertRejectBlankSavepointNameWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("SAVEPOINT"));
            assertRecoveryResponse(actual, "Savepoint name is required.");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteSavepointFlowWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws SQLException, IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
            Map<String, Object> savepointResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("SAVEPOINT sp_1"));
            interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("UPDATE orders SET status = 'DONE' WHERE order_id = 1"));
            Map<String, Object> rollbackResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("ROLLBACK TO SAVEPOINT sp_1"));
            Map<String, Object> commitResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("COMMIT"));
            assertThat(String.valueOf(savepointResponse.get("message")), is("Savepoint created."));
            assertThat(String.valueOf(rollbackResponse.get("message")), is("Savepoint rolled back."));
            assertThat(String.valueOf(commitResponse.get("message")), is("Transaction committed."));
            assertThat(MySQLRuntimeTestSupport.querySingleString(container, String.format("SELECT status FROM %s.orders WHERE order_id = 1", physicalSchemaName)), is("PENDING"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteReleaseSavepointWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("BEGIN"));
            Map<String, Object> savepointResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("SAVEPOINT sp_release"));
            Map<String, Object> releaseResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("RELEASE SAVEPOINT sp_release"));
            Map<String, Object> commitResponse = interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments("COMMIT"));
            assertThat(String.valueOf(savepointResponse.get("message")), is("Savepoint created."));
            assertThat(String.valueOf(releaseResponse.get("message")), is("Savepoint released."));
            assertThat(String.valueOf(commitResponse.get("message")), is("Transaction committed."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertExecuteDdlRefreshesMetadataWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> executeResponse = interactionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments("CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
            Map<String, Object> actualItem = MCPPayloadAssertions.getSingleItem(interactionClient.readResource(
                    "shardingsphere://databases/logic_db/schemas/logic_db/tables/orders_archive"));
            assertThat(String.valueOf(executeResponse.get("message")), is("Statement executed."));
            assertThat(String.valueOf(actualItem.get("table")), is("orders_archive"));
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertAiNativeDeterministicInteractionLoopWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertAiNativeCapabilities(interactionClient.readResource("shardingsphere://capabilities"));
            assertAiNativeDiscovery(interactionClient);
            Map<String, Object> searchMetadataPayload = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "orders", "object_types", List.of("table")));
            Map<String, Object> tableHit = MCPPayloadAssertions.findItem(searchMetadataPayload, "name", "orders");
            String tableResourceUri = String.valueOf(getMap(tableHit.get("resource")).get("uri"));
            assertThat(tableResourceUri, is("shardingsphere://databases/logic_db/schemas/logic_db/tables/orders"));
            assertFalse(getMapList(tableHit.get("next_resources")).isEmpty());
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource(tableResourceUri), "table", "orders");
            assertAiNativeSqlPreview(interactionClient);
            assertAiNativeSqlResult(interactionClient);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertListDatabasesWithMultipleRuntimeDatabasesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient(createPreparedProgrammaticRuntimeDatabases())) {
            List<String> actualDatabaseNames = getPayloadItems(interactionClient.readResource("shardingsphere://databases")).stream()
                    .map(each -> String.valueOf(each.get("database"))).toList();
            assertThat(actualDatabaseNames, hasItems(LOGICAL_DATABASE_NAME, "analytics_db", "warehouse"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRefreshMetadataVisibleForTargetDatabaseOnlyWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient firstInteractionClient = createOpenedInteractionClient(createPreparedProgrammaticRuntimeDatabases())) {
            firstInteractionClient.call("database_gateway_execute_update",
                    createExecuteUpdateArguments(LOGICAL_DATABASE_NAME, "CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
            List<String> firstSessionTableNames = readTableNames(firstInteractionClient, LOGICAL_DATABASE_NAME);
            try (MCPInteractionClient secondInteractionClient = createOpenedInteractionClient(createPreparedProgrammaticRuntimeDatabases())) {
                List<String> secondSessionTableNames = readTableNames(secondInteractionClient, LOGICAL_DATABASE_NAME);
                List<String> analyticsDatabaseTableNames = readTableNames(secondInteractionClient, "analytics_db");
                assertTrue(firstSessionTableNames.contains("orders_archive"));
                assertTrue(secondSessionTableNames.contains("orders_archive"));
                assertFalse(analyticsDatabaseTableNames.contains("orders_archive"));
                assertTrue(analyticsDatabaseTableNames.contains("metrics"));
            }
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectCrossDatabaseTransactionSwitchWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient(createPreparedProgrammaticRuntimeDatabases())) {
            interactionClient.call("database_gateway_execute_update", createExecuteUpdateArguments(LOGICAL_DATABASE_NAME, "BEGIN"));
            Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                    Map.of("database", "analytics_db", "schema", "analytics_db", "sql", "SELECT metric_name FROM metrics ORDER BY metric_id"));
            assertRecoveryResponse(actual, "Cross-database transaction switching is not supported.");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transports")
    void assertRejectMismatchedDatabaseTypeWithActualMySQLBackend(final String name, final RuntimeTransport transport) {
        useTransport(transport);
        if (RuntimeTransport.HTTP == transport) {
            RuntimeDatabaseConnectionException actual = assertThrows(RuntimeDatabaseConnectionException.class,
                    () -> openAndCloseInteractionClient(createMismatchedRuntimeDatabases()));
            assertThat(actual.getMessage(), is("Runtime database `logic_db` connection failed: invalid_configuration."));
            assertThat(actual.getCategory(), is(RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION));
            assertThat(actual.getCause().getMessage(), is("Configured databaseType `PostgreSQL` does not match actual database type `MySQL` for database `logic_db`."));
            return;
        }
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> openAndCloseInteractionClient(createMismatchedRuntimeDatabases()));
        assertThat(actual.getMessage(), containsString("Runtime database `logic_db` connection failed: invalid_configuration."));
        assertThat(actual.getMessage(), containsString("Configured databaseType `PostgreSQL` does not match actual database type `MySQL` for database `logic_db`."));
    }
    
    private static Map<String, Object> createExecuteUpdateArguments(final String sql) {
        return Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", sql, "execution_mode", "execute");
    }
    
    private static Map<String, Object> createExecuteUpdateArguments(final String databaseName, final String sql) {
        return Map.of("database", databaseName, "schema", databaseName, "sql", sql, "execution_mode", "execute");
    }
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isProductionMySQLEnabled() || MCPE2ECondition.isProductionMySQLStdioEnabled();
    }
    
    private static Stream<Arguments> transports() {
        return runtimeTransports().map(each -> Arguments.of(getTransportName(each), each));
    }
    
    private static Stream<Arguments> assertReadSingleMetadataResourceCases() {
        return runtimeTransports().flatMap(each -> Stream.of(
                Arguments.of(getTransportName(each) + " database detail", each, "shardingsphere://databases/logic_db", "database", LOGICAL_DATABASE_NAME),
                Arguments.of(getTransportName(each) + " schema detail", each, "shardingsphere://databases/logic_db/schemas/logic_db", "schema", LOGICAL_DATABASE_NAME),
                Arguments.of(getTransportName(each) + " table column detail", each,
                        "shardingsphere://databases/logic_db/schemas/logic_db/tables/orders/columns/status", "column", "status"),
                Arguments.of(getTransportName(each) + " view detail", each, "shardingsphere://databases/logic_db/schemas/logic_db/views/active_orders", "view", "active_orders"),
                Arguments.of(getTransportName(each) + " view column detail", each,
                        "shardingsphere://databases/logic_db/schemas/logic_db/views/active_orders/columns/status", "column", "status"),
                Arguments.of(getTransportName(each) + " index detail", each,
                        "shardingsphere://databases/logic_db/schemas/logic_db/tables/orders/indexes/idx_orders_status", "index", "idx_orders_status")));
    }
    
    private static Stream<Arguments> assertReadCollectionMetadataResourceCases() {
        return runtimeTransports().flatMap(each -> Stream.of(
                Arguments.of(getTransportName(each) + " schemas list", each, "shardingsphere://databases/logic_db/schemas", "schema", List.of(LOGICAL_DATABASE_NAME)),
                Arguments.of(getTransportName(each) + " tables list", each,
                        "shardingsphere://databases/logic_db/schemas/logic_db/tables", "table", List.of("order_items", "orders")),
                Arguments.of(getTransportName(each) + " table columns list", each,
                        "shardingsphere://databases/logic_db/schemas/logic_db/tables/orders/columns", "column", List.of("amount", "order_id", "status")),
                Arguments.of(getTransportName(each) + " view columns list", each,
                        "shardingsphere://databases/logic_db/schemas/logic_db/views/active_orders/columns", "column", List.of("order_id", "status"))));
    }
    
    private static Stream<RuntimeTransport> runtimeTransports() {
        Stream.Builder<RuntimeTransport> result = Stream.builder();
        if (MCPE2ECondition.isProductionMySQLEnabled()) {
            result.add(RuntimeTransport.HTTP);
        }
        if (MCPE2ECondition.isProductionMySQLStdioEnabled()) {
            result.add(RuntimeTransport.STDIO);
        }
        return result.build();
    }
    
    private static String getTransportName(final RuntimeTransport transport) {
        return RuntimeTransport.HTTP == transport ? "http" : "stdio";
    }
    
    private Map<String, RuntimeDatabaseConfiguration> createPreparedProgrammaticRuntimeDatabases() throws IOException {
        prepareRuntime();
        try {
            return MySQLRuntimeTestSupport.createPreparedProgrammaticRuntimeDatabases(container);
        } catch (final SQLException ex) {
            throw new IOException("Failed to initialize MySQL programmatic runtime databases.", ex);
        }
    }
    
    private Map<String, RuntimeDatabaseConfiguration> createMismatchedRuntimeDatabases() throws IOException {
        prepareRuntime();
        RuntimeDatabaseConfiguration runtimeDatabase = MySQLRuntimeTestSupport.createRuntimeDatabases(container, LOGICAL_DATABASE_NAME).get(LOGICAL_DATABASE_NAME);
        return Map.of(LOGICAL_DATABASE_NAME, new RuntimeDatabaseConfiguration("PostgreSQL", runtimeDatabase.getJdbcUrl(), runtimeDatabase.getUsername(),
                runtimeDatabase.getPassword(), runtimeDatabase.getDriverClassName()));
    }
    
    private void openAndCloseInteractionClient(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient(runtimeDatabases)) {
            interactionClient.listResources();
        } catch (final IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private List<String> readTableNames(final MCPInteractionClient interactionClient, final String databaseName) throws IOException, InterruptedException {
        return getPayloadItems(interactionClient.readResource(String.format("shardingsphere://databases/%s/schemas/%s/tables", databaseName, databaseName)))
                .stream().map(each -> String.valueOf(each.get("table"))).toList();
    }
    
    private void assertOfficialToolNames(final List<String> actualToolNames) {
        assertThat(actualToolNames, containsInAnyOrder(OfficialMCPToolNames.getAll().toArray()));
    }
    
    private void assertToolDefinition(final List<Map<String, Object>> tools, final String toolName, final String expectedTitle,
                                      final String expectedRequiredField, final String expectedPropertyField, final String expectedPropertyType) {
        MCPPayloadAssertions.assertToolDefinition(tools, toolName, expectedTitle, expectedRequiredField, expectedPropertyField, expectedPropertyType);
    }
    
    private void assertJsonRpcErrorWithoutResult(final Map<String, Object> actual, final String requestId) {
        assertThat(String.valueOf(actual.get("jsonrpc")), is("2.0"));
        assertThat(String.valueOf(actual.get("id")), is(requestId));
        assertTrue(MCPInteractionPayloads.hasJsonRpcError(actual));
        assertFalse(actual.containsKey("result"));
        Map<String, Object> error = getMap(actual.get("error"));
        assertThat(error.get("code"), isA(Number.class));
        assertFalse(String.valueOf(error.get("message")).isBlank());
    }
    
    private void assertRecoveryResponse(final Map<String, Object> actual) {
        assertThat(String.valueOf(actual.get("response_mode")), is("recovery"));
        assertFalse(String.valueOf(actual.get("message")).isBlank());
    }
    
    private void assertRecoveryResponse(final Map<String, Object> actual, final String expectedMessage) {
        assertRecoveryResponse(actual);
        assertThat(String.valueOf(actual.get("message")), is(expectedMessage));
    }
    
    private void assertRecoveryResponse(final Map<String, Object> actual, final String expectedMessage, final String expectedCategory) {
        assertRecoveryResponse(actual, expectedMessage);
        assertThat(String.valueOf(getMap(actual.get("recovery")).get("category")), is(expectedCategory));
    }
    
    private void assertAiNativeCapabilities(final Map<String, Object> capabilities) {
        assertTrue(capabilities.containsKey("model_first_summary"));
        assertTrue(capabilities.containsKey("model_contract"));
        assertTrue(capabilities.containsKey("surface_summary"));
        assertTrue(capabilities.containsKey("field_naming_contract"));
        assertTrue(capabilities.containsKey("next_action_contract"));
        assertTrue(capabilities.containsKey("common_flows"));
        assertTrue(capabilities.containsKey("security_hints"));
        assertTrue(capabilities.containsKey("fingerprints"));
        assertFalse(getMap(capabilities.get("fingerprints")).isEmpty());
        assertFalse(((List<?>) capabilities.get("common_flows")).isEmpty());
        Map<String, Object> modelFirstSummary = getMap(capabilities.get("model_first_summary"));
        assertThat(getMap(modelFirstSummary.get("official_discovery_methods")).get("tools"), is("tools/list"));
        assertThat(modelFirstSummary.get("optional_catalog_resource"), is("shardingsphere://capabilities"));
        assertThat(getMap(getMap(modelFirstSummary.get("sql_tool_selection")).get("read_only")).get("tool"), is("database_gateway_execute_query"));
        assertThat(getMap(getMap(modelFirstSummary.get("workflow_rule")).get("preview_tool")).get("tool"), is("database_gateway_apply_workflow"));
        Map<String, Object> surfaceSummary = getMap(capabilities.get("surface_summary"));
        assertThat(getMap(surfaceSummary.get("official_discovery_methods")).get("resources"), is("resources/list"));
        assertThat(surfaceSummary.get("read_only_sql_tool"), is("database_gateway_execute_query"));
        assertThat(surfaceSummary.get("side_effect_sql_tool"), is("database_gateway_execute_update"));
    }
    
    private void assertAiNativeDiscovery(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> runtimeStatus = interactionClient.readResource("shardingsphere://runtime");
        assertThat(String.valueOf(runtimeStatus.get("status")), is("available"));
        assertThat(String.valueOf(runtimeStatus.get("configured_database_count")), is("1"));
        assertTrue(getResources(interactionClient.listResources()).stream().anyMatch(each -> "shardingsphere://runtime".equals(each.get("uri"))));
        assertTrue(getResourceTemplates(interactionClient.listResourceTemplates()).stream()
                .anyMatch(each -> "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}".equals(each.get("uriTemplate"))));
        assertTrue(interactionClient.listTools().stream().anyMatch(each -> "database_gateway_execute_update".equals(each.get("name")) && getMap(each.get("outputSchema")).containsKey("properties")));
        Map<String, Object> promptPayload = interactionClient.getPrompt("inspect_metadata",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "orders"));
        assertTrue(String.valueOf(promptPayload).contains("Stop conditions"));
        Map<String, Object> completionPayload = interactionClient.complete(Map.of("type", "ref/prompt", "name", "inspect_metadata"),
                "schema", "log", Map.of("database", LOGICAL_DATABASE_NAME));
        assertTrue(((List<?>) getMap(completionPayload.get("completion")).get("values")).contains(LOGICAL_DATABASE_NAME));
    }
    
    private void assertAiNativeSqlPreview(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "preview"));
        assertThat(String.valueOf(actual.get("response_mode")), is("preview"));
        assertThat(String.valueOf(actual.get("result_kind")), is("preview"));
        assertThat(String.valueOf(actual.get("preview_semantics")), is("classification_only"));
        assertFalse((Boolean) actual.get("would_execute"));
        List<Map<String, Object>> nextActions = getMapList(actual.get("next_actions"));
        assertThat(nextActions.stream().map(each -> String.valueOf(each.get("type"))).toList(), is(List.of("tool_call")));
        assertFalse(nextActions.get(0).containsKey("requires_user_approval"));
    }
    
    private void assertAiNativeSqlResult(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
        assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        assertThat(String.valueOf(actual.get("row_object_status")), is("available"));
        assertThat(((List<?>) actual.get("row_objects")).size(), is(1));
        assertThat(String.valueOf(actual.get("truncated")), is("true"));
        assertThat(String.valueOf(getMapList(actual.get("next_actions")).get(0).get("type")), is("ask_user"));
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
        List<String> requiredFields = getStringList(request.requestedSchema().get("required"));
        return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of(
                requiredFields.get(0), "1",
                requiredFields.get(1), "3"));
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
        assertTrue(actualProperties.containsKey("field_1"));
        assertTrue(actualProperties.containsKey("field_2"));
        assertThat(String.valueOf(castToMap(actualProperties.get("field_1")).get("description")), is("Please provide property `from-x`."));
        assertThat(String.valueOf(castToMap(actualProperties.get("field_2")).get("description")), is("Please provide property `to-y`."));
        assertFalse(actualProperties.keySet().stream().map(String::valueOf).anyMatch(each -> each.contains("secret") || each.contains("password") || each.contains("token")));
        assertThat(getStringList(actualRequestedSchema.get("required")), hasItems("field_1", "field_2"));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castStructuredContent(final Object value) {
        return (Map<String, Object>) value;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
    
    private Map<String, Object> getMap(final Object value) {
        return value instanceof Map ? castToMap(value) : Map.of();
    }
    
    private List<Map<String, Object>> getMapList(final Object value) {
        return MCPPayloadAssertions.getMapList(value);
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
