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

package org.apache.shardingsphere.mcp.core.resource.handler;

import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory.RequestScopeFixture;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.DatabaseCapabilitiesHandler;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerCapabilitiesHandler;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerGuidanceHandler;
import org.apache.shardingsphere.mcp.core.resource.handler.metadata.MetadataResourceHandler;
import org.apache.shardingsphere.mcp.core.resource.uri.MCPUriPattern;
import org.apache.shardingsphere.mcp.support.database.response.MCPDatabaseCapabilityResponse;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPItemsResponse;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreResourceHandlerSurfaceTest {
    
    private final MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertGetResourceDescriptor(final HandlerCase handlerCase) {
        MCPResourceDescriptor actual = MCPDescriptorCatalogIndex.getRequiredResourceDescriptor(handlerCase.getHandler().getResourceUriTemplate());
        assertThat(actual.getUriTemplate(), is(handlerCase.getExpectedUriTemplate()));
        assertFalse(actual.getDescription().isBlank());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertHandle(final HandlerCase handlerCase) {
        try (RequestScopeFixture requestScopeFixture = ResourceTestDataFactory.createRequestScopeFixture(runtimeContext, ResourceTestDataFactory.createDatabaseMetadata())) {
            MCPRequestScope requestContext = requestScopeFixture.getRequestScope();
            MCPResponse actual = handle(handlerCase.getHandler(), requestContext, parseUriVariables(handlerCase.getExpectedUriTemplate(), handlerCase.getResourceUri()));
            Map<String, Object> actualPayload = actual.toPayload();
            if (HandlerResultType.DATABASE_CAPABILITY == handlerCase.getExpectedType()) {
                assertThat(actual, isA(MCPDatabaseCapabilityResponse.class));
                assertThat(actualPayload.get("database"), is(handlerCase.getExpectedDatabase()));
                return;
            }
            if (HandlerResultType.SERVICE_CAPABILITY == handlerCase.getExpectedType()) {
                assertThat(actual, isA(MCPMapResponse.class));
                assertTrue(((List<?>) actualPayload.get("supportedResources")).contains("shardingsphere://capabilities"));
                assertTrue(((List<?>) actualPayload.get("supportedResources")).contains("shardingsphere://guidance"));
                assertTrue(((List<?>) actualPayload.get("prompts")).stream().map(String::valueOf).anyMatch(each -> each.contains("inspect_metadata")));
                assertTrue(((List<?>) actualPayload.get("completionTargets")).stream().map(String::valueOf).anyMatch(each -> each.contains("inspect_metadata")));
                assertTrue(((List<?>) actualPayload.get("resourceNavigation")).stream().map(String::valueOf).anyMatch(each -> each.contains("database_gateway_apply_workflow")));
                assertFalse(actualPayload.containsKey("fingerprints"));
                assertTrue((Boolean) ((Map<?, ?>) actualPayload.get("protocolAvailability")).get("resourceNavigation"));
                return;
            }
            if (HandlerResultType.SERVICE_GUIDANCE == handlerCase.getExpectedType()) {
                assertThat(actual, isA(MCPMapResponse.class));
                assertThat(actualPayload.get("response_mode"), is("guidance"));
                assertThat(actualPayload.get("guidance_resource"), is("shardingsphere://guidance"));
                assertTrue(actualPayload.containsKey("model_contract"));
                assertTrue(actualPayload.containsKey("common_flows"));
                return;
            }
            assertMetadataResponse(handlerCase, actual, actualPayload);
        }
    }
    
    @Test
    void assertHandleWithoutIndexMetadata() {
        try (RequestScopeFixture requestScopeFixture = ResourceTestDataFactory.createRequestScopeFixture(runtimeContext, ResourceTestDataFactory.createDatabaseMetadata())) {
            MCPRequestScope requestContext = requestScopeFixture.getRequestScope();
            MCPResponse actual = new MetadataResourceHandler(
                    "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                    (featureContext, uriVariables) -> featureContext.getMetadataQueryFacade().queryIndexes(
                            uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table")))
                    .handle(requestContext,
                            parseUriVariables("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                                    "shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actual, isA(MCPItemsResponse.class));
            assertThat(actualPayload.get("count"), is(0));
            assertThat(actualPayload.get("self_uri"), is("shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes"));
        }
    }
    
    @Test
    void assertHandleWithUnsupportedSequenceResource() {
        MCPRequestScope requestContext = new MCPRequestScope(runtimeContext, "session-1");
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new MetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/sequences",
                (featureContext, uriVariables) -> featureContext.getMetadataQueryFacade().querySequences(
                        uriVariables.getValue("database"), uriVariables.getValue("schema")))
                .handle(requestContext,
                        parseUriVariables("shardingsphere://databases/{database}/schemas/{schema}/sequences",
                                "shardingsphere://databases/warehouse/schemas/warehouse/sequences")));
        assertThat(actual.getMessage(), is("Sequence resources are not supported for the current database."));
    }
    
    @Test
    void assertHandleWithUnsupportedStorageUnitResource() {
        MCPRequestScope requestContext = new MCPRequestScope(runtimeContext, "session-1");
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new MetadataResourceHandler(
                "shardingsphere://databases/{database}/storage-units",
                (featureContext, uriVariables) -> {
                    throw new MCPUnsupportedException("Storage unit resources are not supported for the current database.");
                }).handle(requestContext,
                        parseUriVariables("shardingsphere://databases/{database}/storage-units", "shardingsphere://databases/logic_db/storage-units")));
        assertThat(actual.getMessage(), is("Storage unit resources are not supported for the current database."));
    }
    
    private MCPUriVariables parseUriVariables(final String uriTemplate, final String resourceUri) {
        return new MCPUriPattern(uriTemplate).parse(resourceUri).orElseThrow();
    }
    
    private <T extends MCPRequestContext> MCPResponse handle(final MCPResourceHandler<T> handler, final MCPRequestScope requestContext, final MCPUriVariables uriVariables) {
        return handler.handle(handler.getContextType().cast(requestContext), uriVariables);
    }
    
    private void assertMetadataResponse(final HandlerCase handlerCase, final MCPResponse actual, final Map<String, Object> actualPayload) {
        if (actualPayload.containsKey("resource_kind")) {
            assertThat(actual, isA(MCPMapResponse.class));
            assertThat(actualPayload.get("resource_kind"), is("detail"));
            assertThat(actualPayload.get("found"), is(!handlerCase.getExpectedObjectNames().isEmpty()));
        } else {
            assertThat(actual, isA(MCPItemsResponse.class));
        }
        assertThat(actualPayload.get("count"), is(handlerCase.getExpectedObjectNames().size()));
        assertThat(actualPayload.get("self_uri"), is(handlerCase.getResourceUri()));
        assertParentResource(handlerCase.getResourceUri(), actualPayload);
        assertNextResources(handlerCase.getResourceUri(), actualPayload);
        assertThat(extractMetadataNames(actualPayload), is(handlerCase.getExpectedObjectNames()));
    }
    
    private void assertParentResource(final String resourceUri, final Map<String, Object> actualPayload) {
        String expectedParentUri = createParentUri(resourceUri);
        if (expectedParentUri.isEmpty()) {
            assertFalse(actualPayload.containsKey("parent_resource"));
            return;
        }
        assertThat(((Map<?, ?>) actualPayload.get("parent_resource")).get("uri"), is(expectedParentUri));
    }
    
    private String createParentUri(final String resourceUri) {
        String prefix = "shardingsphere://";
        String path = resourceUri.substring(prefix.length());
        int lastSeparatorIndex = path.lastIndexOf('/');
        return 0 > lastSeparatorIndex ? "" : prefix + path.substring(0, lastSeparatorIndex);
    }
    
    private void assertNextResources(final String resourceUri, final Map<String, Object> actualPayload) {
        if (!"shardingsphere://databases/logic_db/schemas/public/tables/orders".equals(resourceUri)) {
            return;
        }
        assertThat(extractResourceUris((List<?>) actualPayload.get("next_resources")), is(List.of(
                "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns",
                "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes")));
    }
    
    private List<String> extractResourceUris(final List<?> resources) {
        List<String> result = new LinkedList<>();
        for (Object each : resources) {
            result.add((String) ((Map<?, ?>) each).get("uri"));
        }
        return result;
    }
    
    private List<String> extractMetadataNames(final Map<String, Object> payload) {
        List<String> result = new LinkedList<>();
        for (Object each : getMetadataItems(payload)) {
            if (each instanceof Map) {
                result.add(extractMetadataName((Map<?, ?>) each));
            }
        }
        return result;
    }
    
    private String extractMetadataName(final Map<?, ?> metadata) {
        for (String each : List.of("sequence", "index", "column", "view", "table", "schema", "database")) {
            if (metadata.containsKey(each)) {
                return String.valueOf(metadata.get(each));
            }
        }
        if (metadata.containsKey("name")) {
            return String.valueOf(metadata.get("name"));
        }
        if (metadata.containsKey("table_name")) {
            return String.valueOf(metadata.get("table_name"));
        }
        return metadata.containsKey("storage_unit_name") ? String.valueOf(metadata.get("storage_unit_name")) : "";
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> getMetadataItems(final Map<String, Object> payload) {
        return (List<Object>) payload.get("items");
    }
    
    private static Stream<HandlerCase> handlerCases() {
        return Stream.of(
                new HandlerCase("server capabilities", new ServerCapabilitiesHandler(), "shardingsphere://capabilities",
                        "shardingsphere://capabilities", HandlerResultType.SERVICE_CAPABILITY, "", List.of()),
                new HandlerCase("server guidance", new ServerGuidanceHandler(), "shardingsphere://guidance",
                        "shardingsphere://guidance", HandlerResultType.SERVICE_GUIDANCE, "", List.of()),
                new HandlerCase("databases", new MetadataResourceHandler("shardingsphere://databases",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryDatabases()), "shardingsphere://databases",
                        "shardingsphere://databases", HandlerResultType.METADATA, "", List.of("logic_db", "runtime_db", "warehouse")),
                new HandlerCase("database", new MetadataResourceHandler("shardingsphere://databases/{database}",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryDatabase(uriVariables.getValue("database"))
                                .map(CoreResourceHandlerSurfaceTest::createSingletonList).orElse(Collections.emptyList())),
                        "shardingsphere://databases/{database}",
                        "shardingsphere://databases/logic_db", HandlerResultType.METADATA, "", List.of("logic_db")),
                new HandlerCase("database storage units", new MetadataResourceHandler("shardingsphere://databases/{database}/storage-units",
                        (requestContext, uriVariables) -> List.of(Map.of("name", "write_ds"))),
                        "shardingsphere://databases/{database}/storage-units",
                        "shardingsphere://databases/logic_db/storage-units", HandlerResultType.METADATA, "", List.of("write_ds")),
                new HandlerCase("database storage unit", new MetadataResourceHandler("shardingsphere://databases/{database}/storage-units/{storageUnit}",
                        (requestContext, uriVariables) -> List.of(Map.of("name", uriVariables.getValue("storageUnit")))),
                        "shardingsphere://databases/{database}/storage-units/{storageUnit}",
                        "shardingsphere://databases/logic_db/storage-units/write_ds", HandlerResultType.METADATA, "", List.of("write_ds")),
                new HandlerCase("database storage unit used by rules",
                        new MetadataResourceHandler("shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules",
                                (requestContext, uriVariables) -> List.of(Map.of("type", "readwrite_splitting", "name", "ms_group_0"))),
                        "shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules",
                        "shardingsphere://databases/logic_db/storage-units/write_ds/used-by-rules", HandlerResultType.METADATA, "", List.of("ms_group_0")),
                new HandlerCase("database single tables", new MetadataResourceHandler("shardingsphere://databases/{database}/single-tables",
                        (requestContext, uriVariables) -> List.of(Map.of("table_name", "t_user", "storage_unit_name", "ds_0"))),
                        "shardingsphere://databases/{database}/single-tables",
                        "shardingsphere://databases/logic_db/single-tables", HandlerResultType.METADATA, "", List.of("t_user")),
                new HandlerCase("database single table", new MetadataResourceHandler("shardingsphere://databases/{database}/single-tables/{table}",
                        (requestContext, uriVariables) -> List.of(Map.of("table_name", uriVariables.getValue("table"), "storage_unit_name", "ds_0"))),
                        "shardingsphere://databases/{database}/single-tables/{table}",
                        "shardingsphere://databases/logic_db/single-tables/t_user", HandlerResultType.METADATA, "", List.of("t_user")),
                new HandlerCase("database default single table storage unit",
                        new MetadataResourceHandler("shardingsphere://databases/{database}/single-table/default-storage-unit",
                                (requestContext, uriVariables) -> List.of(Map.of("storage_unit_name", "ds_0"))),
                        "shardingsphere://databases/{database}/single-table/default-storage-unit",
                        "shardingsphere://databases/logic_db/single-table/default-storage-unit", HandlerResultType.METADATA, "", List.of("ds_0")),
                new HandlerCase("database capabilities", new DatabaseCapabilitiesHandler(), "shardingsphere://databases/{database}/capabilities",
                        "shardingsphere://databases/logic_db/capabilities", HandlerResultType.DATABASE_CAPABILITY, "logic_db", List.of()),
                new HandlerCase("database schemas", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySchemas(uriVariables.getValue("database"))),
                        "shardingsphere://databases/{database}/schemas",
                        "shardingsphere://databases/logic_db/schemas", HandlerResultType.METADATA, "", List.of("public")),
                new HandlerCase("database schema sequences", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/sequences",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySequences(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/sequences",
                        "shardingsphere://databases/runtime_db/schemas/public/sequences", HandlerResultType.METADATA, "", List.of("order_seq")),
                new HandlerCase("database schema sequence", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySequence(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("sequence"))
                                .map(CoreResourceHandlerSurfaceTest::createSingletonList).orElse(Collections.emptyList())),
                        "shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                        "shardingsphere://databases/runtime_db/schemas/public/sequences/order_seq", HandlerResultType.METADATA, "", List.of("order_seq")),
                new HandlerCase("database schema", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySchema(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"))
                                .map(CoreResourceHandlerSurfaceTest::createSingletonList).orElse(Collections.emptyList())),
                        "shardingsphere://databases/{database}/schemas/{schema}",
                        "shardingsphere://databases/logic_db/schemas/public", HandlerResultType.METADATA, "", List.of("public")),
                new HandlerCase("database schema tables", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTables(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables",
                        "shardingsphere://databases/logic_db/schemas/public/tables", HandlerResultType.METADATA, "", List.of("order_items", "orders")),
                new HandlerCase("database schema views", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/views",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViews(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/views",
                        "shardingsphere://databases/logic_db/schemas/public/views", HandlerResultType.METADATA, "", List.of("orders_view")),
                new HandlerCase("database schema table", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTable(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"))
                                .map(CoreResourceHandlerSurfaceTest::createSingletonList).orElse(Collections.emptyList())),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders", HandlerResultType.METADATA, "", List.of("orders")),
                new HandlerCase("database schema table columns", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTableColumns(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema table column", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTableColumn(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"), uriVariables.getValue("column"))
                                .map(CoreResourceHandlerSurfaceTest::createSingletonList).orElse(Collections.emptyList())),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema view", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryView(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("view"))
                                .map(CoreResourceHandlerSurfaceTest::createSingletonList).orElse(Collections.emptyList())),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view", HandlerResultType.METADATA, "", List.of("orders_view")),
                new HandlerCase("database schema view columns", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViewColumns(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("view"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema view column", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViewColumn(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("view"), uriVariables.getValue("column"))
                                .map(CoreResourceHandlerSurfaceTest::createSingletonList).orElse(Collections.emptyList())),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns/order_id", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema table indexes", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryIndexes(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes", HandlerResultType.METADATA, "", List.of("order_idx")),
                new HandlerCase("database schema table index", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryIndex(
                                uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"), uriVariables.getValue("index"))
                                .map(CoreResourceHandlerSurfaceTest::createSingletonList).orElse(Collections.emptyList())),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes/order_idx", HandlerResultType.METADATA, "", List.of("order_idx")));
    }
    
    private static List<?> createSingletonList(final Object metadata) {
        return Collections.singletonList(metadata);
    }
    
    private static final class HandlerCase {
        
        private final String description;
        
        private final MCPResourceHandler<?> handler;
        
        private final String expectedUriTemplate;
        
        private final String resourceUri;
        
        private final HandlerResultType expectedType;
        
        private final String expectedDatabase;
        
        private final List<String> expectedObjectNames;
        
        private HandlerCase(final String description, final MCPResourceHandler<?> handler, final String expectedUriTemplate, final String resourceUri,
                            final HandlerResultType expectedType, final String expectedDatabase, final List<String> expectedObjectNames) {
            this.description = description;
            this.handler = handler;
            this.expectedUriTemplate = expectedUriTemplate;
            this.resourceUri = resourceUri;
            this.expectedType = expectedType;
            this.expectedDatabase = expectedDatabase;
            this.expectedObjectNames = expectedObjectNames;
        }
        
        private MCPResourceHandler<?> getHandler() {
            return handler;
        }
        
        private String getExpectedUriTemplate() {
            return expectedUriTemplate;
        }
        
        private String getResourceUri() {
            return resourceUri;
        }
        
        private HandlerResultType getExpectedType() {
            return expectedType;
        }
        
        private String getExpectedDatabase() {
            return expectedDatabase;
        }
        
        private List<String> getExpectedObjectNames() {
            return expectedObjectNames;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
    
    private enum HandlerResultType {
        
        SERVICE_CAPABILITY,
        
        SERVICE_GUIDANCE,
        
        DATABASE_CAPABILITY,
        
        METADATA
    }
}
