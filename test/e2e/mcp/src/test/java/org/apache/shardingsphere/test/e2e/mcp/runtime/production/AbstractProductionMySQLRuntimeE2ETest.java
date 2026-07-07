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

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.OfficialMCPToolNames;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.provider.Arguments;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("isEnabled")
abstract class AbstractProductionMySQLRuntimeE2ETest extends AbstractTransportParameterizedProductionRuntimeE2ETest {
    
    protected static final String LOGICAL_DATABASE_NAME = "logic_db";
    
    protected static final String PHYSICAL_DATABASE_NAME = "orders";
    
    protected static final String MASK_PLAN_TOOL_NAME = "database_gateway_plan_mask_rule";
    
    @Getter(AccessLevel.PROTECTED)
    private GenericContainer<?> container;
    
    @Getter(AccessLevel.PROTECTED)
    private String physicalSchemaName;
    
    private MySQLRuntimeFixture sharedRuntimeFixture;
    
    @AfterEach
    void tearDownContainer() {
        if (useSharedRuntimeFixture()) {
            container = null;
            physicalSchemaName = null;
            return;
        }
        if (null != container) {
            container.stop();
            container = null;
        }
        physicalSchemaName = null;
    }
    
    @AfterAll
    void tearDownSharedContainer() {
        if (null != sharedRuntimeFixture) {
            sharedRuntimeFixture.close();
            sharedRuntimeFixture = null;
        }
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(),
                () -> MySQLRuntimeTestSupport.createDockerRequiredMessage("Docker is required for the MySQL-backed production runtime E2E test."));
        if (useSharedRuntimeFixture()) {
            prepareSharedRuntimeFixture();
            return;
        }
        applyRuntimeFixture(createRuntimeFixture());
    }
    
    private void prepareSharedRuntimeFixture() throws IOException {
        if (null == sharedRuntimeFixture) {
            sharedRuntimeFixture = createRuntimeFixture();
        }
        applyRuntimeFixture(sharedRuntimeFixture);
    }
    
    private MySQLRuntimeFixture createRuntimeFixture() throws IOException {
        GenericContainer<?> result = MySQLRuntimeTestSupport.createContainer();
        boolean success = false;
        try {
            result.start();
            MySQLRuntimeTestSupport.initializeDatabase(result);
            String detectedSchemaName = MySQLRuntimeTestSupport.detectSchema(result);
            success = true;
            return new MySQLRuntimeFixture(result, detectedSchemaName.isEmpty() ? PHYSICAL_DATABASE_NAME : detectedSchemaName);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        } finally {
            if (!success) {
                result.stop();
            }
        }
    }
    
    private void applyRuntimeFixture(final MySQLRuntimeFixture fixture) {
        container = fixture.container();
        physicalSchemaName = fixture.physicalSchemaName();
    }
    
    protected boolean useSharedRuntimeFixture() {
        return false;
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return MySQLRuntimeTestSupport.createRuntimeDatabases(container, LOGICAL_DATABASE_NAME);
    }
    
    protected static Map<String, Object> createExecuteUpdateArguments(final String sql) {
        return Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", sql, "execution_mode", "execute");
    }
    
    protected static Map<String, Object> createExecuteUpdateArguments(final String databaseName, final String sql) {
        return Map.of("database", databaseName, "schema", databaseName, "sql", sql, "execution_mode", "execute");
    }
    
    protected static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    protected static Stream<Arguments> transports() {
        return ProductionRuntimeTransportCases.transports();
    }
    
    protected static Stream<Arguments> dualTransports() {
        return transports();
    }
    
    protected static Stream<Arguments> semanticPrimaryTransport() {
        return ProductionRuntimeTransportCases.semanticPrimaryTransport();
    }
    
    protected static Stream<Arguments> assertReadSingleMetadataResourceCases() {
        return ProductionRuntimeTransportCases.assertReadSingleMetadataResourceCases(LOGICAL_DATABASE_NAME);
    }
    
    protected static Stream<Arguments> assertReadCollectionMetadataResourceCases() {
        return ProductionRuntimeTransportCases.assertReadCollectionMetadataResourceCases(LOGICAL_DATABASE_NAME);
    }
    
    protected Map<String, RuntimeDatabaseConfiguration> createPreparedProgrammaticRuntimeDatabases() throws IOException {
        prepareRuntime();
        try {
            return MySQLRuntimeTestSupport.createPreparedProgrammaticRuntimeDatabases(container);
        } catch (final SQLException ex) {
            throw new IOException("Failed to initialize MySQL programmatic runtime databases.", ex);
        }
    }
    
    protected List<String> readTableNames(final MCPInteractionClient interactionClient, final String databaseName) throws IOException, InterruptedException {
        return getPayloadItems(interactionClient.readResource(String.format("shardingsphere://databases/%s/schemas/%s/tables", databaseName, databaseName)))
                .stream().map(each -> String.valueOf(each.get("table"))).toList();
    }
    
    protected void assertOfficialToolNames(final List<String> actualToolNames) {
        assertThat(actualToolNames, containsInAnyOrder(OfficialMCPToolNames.getAll().toArray()));
    }
    
    protected void assertToolDefinition(final List<Map<String, Object>> tools, final String toolName, final String expectedTitle,
                                        final String expectedRequiredField, final String expectedPropertyField, final String expectedPropertyType) {
        MCPPayloadAssertions.assertToolDefinition(tools, toolName, expectedTitle, expectedRequiredField, expectedPropertyField, expectedPropertyType);
    }
    
    protected void assertJsonRpcErrorWithoutResult(final Map<String, Object> actual, final String requestId) {
        assertThat(String.valueOf(actual.get("jsonrpc")), is("2.0"));
        assertThat(String.valueOf(actual.get("id")), is(requestId));
        assertTrue(MCPInteractionPayloads.hasJsonRpcError(actual));
        assertFalse(actual.containsKey("result"));
        Map<String, Object> error = getMap(actual.get("error"));
        assertThat(error.get("code"), isA(Number.class));
        assertFalse(String.valueOf(error.get("message")).isBlank());
    }
    
    protected void assertRecoveryResponse(final Map<String, Object> actual) {
        assertThat(String.valueOf(actual.get("response_mode")), is("recovery"));
        assertFalse(String.valueOf(actual.get("message")).isBlank());
    }
    
    protected void assertRecoveryResponse(final Map<String, Object> actual, final String expectedMessage) {
        assertRecoveryResponse(actual);
        assertThat(String.valueOf(actual.get("message")), is(expectedMessage));
    }
    
    protected void assertRecoveryResponse(final Map<String, Object> actual, final String expectedMessage, final String expectedCategory) {
        assertRecoveryResponse(actual, expectedMessage);
        assertThat(String.valueOf(getMap(actual.get("recovery")).get("category")), is(expectedCategory));
    }
    
    protected void assertAiNativeGuidance(final Map<String, Object> guidance) {
        assertTrue(guidance.containsKey("model_first_summary"));
        assertTrue(guidance.containsKey("model_contract"));
        assertTrue(guidance.containsKey("surface_summary"));
        assertTrue(guidance.containsKey("field_naming_contract"));
        assertTrue(guidance.containsKey("next_action_contract"));
        assertTrue(guidance.containsKey("common_flows"));
        assertTrue(guidance.containsKey("security_hints"));
        assertFalse(guidance.containsKey("fingerprints"));
        assertFalse(((List<?>) guidance.get("common_flows")).isEmpty());
        Map<String, Object> modelFirstSummary = getMap(guidance.get("model_first_summary"));
        assertThat(getMap(modelFirstSummary.get("official_discovery_methods")).get("tools"), is("tools/list"));
        assertThat(modelFirstSummary.get("guidance_resource"), is("shardingsphere://guidance"));
        assertThat(getMap(modelFirstSummary.get("preflight_rule")).get("tool"), is("database_gateway_validate_runtime_database"));
        assertThat(getMap(getMap(modelFirstSummary.get("sql_tool_selection")).get("read_only")).get("tool"), is("database_gateway_execute_query"));
        assertThat(getMap(getMap(modelFirstSummary.get("workflow_rule")).get("preview_tool")).get("tool"), is("database_gateway_apply_workflow"));
        Map<String, Object> surfaceSummary = getMap(guidance.get("surface_summary"));
        assertThat(getMap(surfaceSummary.get("official_discovery_methods")).get("resources"), is("resources/list"));
        assertThat(surfaceSummary.get("preflight_validation_tool"), is("database_gateway_validate_runtime_database"));
        assertThat(surfaceSummary.get("read_only_sql_tool"), is("database_gateway_execute_query"));
        assertThat(surfaceSummary.get("side_effect_sql_tool"), is("database_gateway_execute_update"));
    }
    
    protected void assertAiNativeDiscovery(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
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
    
    protected void assertAiNativeSqlPreview(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_update",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "preview"));
        assertThat(String.valueOf(actual.get("response_mode")), is("preview"));
        assertThat(String.valueOf(actual.get("result_kind")), is("preview"));
        assertThat(String.valueOf(actual.get("preview_semantics")), is("classification_only"));
        assertFalse((Boolean) actual.get("would_execute"));
        List<Map<String, Object>> nextActions = getMapList(actual.get("next_actions"));
        assertThat(nextActions.stream().map(each -> String.valueOf(each.get("type"))).toList(), is(List.of("ask_user", "tool_call")));
        Map<String, Object> askUserAction = nextActions.getFirst();
        assertThat(askUserAction.get("order"), is(1));
        assertThat(askUserAction.get("required_inputs"), is(List.of("execution_approved")));
        Map<String, Object> toolCallAction = nextActions.get(1);
        assertThat(toolCallAction.get("order"), is(2));
        assertThat(toolCallAction.get("tool_name"), is("database_gateway_execute_update"));
        assertThat(toolCallAction.get("depends_on"), is(List.of(1)));
        assertThat(getMap(toolCallAction.get("arguments")).get("execution_mode"), is("execute"));
    }
    
    protected void assertAiNativeSqlResult(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actual = interactionClient.call("database_gateway_execute_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT order_id, status FROM orders ORDER BY order_id", "max_rows", 1));
        assertThat(String.valueOf(actual.get("result_kind")), is("result_set"));
        assertThat(String.valueOf(actual.get("row_object_status")), is("available"));
        assertThat(((List<?>) actual.get("row_objects")).size(), is(1));
        assertThat(String.valueOf(actual.get("truncated")), is("true"));
        assertThat(String.valueOf(getMapList(actual.get("next_actions")).getFirst().get("type")), is("ask_user"));
    }
    
    protected McpSyncClient createElicitationClient(final RuntimeTransport transport, final List<McpSchema.ElicitRequest> elicitationRequests) throws IOException {
        return ProductionMCPClientTransportFactory.createElicitationClient(createClientTransport(transport), elicitationRequests, this::createElicitationResult);
    }
    
    private McpClientTransport createClientTransport(final RuntimeTransport transport) throws IOException {
        return RuntimeTransport.HTTP == transport
                ? ProductionMCPClientTransportFactory.createHttpClientTransport(getHttpEndpointUri())
                : ProductionMCPClientTransportFactory.createStdioClientTransport(getConfigFile());
    }
    
    protected McpSchema.ElicitResult createElicitationResult(final List<McpSchema.ElicitRequest> elicitationRequests,
                                                             final McpSchema.ElicitRequest request) {
        elicitationRequests.add(request);
        List<String> requiredFields = getStringList(request.requestedSchema().get("required"));
        return new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of(
                requiredFields.getFirst(), "1",
                requiredFields.get(1), "3"));
    }
    
    protected void assertElicitationRequest(final List<McpSchema.ElicitRequest> actualRequests) {
        ProductionMCPClientTransportFactory.assertElicitationRequest(actualRequests);
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> castStructuredContent(final Object value) {
        return (Map<String, Object>) value;
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
    
    protected Map<String, Object> getMap(final Object value) {
        return value instanceof Map ? castToMap(value) : Map.of();
    }
    
    protected List<Map<String, Object>> getMapList(final Object value) {
        return MCPPayloadAssertions.getMapList(value);
    }
    
    protected List<String> getStringList(final Object value) {
        return ((List<?>) value).stream().map(String::valueOf).toList();
    }
    
    private static final class MySQLRuntimeFixture implements AutoCloseable {
        
        private final GenericContainer<?> container;
        
        private final String physicalSchemaName;
        
        private MySQLRuntimeFixture(final GenericContainer<?> container, final String physicalSchemaName) {
            this.container = container;
            this.physicalSchemaName = physicalSchemaName;
        }
        
        private GenericContainer<?> container() {
            return container;
        }
        
        private String physicalSchemaName() {
            return physicalSchemaName;
        }
        
        @Override
        public void close() {
            container.stop();
        }
    }
}
