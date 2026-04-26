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

package org.apache.shardingsphere.mcp.resource.handler;

import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.response.MCPMetadataResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.resource.handler.capability.DatabaseCapabilitiesHandler;
import org.apache.shardingsphere.mcp.resource.handler.capability.ServiceCapabilitiesHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.MetadataResourceHandler;
import org.apache.shardingsphere.mcp.resource.response.MCPDatabaseCapabilityResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPServiceCapabilityResponse;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriPattern;
import org.apache.shardingsphere.mcp.resource.uri.MCPUriVariables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceHandlerTest {
    
    private final MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertGetUriPattern(final HandlerCase handlerCase) {
        assertThat(handlerCase.getHandler().getUriPattern(), is(handlerCase.getExpectedUriPattern()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertHandle(final HandlerCase handlerCase) {
        try (MCPRequestContext requestContext = new MCPRequestContext(runtimeContext)) {
            MCPResponse actual = handlerCase.getHandler().handle(requestContext, match(handlerCase.getExpectedUriPattern(), handlerCase.getResourceUri()));
            Map<String, Object> actualPayload = actual.toPayload();
            if (HandlerResultType.DATABASE_CAPABILITY == handlerCase.getExpectedType()) {
                assertThat(actual, org.hamcrest.Matchers.instanceOf(MCPDatabaseCapabilityResponse.class));
                assertThat(actualPayload.get("database"), is(handlerCase.getExpectedDatabase()));
                return;
            }
            if (HandlerResultType.SERVICE_CAPABILITY == handlerCase.getExpectedType()) {
                assertThat(actual, org.hamcrest.Matchers.instanceOf(MCPServiceCapabilityResponse.class));
                assertTrue(((List<?>) actualPayload.get("supportedResources")).contains("shardingsphere://capabilities"));
                return;
            }
            assertThat(actual, org.hamcrest.Matchers.instanceOf(MCPMetadataResponse.class));
            assertThat(extractMetadataNames(actualPayload), is(handlerCase.getExpectedObjectNames()));
        }
    }
    
    @Test
    void assertHandleWithUnsupportedIndexResource() {
        try (MCPRequestContext requestContext = new MCPRequestContext(runtimeContext)) {
            MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new MetadataResourceHandler(
                    "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                    (featureContext, uriVariables) -> featureContext.getMetadataQueryFacade().queryIndexes(
                            uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table")))
                    .handle(requestContext,
                            match("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                                    "shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes")));
            assertThat(actual.getMessage(), is("Index resources are not supported for the current database."));
        }
    }
    
    @Test
    void assertHandleWithUnsupportedSequenceResource() {
        try (MCPRequestContext requestContext = new MCPRequestContext(runtimeContext)) {
            MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> new MetadataResourceHandler(
                    "shardingsphere://databases/{database}/schemas/{schema}/sequences",
                    (featureContext, uriVariables) -> featureContext.getMetadataQueryFacade().querySequences(
                            uriVariables.getVariable("database"), uriVariables.getVariable("schema")))
                    .handle(requestContext,
                            match("shardingsphere://databases/{database}/schemas/{schema}/sequences",
                                    "shardingsphere://databases/warehouse/schemas/warehouse/sequences")));
            assertThat(actual.getMessage(), is("Sequence resources are not supported for the current database."));
        }
    }
    
    private MCPUriVariables match(final String uriPattern, final String resourceUri) {
        return new MCPUriPattern(uriPattern).parse(resourceUri).orElseThrow();
    }
    
    private List<String> extractMetadataNames(final Map<String, Object> payload) {
        List<String> result = new LinkedList<>();
        for (Object each : getMetadataItems(payload)) {
            if (each instanceof MCPDatabaseMetadata) {
                result.add(((MCPDatabaseMetadata) each).getDatabase());
                continue;
            }
            if (each instanceof MCPSchemaMetadata) {
                result.add(((MCPSchemaMetadata) each).getSchema());
                continue;
            }
            if (each instanceof MCPTableMetadata) {
                result.add(((MCPTableMetadata) each).getTable());
                continue;
            }
            if (each instanceof MCPViewMetadata) {
                result.add(((MCPViewMetadata) each).getView());
                continue;
            }
            if (each instanceof MCPColumnMetadata) {
                result.add(((MCPColumnMetadata) each).getColumn());
                continue;
            }
            if (each instanceof MCPIndexMetadata) {
                result.add(((MCPIndexMetadata) each).getIndex());
                continue;
            }
            if (each instanceof MCPSequenceMetadata) {
                result.add(((MCPSequenceMetadata) each).getSequence());
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> getMetadataItems(final Map<String, Object> payload) {
        return (List<Object>) payload.get("items");
    }
    
    private static Stream<HandlerCase> handlerCases() {
        return Stream.of(
                new HandlerCase("service capabilities", new ServiceCapabilitiesHandler(), "shardingsphere://capabilities",
                        "shardingsphere://capabilities", HandlerResultType.SERVICE_CAPABILITY, "", List.of()),
                new HandlerCase("databases", new MetadataResourceHandler("shardingsphere://databases",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryDatabases()), "shardingsphere://databases",
                        "shardingsphere://databases", HandlerResultType.METADATA, "", List.of("logic_db", "runtime_db", "warehouse")),
                new HandlerCase("database", new MetadataResourceHandler("shardingsphere://databases/{database}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryDatabase(uriVariables.getVariable("database")))),
                        "shardingsphere://databases/{database}",
                        "shardingsphere://databases/logic_db", HandlerResultType.METADATA, "", List.of("logic_db")),
                new HandlerCase("database capabilities", new DatabaseCapabilitiesHandler(), "shardingsphere://databases/{database}/capabilities",
                        "shardingsphere://databases/logic_db/capabilities", HandlerResultType.DATABASE_CAPABILITY, "logic_db", List.of()),
                new HandlerCase("database schemas", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySchemas(uriVariables.getVariable("database"))),
                        "shardingsphere://databases/{database}/schemas",
                        "shardingsphere://databases/logic_db/schemas", HandlerResultType.METADATA, "", List.of("public")),
                new HandlerCase("database schema sequences", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/sequences",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySequences(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/sequences",
                        "shardingsphere://databases/runtime_db/schemas/public/sequences", HandlerResultType.METADATA, "", List.of("order_seq")),
                new HandlerCase("database schema sequence", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().querySequence(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("sequence")))),
                        "shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                        "shardingsphere://databases/runtime_db/schemas/public/sequences/order_seq", HandlerResultType.METADATA, "", List.of("order_seq")),
                new HandlerCase("database schema", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().querySchema(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema")))),
                        "shardingsphere://databases/{database}/schemas/{schema}",
                        "shardingsphere://databases/logic_db/schemas/public", HandlerResultType.METADATA, "", List.of("public")),
                new HandlerCase("database schema tables", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTables(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables",
                        "shardingsphere://databases/logic_db/schemas/public/tables", HandlerResultType.METADATA, "", List.of("order_items", "orders")),
                new HandlerCase("database schema views", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/views",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViews(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/views",
                        "shardingsphere://databases/logic_db/schemas/public/views", HandlerResultType.METADATA, "", List.of("orders_view")),
                new HandlerCase("database schema table", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryTable(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table")))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders", HandlerResultType.METADATA, "", List.of("orders")),
                new HandlerCase("database schema table columns", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTableColumns(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema table column", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryTableColumn(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table"), uriVariables.getVariable("column")))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema view", new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryView(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("view")))),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view", HandlerResultType.METADATA, "", List.of("orders_view")),
                new HandlerCase("database schema view columns", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViewColumns(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("view"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema view column", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryViewColumn(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("view"), uriVariables.getVariable("column")))),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns/order_id", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema table indexes", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryIndexes(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table"))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes", HandlerResultType.METADATA, "", List.of("order_idx")),
                new HandlerCase("database schema table index", new MetadataResourceHandler(
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryIndex(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table"), uriVariables.getVariable("index")))),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes/order_idx", HandlerResultType.METADATA, "", List.of("order_idx")));
    }
    
    private static List<?> singletonOrEmpty(final Optional<?> metadata) {
        return metadata.map(Collections::singletonList).orElse(Collections.emptyList());
    }
    
    private static final class HandlerCase {
        
        private final String description;
        
        private final ResourceHandler handler;
        
        private final String expectedUriPattern;
        
        private final String resourceUri;
        
        private final HandlerResultType expectedType;
        
        private final String expectedDatabase;
        
        private final List<String> expectedObjectNames;
        
        private HandlerCase(final String description, final ResourceHandler handler, final String expectedUriPattern, final String resourceUri,
                            final HandlerResultType expectedType, final String expectedDatabase, final List<String> expectedObjectNames) {
            this.description = description;
            this.handler = handler;
            this.expectedUriPattern = expectedUriPattern;
            this.resourceUri = resourceUri;
            this.expectedType = expectedType;
            this.expectedDatabase = expectedDatabase;
            this.expectedObjectNames = expectedObjectNames;
        }
        
        private ResourceHandler getHandler() {
            return handler;
        }
        
        private String getExpectedUriPattern() {
            return expectedUriPattern;
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
        
        DATABASE_CAPABILITY,
        
        METADATA
    }
}
