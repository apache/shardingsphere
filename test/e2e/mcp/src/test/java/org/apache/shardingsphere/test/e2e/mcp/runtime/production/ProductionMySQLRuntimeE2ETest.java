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

import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.support.markdown.MCPMarkdownResourceLoader;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPPayloadAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class ProductionMySQLRuntimeE2ETest extends AbstractProductionMySQLRuntimeE2ETest {
    
    @Override
    protected boolean useSharedRuntimeFixture() {
        return true;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertReadCapabilitiesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource("shardingsphere://databases/logic_db/capabilities");
            assertThat(String.valueOf(actual.get("databaseType")), is("MySQL"));
            assertThat(String.valueOf(actual.get("supportsExplain")), is("true"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertReadDatabasesResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            MCPPayloadAssertions.assertSingleItemValue(interactionClient.readResource("shardingsphere://databases"), "database", LOGICAL_DATABASE_NAME);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertServiceCapabilitiesResourceWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.readResource("shardingsphere://capabilities");
            assertFalse(((Collection<?>) actual.get("supportedStatementClasses")).isEmpty());
            assertFalse(((List<?>) actual.get("completionTargets")).isEmpty());
            assertFalse(((List<?>) actual.get("resourceNavigation")).isEmpty());
            assertFalse(actual.containsKey("supportedTools"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertListToolsWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> actual = interactionClient.listTools();
            assertOfficialToolNames(actual.stream().map(each -> String.valueOf(each.get("name"))).toList());
            assertToolDefinition(actual, "database_gateway_search_metadata", "Search Metadata", "", "object_types", "array");
            assertToolDefinition(actual, "database_gateway_validate_runtime_database", "Validate Runtime Database", "database", "database", "string");
            assertToolDefinition(actual, "database_gateway_execute_query", "Execute Query SQL", "sql", "timeout_ms", "integer");
            assertToolDefinition(actual, "database_gateway_execute_update", "Preview or Execute Side-Effecting SQL", "sql", "timeout_ms", "integer");
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertListResourcesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            assertTrue(getResources(interactionClient.listResources()).stream().anyMatch(each -> "shardingsphere://capabilities".equals(each.get("uri"))));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
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
    @MethodSource("semanticPrimaryTransport")
    void assertRejectUnsupportedResourceUriWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            String requestId = "resources-read-unsupported-1";
            Map<String, Object> actual = interactionClient.sendRawRequest(requestId, "resources/read", Map.of("uri", "unsupported://resource"));
            assertJsonRpcErrorWithoutResult(actual, requestId);
            assertFalse(getObjectOrEmpty(actual.get("result")).containsKey("contents"));
            assertThat(String.valueOf(getObjectOrEmpty(actual.get("error")).get("message")), is("Resource not found"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertRejectUnsupportedToolNameWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            String requestId = "tools-call-unsupported-1";
            Map<String, Object> actual = interactionClient.sendRawRequest(requestId, "tools/call", Map.of("name", "unsupported_tool", "arguments", Map.of()));
            assertJsonRpcErrorWithoutResult(actual, requestId);
            assertFalse(getObjectOrEmpty(actual.get("result")).containsKey("isError"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertInitializeExposesMarkdownInstructionsWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actualResult = getObjectOrEmpty(interactionClient.getInitializePayload().get("result"));
            String actualInstructions = String.valueOf(actualResult.get("instructions"));
            assertThat(actualInstructions, is(MCPMarkdownResourceLoader.load(MCPTransportConstants.SERVER_INSTRUCTIONS_RESOURCE, "server instruction")));
            assertThat(actualInstructions.lines().findFirst().orElse(""), is("Apache ShardingSphere MCP."));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
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
    @MethodSource("semanticPrimaryTransport")
    void assertReadTableDetailWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/tables/orders", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME)));
            assertThat(items.size(), is(1));
            Map<String, Object> actualItem = items.getFirst();
            assertThat(String.valueOf(actualItem.get("table")), is("orders"));
            assertThat(getNestedNames(actualItem, "columns", "column"), is(List.of("amount", "order_id", "status")));
            assertThat(getNestedNames(actualItem, "indexes", "index"), containsInAnyOrder("PRIMARY", "idx_orders_status"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertSearchMetadataTablesAndViewsWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "order", "object_types", List.of("table", "view"))));
            assertThat(items.stream().map(each -> String.valueOf(each.get("name"))).toList(), is(List.of("order_items", "orders", "active_orders")));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertValidateRuntimeDatabaseWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> actual = interactionClient.call(
                    "database_gateway_validate_runtime_database", Map.of("database", LOGICAL_DATABASE_NAME));
            assertThat(actual.get("response_mode"), is("validation"));
            assertThat(actual.get("status"), is("ready"));
            assertThat(actual.get("database"), is(LOGICAL_DATABASE_NAME));
            assertFalse(((List<?>) actual.get("checks")).isEmpty());
            assertTrue(((List<?>) actual.get("checks")).stream().map(each -> (Map<?, ?>) each)
                    .allMatch(each -> "passed".equals(each.get("status"))));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertSearchMetadataPaginationWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            Map<String, Object> firstPage = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "order",
                            "object_types", List.of("table", "view"), "limit", 2, "offset", 0));
            assertThat(getPayloadItems(firstPage).stream().map(each -> String.valueOf(each.get("name"))).toList(), is(List.of("order_items", "orders")));
            assertTrue((Boolean) firstPage.get("has_more"));
            assertThat(firstPage.get("next_offset"), is(2));
            Map<String, Object> secondPage = interactionClient.call("database_gateway_search_metadata",
                    Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "order",
                            "object_types", List.of("table", "view"), "limit", 2, "offset", 2));
            assertThat(getPayloadItems(secondPage).stream().map(each -> String.valueOf(each.get("name"))).toList(), is(List.of("active_orders")));
            assertFalse((Boolean) secondPage.get("has_more"));
            assertFalse(secondPage.containsKey("next_offset"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertReadViewsWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<Map<String, Object>> items = getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/views", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME)));
            assertThat(items.size(), is(1));
            assertThat(String.valueOf(items.getFirst().get("view")), is("active_orders"));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("semanticPrimaryTransport")
    void assertReadIndexesWithActualMySQLBackend(final String name, final RuntimeTransport transport) throws IOException, InterruptedException {
        useTransport(transport);
        try (MCPInteractionClient interactionClient = createOpenedInteractionClient()) {
            List<String> actualIndexNames = getPayloadItems(interactionClient.readResource(
                    String.format("shardingsphere://databases/%s/schemas/%s/tables/orders/indexes", LOGICAL_DATABASE_NAME, LOGICAL_DATABASE_NAME)))
                    .stream().map(each -> String.valueOf(each.get("index"))).toList();
            assertThat(actualIndexNames, hasItems("PRIMARY", "idx_orders_status"));
        }
    }
    
}
