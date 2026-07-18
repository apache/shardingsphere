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

package org.apache.shardingsphere.mcp.core.tool.handler.metadata;

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.payload.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPItemsPayload;

import java.util.Map;
import java.util.Set;

/**
 * Handler for search-metadata tool.
 */
public final class SearchMetadataToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private static final String TOOL_NAME = "database_gateway_search_metadata";
    
    private static final Set<SupportedMCPMetadataObjectType> SUPPORTED_OBJECT_TYPES = Set.of(
            SupportedMCPMetadataObjectType.DATABASE, SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE,
            SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN, SupportedMCPMetadataObjectType.INDEX,
            SupportedMCPMetadataObjectType.STORAGE_UNIT, SupportedMCPMetadataObjectType.SEQUENCE);
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        String query = toolArguments.getStringArgument("query");
        MetadataSearchRequest request = new MetadataSearchRequest(
                toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), query,
                toolArguments.getObjectTypes(SUPPORTED_OBJECT_TYPES));
        MetadataSearchResult searchResult = new SearchMetadataToolService(requestContext.getMetadataQueryFacade(), requestContext.getQueryFacade()).execute(request);
        return new MCPItemsPayload(searchResult.getItems(), "", SearchMetadataPayloadBuilder.build(requestContext, request, searchResult, TOOL_NAME), MCPResponseMode.SEARCH);
    }
}
