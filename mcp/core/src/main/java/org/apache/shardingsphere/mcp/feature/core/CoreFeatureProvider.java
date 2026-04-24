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

package org.apache.shardingsphere.mcp.feature.core;

import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.resource.handler.capability.DatabaseCapabilitiesHandler;
import org.apache.shardingsphere.mcp.resource.handler.capability.ServiceCapabilitiesHandler;
import org.apache.shardingsphere.mcp.resource.handler.metadata.MetadataResourceHandler;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.execute.ExecuteSQLToolHandler;
import org.apache.shardingsphere.mcp.tool.handler.metadata.SearchMetadataToolHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Core MCP feature provider.
 */
public final class CoreFeatureProvider implements MCPFeatureProvider {
    
    @Override
    public Collection<ToolHandler> getToolHandlers() {
        return List.of(new SearchMetadataToolHandler(), new ExecuteSQLToolHandler());
    }
    
    @Override
    public Collection<ResourceHandler> getResourceHandlers() {
        return List.of(
                new ServiceCapabilitiesHandler(),
                new DatabaseCapabilitiesHandler(),
                new MetadataResourceHandler("shardingsphere://databases", (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryDatabases()),
                new MetadataResourceHandler("shardingsphere://databases/{database}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryDatabase(uriVariables.getVariable("database")))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySchemas(uriVariables.getVariable("database"))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}",
                        (requestContext, uriVariables) -> singletonOrEmpty(
                                requestContext.getMetadataQueryFacade().querySchema(uriVariables.getVariable("database"), uriVariables.getVariable("schema")))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/sequences",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().querySequences(uriVariables.getVariable("database"), uriVariables.getVariable("schema"))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().querySequence(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("sequence")))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTables(uriVariables.getVariable("database"), uriVariables.getVariable("schema"))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/views",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViews(uriVariables.getVariable("database"), uriVariables.getVariable("schema"))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryTable(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table")))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryTableColumns(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table"))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryTableColumn(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table"), uriVariables.getVariable("column")))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/views/{view}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryView(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("view")))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryViewColumns(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("view"))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryViewColumn(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("view"), uriVariables.getVariable("column")))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes",
                        (requestContext, uriVariables) -> requestContext.getMetadataQueryFacade().queryIndexes(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table"))),
                new MetadataResourceHandler("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}",
                        (requestContext, uriVariables) -> singletonOrEmpty(requestContext.getMetadataQueryFacade().queryIndex(
                                uriVariables.getVariable("database"), uriVariables.getVariable("schema"), uriVariables.getVariable("table"), uriVariables.getVariable("index")))));
    }
    
    private static List<?> singletonOrEmpty(final Optional<?> metadata) {
        return metadata.map(Collections::singletonList).orElse(Collections.emptyList());
    }
}
