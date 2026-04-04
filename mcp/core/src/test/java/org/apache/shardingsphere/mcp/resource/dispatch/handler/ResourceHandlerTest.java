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

package org.apache.shardingsphere.mcp.resource.dispatch.handler;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObject;
import org.apache.shardingsphere.mcp.protocol.MCPPayloadBuilder;
import org.apache.shardingsphere.mcp.resource.response.MCPDatabaseCapabilityResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPMetadataResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPResourceResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPServiceCapabilityResponse;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceHandler;
import org.apache.shardingsphere.mcp.uri.MCPUriPattern;
import org.apache.shardingsphere.mcp.uri.MCPUriVariables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceHandlerTest {
    
    private final MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
    
    private final MCPPayloadBuilder payloadBuilder = new MCPPayloadBuilder();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertGetUriPattern(final HandlerCase handlerCase) {
        assertThat(handlerCase.getHandler().getUriPattern(), is(handlerCase.getExpectedUriPattern()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertHandle(final HandlerCase handlerCase) {
        MCPResourceResponse actual = handlerCase.getHandler().handle(runtimeContext, match(handlerCase.getExpectedUriPattern(), handlerCase.getResourceUri()));
        Map<String, Object> actualPayload = actual.toPayload(payloadBuilder);
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
        assertThat(getMetadataObjects(actualPayload).stream().map(MetadataObject::getName).toList(),
                is(handlerCase.getExpectedObjectNames()));
    }
    
    @Test
    void assertHandleWithUnsupportedIndexResource() {
        MCPResourceResponse actual = new DatabaseSchemaTableIndexesHandler().handle(runtimeContext,
                match("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                        "shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes"));
        assertThat(actual, org.hamcrest.Matchers.instanceOf(MCPErrorResponse.class));
        assertThat(actual.toPayload(payloadBuilder).get("error_code"), is("unsupported"));
    }

    private MCPUriVariables match(final String uriPattern, final String resourceUri) {
        return new MCPUriPattern(uriPattern).parse(resourceUri).orElseThrow();
    }
    
    @SuppressWarnings("unchecked")
    private List<MetadataObject> getMetadataObjects(final Map<String, Object> payload) {
        return (List<MetadataObject>) payload.get("items");
    }
    
    private static Stream<HandlerCase> handlerCases() {
        return Stream.of(
                new HandlerCase("service capabilities", new ServiceCapabilitiesHandler(), "shardingsphere://capabilities",
                        "shardingsphere://capabilities", HandlerResultType.SERVICE_CAPABILITY, "", List.of()),
                new HandlerCase("databases", new DatabasesHandler(), "shardingsphere://databases",
                        "shardingsphere://databases", HandlerResultType.METADATA, "", List.of("logic_db", "warehouse")),
                new HandlerCase("database", new DatabaseHandler(), "shardingsphere://databases/{database}",
                        "shardingsphere://databases/logic_db", HandlerResultType.METADATA, "", List.of("logic_db")),
                new HandlerCase("database capabilities", new DatabaseCapabilitiesHandler(), "shardingsphere://databases/{database}/capabilities",
                        "shardingsphere://databases/logic_db/capabilities", HandlerResultType.DATABASE_CAPABILITY, "logic_db", List.of()),
                new HandlerCase("database schemas", new DatabaseSchemasHandler(), "shardingsphere://databases/{database}/schemas",
                        "shardingsphere://databases/logic_db/schemas", HandlerResultType.METADATA, "", List.of("public")),
                new HandlerCase("database schema", new DatabaseSchemaHandler(), "shardingsphere://databases/{database}/schemas/{schema}",
                        "shardingsphere://databases/logic_db/schemas/public", HandlerResultType.METADATA, "", List.of("public")),
                new HandlerCase("database schema tables", new DatabaseSchemaTablesHandler(), "shardingsphere://databases/{database}/schemas/{schema}/tables",
                        "shardingsphere://databases/logic_db/schemas/public/tables", HandlerResultType.METADATA, "", List.of("order_items", "orders")),
                new HandlerCase("database schema views", new DatabaseSchemaViewsHandler(), "shardingsphere://databases/{database}/schemas/{schema}/views",
                        "shardingsphere://databases/logic_db/schemas/public/views", HandlerResultType.METADATA, "", List.of("orders_view")),
                new HandlerCase("database schema table", new DatabaseSchemaTableHandler(), "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders", HandlerResultType.METADATA, "", List.of("orders")),
                new HandlerCase("database schema table columns", new DatabaseSchemaTableColumnsHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema table column", new DatabaseSchemaTableColumnHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema view", new DatabaseSchemaViewHandler(), "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view", HandlerResultType.METADATA, "", List.of("orders_view")),
                new HandlerCase("database schema view columns", new DatabaseSchemaViewColumnsHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema view column", new DatabaseSchemaViewColumnHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns/order_id", HandlerResultType.METADATA, "", List.of("order_id")),
                new HandlerCase("database schema table indexes", new DatabaseSchemaTableIndexesHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes", HandlerResultType.METADATA, "", List.of("order_idx")),
                new HandlerCase("database schema table index", new DatabaseSchemaTableIndexHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes/order_idx", HandlerResultType.METADATA, "", List.of("order_idx")));
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
