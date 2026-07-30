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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.DatabaseCapabilitiesHandler;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.RuntimeStatusHandler;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerCapabilitiesHandler;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerGuidanceHandler;
import org.apache.shardingsphere.mcp.core.metadata.GovernanceMetadataQueryService;
import org.apache.shardingsphere.mcp.core.resource.handler.metadata.MetadataResourceHandler;
import org.apache.shardingsphere.mcp.core.resource.handler.workflow.WorkflowPlanHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Core resource handlers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CoreResourceHandlers {
    
    static Collection<MCPResourceHandler<?>> createHandlers() {
        Collection<MCPResourceHandler<?>> result = new LinkedList<>();
        GovernanceMetadataQueryService governanceMetadataQueryService = new GovernanceMetadataQueryService();
        addTopLevelHandlers(result);
        addDatabaseHandlers(result);
        addGovernanceMetadataHandlers(result, governanceMetadataQueryService);
        addSchemaMetadataHandlers(result);
        return result;
    }
    
    private static void addTopLevelHandlers(final Collection<MCPResourceHandler<?>> result) {
        result.add(new ServerCapabilitiesHandler());
        result.add(new ServerGuidanceHandler());
        result.add(new RuntimeStatusHandler());
        result.add(new WorkflowPlanHandler());
        result.add(new DatabaseCapabilitiesHandler());
    }
    
    private static void addDatabaseHandlers(final Collection<MCPResourceHandler<?>> result) {
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryDatabases()));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryDatabase(uriVariables.getValue("database"))
                        .map(CoreResourceHandlers::createSingletonList).orElse(Collections.emptyList())));
    }
    
    private static void addGovernanceMetadataHandlers(final Collection<MCPResourceHandler<?>> result, final GovernanceMetadataQueryService governanceMetadataQueryService) {
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/storage-units",
                (requestContext, uriVariables) -> governanceMetadataQueryService.queryStorageUnits(requestContext.getQueryFacade(), uriVariables.getValue("database"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/storage-units/{storageUnit}",
                (requestContext, uriVariables) -> governanceMetadataQueryService.queryStorageUnit(
                        requestContext.getQueryFacade(), uriVariables.getValue("database"), uriVariables.getValue("storageUnit"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules",
                (requestContext, uriVariables) -> governanceMetadataQueryService.queryRulesUsedStorageUnit(
                        requestContext.getQueryFacade(), uriVariables.getValue("database"), uriVariables.getValue("storageUnit"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/single-tables",
                (requestContext, uriVariables) -> governanceMetadataQueryService.querySingleTables(requestContext.getQueryFacade(), uriVariables.getValue("database"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/single-tables/{table}",
                (requestContext, uriVariables) -> governanceMetadataQueryService.querySingleTable(
                        requestContext.getQueryFacade(), uriVariables.getValue("database"), uriVariables.getValue("table"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/single-table/default-storage-unit",
                (requestContext, uriVariables) -> governanceMetadataQueryService.queryDefaultSingleTableStorageUnit(requestContext.getQueryFacade(), uriVariables.getValue("database"))));
    }
    
    private static void addSchemaMetadataHandlers(final Collection<MCPResourceHandler<?>> result) {
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySchemas(uriVariables.getValue("database"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySchema(uriVariables.getValue("database"), uriVariables.getValue("schema"))
                        .map(CoreResourceHandlers::createSingletonList).orElse(Collections.emptyList())));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/sequences",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySequences(uriVariables.getValue("database"), uriVariables.getValue("schema"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySequence(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("sequence"))
                        .map(CoreResourceHandlers::createSingletonList).orElse(Collections.emptyList())));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/tables",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTables(uriVariables.getValue("database"), uriVariables.getValue("schema"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/views",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViews(uriVariables.getValue("database"), uriVariables.getValue("schema"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTable(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"))
                        .map(CoreResourceHandlers::createSingletonList).orElse(Collections.emptyList())));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTableColumns(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTableColumn(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"), uriVariables.getValue("column"))
                        .map(CoreResourceHandlers::createSingletonList).orElse(Collections.emptyList())));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryView(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("view"))
                        .map(CoreResourceHandlers::createSingletonList).orElse(Collections.emptyList())));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViewColumns(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("view"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViewColumn(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("view"), uriVariables.getValue("column"))
                        .map(CoreResourceHandlers::createSingletonList).orElse(Collections.emptyList())));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryIndexes(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"))));
        result.add(createMetadataResourceHandler(
                "shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryIndex(
                        uriVariables.getValue("database"), uriVariables.getValue("schema"), uriVariables.getValue("table"), uriVariables.getValue("index"))
                        .map(CoreResourceHandlers::createSingletonList).orElse(Collections.emptyList())));
    }
    
    private static List<?> createSingletonList(final Object metadata) {
        return Collections.singletonList(metadata);
    }
    
    private static MetadataResourceHandler createMetadataResourceHandler(final String uriTemplate, final BiFunction<MCPFeatureRequestContext, MCPResourceURIVariables, List<?>> metadataLoader) {
        return new MetadataResourceHandler(uriTemplate, metadataLoader);
    }
}
