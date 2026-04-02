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

import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.MetadataResourceQuery;
import org.apache.shardingsphere.mcp.resource.ResourceReadPlan;
import org.apache.shardingsphere.mcp.resource.ResourceReadPlan.ResourceReadPlanType;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceUriMatcher;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.dispatch.ResourceUriMatch;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ResourceHandlerTest {
    
    private final ResourceUriMatcher resourceUriMatcher = new ResourceUriMatcher();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertGetUriTemplate(final HandlerCase handlerCase) {
        assertThat(handlerCase.getHandler().getUriTemplate(), is(handlerCase.getExpectedUriTemplate()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("handlerCases")
    void assertHandle(final HandlerCase handlerCase) {
        Optional<ResourceUriMatch> uriMatch = resourceUriMatcher.match(handlerCase.getExpectedUriTemplate(), handlerCase.getResourceUri());
        
        ResourceReadPlan actual = handlerCase.getHandler().handle(uriMatch.orElseThrow());
        assertThat(actual.getType(), is(handlerCase.getExpectedType()));
        if (ResourceReadPlanType.DATABASE_CAPABILITIES == handlerCase.getExpectedType()) {
            assertThat(actual.getDatabase().orElse(""), is(handlerCase.getExpectedDatabase()));
            return;
        }
        if (ResourceReadPlanType.SERVICE_CAPABILITIES == handlerCase.getExpectedType()) {
            return;
        }
        MetadataResourceQuery actualQuery = actual.getMetadataResourceQuery().orElseThrow();
        assertThat(actualQuery.getDatabase(), is(handlerCase.getExpectedRequestDatabase()));
        assertThat(actualQuery.getSchema(), is(handlerCase.getExpectedRequestSchema()));
        assertThat(actualQuery.getObjectType(), is(handlerCase.getExpectedObjectType()));
        assertThat(actualQuery.getObjectName(), is(handlerCase.getExpectedObjectName()));
        assertThat(actualQuery.getParentObjectType(), is(handlerCase.getExpectedParentObjectType()));
        assertThat(actualQuery.getParentObjectName(), is(handlerCase.getExpectedParentObjectName()));
    }
    
    private static Stream<HandlerCase> handlerCases() {
        return Stream.of(
                new HandlerCase("service capabilities", new ServiceCapabilitiesHandler(), "shardingsphere://capabilities",
                        "shardingsphere://capabilities", ResourceReadPlanType.SERVICE_CAPABILITIES, "", null, "", "", "", "", ""),
                new HandlerCase("databases", new DatabasesHandler(), "shardingsphere://databases",
                        "shardingsphere://databases", ResourceReadPlanType.METADATA, "", MetadataObjectType.DATABASE, "", "", "", "", ""),
                new HandlerCase("database", new DatabaseHandler(), "shardingsphere://databases/{database}",
                        "shardingsphere://databases/logic_db", ResourceReadPlanType.METADATA, "", MetadataObjectType.DATABASE, "", "", "logic_db", "", ""),
                new HandlerCase("database capabilities", new DatabaseCapabilitiesHandler(), "shardingsphere://databases/{database}/capabilities",
                        "shardingsphere://databases/logic_db/capabilities", ResourceReadPlanType.DATABASE_CAPABILITIES, "logic_db", null, "", "", "", "", ""),
                new HandlerCase("database schemas", new DatabaseSchemasHandler(), "shardingsphere://databases/{database}/schemas",
                        "shardingsphere://databases/logic_db/schemas", ResourceReadPlanType.METADATA, "", MetadataObjectType.SCHEMA, "logic_db", "", "", "", ""),
                new HandlerCase("database schema", new DatabaseSchemaHandler(), "shardingsphere://databases/{database}/schemas/{schema}",
                        "shardingsphere://databases/logic_db/schemas/public", ResourceReadPlanType.METADATA, "", MetadataObjectType.SCHEMA, "logic_db", "public", "public", "", ""),
                new HandlerCase("database schema tables", new DatabaseSchemaTablesHandler(), "shardingsphere://databases/{database}/schemas/{schema}/tables",
                        "shardingsphere://databases/logic_db/schemas/public/tables", ResourceReadPlanType.METADATA, "", MetadataObjectType.TABLE, "logic_db", "public", "", "", ""),
                new HandlerCase("database schema views", new DatabaseSchemaViewsHandler(), "shardingsphere://databases/{database}/schemas/{schema}/views",
                        "shardingsphere://databases/logic_db/schemas/public/views", ResourceReadPlanType.METADATA, "", MetadataObjectType.VIEW, "logic_db", "public", "", "", ""),
                new HandlerCase("database schema table", new DatabaseSchemaTableHandler(), "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders", ResourceReadPlanType.METADATA, "", MetadataObjectType.TABLE, "logic_db", "public", "orders", "", ""),
                new HandlerCase("database schema table columns", new DatabaseSchemaTableColumnsHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns", ResourceReadPlanType.METADATA, "",
                        MetadataObjectType.COLUMN, "logic_db", "public", "", "TABLE", "orders"),
                new HandlerCase("database schema table column", new DatabaseSchemaTableColumnHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/columns/order_id", ResourceReadPlanType.METADATA, "",
                        MetadataObjectType.COLUMN, "logic_db", "public", "order_id", "TABLE", "orders"),
                new HandlerCase("database schema view", new DatabaseSchemaViewHandler(), "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view", ResourceReadPlanType.METADATA, "", MetadataObjectType.VIEW,
                        "logic_db", "public", "orders_view", "", ""),
                new HandlerCase("database schema view columns", new DatabaseSchemaViewColumnsHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns", ResourceReadPlanType.METADATA, "",
                        MetadataObjectType.COLUMN, "logic_db", "public", "", "VIEW", "orders_view"),
                new HandlerCase("database schema view column", new DatabaseSchemaViewColumnHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                        "shardingsphere://databases/logic_db/schemas/public/views/orders_view/columns/order_id", ResourceReadPlanType.METADATA, "",
                        MetadataObjectType.COLUMN, "logic_db", "public", "order_id", "VIEW", "orders_view"),
                new HandlerCase("database schema table indexes", new DatabaseSchemaTableIndexesHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes", ResourceReadPlanType.METADATA, "",
                        MetadataObjectType.INDEX, "logic_db", "public", "", "TABLE", "orders"),
                new HandlerCase("database schema table index", new DatabaseSchemaTableIndexHandler(),
                        "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes/order_idx", ResourceReadPlanType.METADATA, "",
                        MetadataObjectType.INDEX, "logic_db", "public", "order_idx", "TABLE", "orders"));
    }
    
    private static final class HandlerCase {
        
        private final String description;
        
        private final ResourceHandler handler;
        
        private final String expectedUriTemplate;
        
        private final String resourceUri;
        
        private final ResourceReadPlanType expectedType;
        
        private final String expectedDatabase;
        
        private final MetadataObjectType expectedObjectType;
        
        private final String expectedRequestDatabase;
        
        private final String expectedRequestSchema;
        
        private final String expectedObjectName;
        
        private final String expectedParentObjectType;
        
        private final String expectedParentObjectName;
        
        private HandlerCase(final String description, final ResourceHandler handler, final String expectedUriTemplate, final String resourceUri,
                            final ResourceReadPlanType expectedType, final String expectedDatabase, final MetadataObjectType expectedObjectType,
                            final String expectedRequestDatabase, final String expectedRequestSchema, final String expectedObjectName,
                            final String expectedParentObjectType, final String expectedParentObjectName) {
            this.description = description;
            this.handler = handler;
            this.expectedUriTemplate = expectedUriTemplate;
            this.resourceUri = resourceUri;
            this.expectedType = expectedType;
            this.expectedDatabase = expectedDatabase;
            this.expectedObjectType = expectedObjectType;
            this.expectedRequestDatabase = expectedRequestDatabase;
            this.expectedRequestSchema = expectedRequestSchema;
            this.expectedObjectName = expectedObjectName;
            this.expectedParentObjectType = expectedParentObjectType;
            this.expectedParentObjectName = expectedParentObjectName;
        }
        
        private ResourceHandler getHandler() {
            return handler;
        }
        
        private String getExpectedUriTemplate() {
            return expectedUriTemplate;
        }
        
        private String getResourceUri() {
            return resourceUri;
        }
        
        private ResourceReadPlanType getExpectedType() {
            return expectedType;
        }
        
        private String getExpectedDatabase() {
            return expectedDatabase;
        }
        
        private MetadataObjectType getExpectedObjectType() {
            return expectedObjectType;
        }
        
        private String getExpectedRequestDatabase() {
            return expectedRequestDatabase;
        }
        
        private String getExpectedRequestSchema() {
            return expectedRequestSchema;
        }
        
        private String getExpectedObjectName() {
            return expectedObjectName;
        }
        
        private String getExpectedParentObjectType() {
            return expectedParentObjectType;
        }
        
        private String getExpectedParentObjectName() {
            return expectedParentObjectName;
        }
        
        @Override
        public String toString() {
            return description;
        }
    }
}
