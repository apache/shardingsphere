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

package org.apache.shardingsphere.mcp.core.handler.core;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.completion.handler.MetadataCompletionHandler;
import org.apache.shardingsphere.mcp.core.completion.handler.WorkflowPlanIdCompletionHandler;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.RequestContextFixture;
import org.apache.shardingsphere.mcp.core.resource.handler.metadata.MetadataResourceHandler;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreHandlerProviderTest {
    
    private final CoreHandlerProvider provider = ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class).stream()
            .filter(CoreHandlerProvider.class::isInstance).map(CoreHandlerProvider.class::cast).findFirst().orElseThrow();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("metadataResourceCases")
    void assertGetResourceHandlersWithMetadata(final String name, final String template, final String uri, final String nameField, final Collection<String> expectedNames) {
        MetadataResourceHandler actualHandler = (MetadataResourceHandler) provider.getResourceHandlers().stream()
                .filter(handler -> template.equals(handler.getResourceUriTemplate())).findFirst().orElseThrow();
        try (RequestContextFixture fixture = ResourceTestDataFactory.createRequestContextFixture(ResourceTestDataFactory.createDatabaseMetadata())) {
            MCPResourceURIVariables variables = new MCPUriTemplate(template).parse(uri).orElseThrow();
            Map<String, Object> actual = actualHandler.handle(fixture.getRequestContext(), variables).toPayload();
            assertThat(actual.get("count"), is(expectedNames.size()));
            assertThat(((Collection<?>) actual.get("items")).stream().map(item -> ((Map<?, ?>) item).get(nameField)).toList(), is(expectedNames));
            assertThat(((Map<?, ?>) actual.get("self_resource")).get("uri"), is(uri));
        }
    }
    
    private static Stream<Arguments> metadataResourceCases() {
        return Stream.of(
                Arguments.of("databases", "shardingsphere://databases", "shardingsphere://databases", "database", List.of("logic_db", "runtime_db", "warehouse")),
                Arguments.of("database", "shardingsphere://databases/{database}", "shardingsphere://databases/logic_db", "database", List.of("logic_db")),
                Arguments.of("schemas", "shardingsphere://databases/{database}/schemas", "shardingsphere://databases/logic_db/schemas", "schema", List.of("public")),
                Arguments.of("schema", "shardingsphere://databases/{database}/schemas/{schema}", "shardingsphere://databases/logic_db/schemas/public", "schema", List.of("public")),
                Arguments.of("sequences", "shardingsphere://databases/{database}/schemas/{schema}/sequences", "shardingsphere://databases/runtime_db/schemas/public/sequences", "sequence",
                        List.of("order_seq")),
                Arguments.of("sequence", "shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                        "shardingsphere://databases/runtime_db/schemas/public/sequences/order_seq", "sequence", List.of("order_seq")),
                Arguments.of("tables", "shardingsphere://databases/{database}/schemas/{schema}/tables", "shardingsphere://databases/logic_db/schemas/public/tables", "table", List.of("order_items",
                        "orders")),
                Arguments.of("table", "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}", "shardingsphere://databases/logic_db/schemas/public/tables/orders", "table",
                        List.of("orders")),
                Arguments.of("views", "shardingsphere://databases/{database}/schemas/{schema}/views", "shardingsphere://databases/logic_db/schemas/public/views", "view", List.of("orders_view")),
                Arguments.of("view", "shardingsphere://databases/{database}/schemas/{schema}/views/{view}", "shardingsphere://databases/logic_db/schemas/public/views/orders_view", "view",
                        List.of("orders_view")),
                Arguments.of("table columns", "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns", "column", List.of("order_id")),
                Arguments.of("table column", "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id", "column", List.of("order_id")),
                Arguments.of("view columns", "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns", "column", List.of("order_id")),
                Arguments.of("view column", "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns/order_id", "column", List.of("order_id")),
                Arguments.of("indexes", "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes", "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes",
                        "index", List.of("order_idx")),
                Arguments.of("index", "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes/order_idx", "index", List.of("order_idx")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("governanceResourceCases")
    void assertGetResourceHandlersWithGovernanceMetadata(final String name, final String template, final String uri, final String sql, final Map<String, Object> expectedRow) {
        MCPFeatureRequestContext requestContext = mock(MCPFeatureRequestContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(requestContext.getQueryFacade()).thenReturn(queryFacade);
        when(queryFacade.query("foo_db", sql)).thenReturn(List.of(expectedRow));
        MetadataResourceHandler actualHandler = (MetadataResourceHandler) provider.getResourceHandlers().stream()
                .filter(handler -> template.equals(handler.getResourceUriTemplate())).findFirst().orElseThrow();
        Map<String, Object> actual = actualHandler.handle(requestContext, new MCPUriTemplate(template).parse(uri).orElseThrow()).toPayload();
        assertThat(actual.get("items"), is(List.of(expectedRow)));
        assertThat(((Map<?, ?>) actual.get("self_resource")).get("uri"), is(uri));
        verify(queryFacade).query("foo_db", sql);
    }
    
    private static Stream<Arguments> governanceResourceCases() {
        return Stream.of(
                Arguments.of("storage units", "shardingsphere://databases/{database}/storage-units", "shardingsphere://databases/foo_db/storage-units",
                        "SHOW STORAGE UNITS FROM foo_db", Map.of("name", "foo_ds")),
                Arguments.of("storage unit", "shardingsphere://databases/{database}/storage-units/{storageUnit}", "shardingsphere://databases/foo_db/storage-units/foo_ds",
                        "SHOW STORAGE UNITS FROM foo_db", Map.of("name", "foo_ds")),
                Arguments.of("storage unit rules", "shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules",
                        "shardingsphere://databases/foo_db/storage-units/foo_ds/used-by-rules",
                        "SHOW RULES USED STORAGE UNIT foo_ds FROM foo_db", Map.of("name", "foo_rule")),
                Arguments.of("single tables", "shardingsphere://databases/{database}/single-tables", "shardingsphere://databases/foo_db/single-tables",
                        "SHOW SINGLE TABLES FROM foo_db", Map.of("table_name", "foo_table")),
                Arguments.of("single table", "shardingsphere://databases/{database}/single-tables/{table}", "shardingsphere://databases/foo_db/single-tables/foo_table",
                        "SHOW SINGLE TABLE foo_table FROM foo_db", Map.of("table_name", "foo_table")),
                Arguments.of("default single storage unit", "shardingsphere://databases/{database}/single-table/default-storage-unit",
                        "shardingsphere://databases/foo_db/single-table/default-storage-unit",
                        "SHOW DEFAULT SINGLE TABLE STORAGE UNIT FROM foo_db", Map.of("storage_unit_name", "foo_ds")));
    }
    
    @Test
    void assertGetToolHandlers() {
        Collection<MCPToolHandler<?>> actual = new CoreHandlerProvider().getToolHandlers();
        assertThat(actual.stream().map(MCPToolHandler::getToolName).toList(),
                is(List.of("database_gateway_search_metadata", "database_gateway_validate_runtime_database", "database_gateway_execute_query", "database_gateway_execute_explain_query",
                        "database_gateway_execute_update", "database_gateway_apply_workflow", "database_gateway_validate_workflow")));
    }
    
    @Test
    void assertGetResourceHandlers() {
        Collection<MCPResourceHandler<?>> actual = new CoreHandlerProvider().getResourceHandlers();
        assertThat(actual.size(), is(27));
        List<String> actualUris = actual.stream().map(MCPResourceHandler::getResourceUriTemplate).toList();
        assertTrue(actualUris.contains("shardingsphere://capabilities"));
        assertTrue(actualUris.contains("shardingsphere://guidance"));
        assertTrue(actualUris.contains("shardingsphere://runtime"));
        assertTrue(actualUris.contains("shardingsphere://workflows/{plan_id}"));
        assertTrue(actualUris.contains("shardingsphere://databases/{database}/storage-units/{storageUnit}"));
        assertTrue(actualUris.contains("shardingsphere://databases/{database}/single-table/default-storage-unit"));
        assertTrue(actualUris.contains("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"));
    }
    
    @Test
    void assertGetCompletionHandlers() {
        Collection<MCPCompletionHandler<?>> actual = new CoreHandlerProvider().getCompletionHandlers();
        assertThat(actual.stream().map(Object::getClass).toList(), is(List.of(MetadataCompletionHandler.class, WorkflowPlanIdCompletionHandler.class)));
    }
}
