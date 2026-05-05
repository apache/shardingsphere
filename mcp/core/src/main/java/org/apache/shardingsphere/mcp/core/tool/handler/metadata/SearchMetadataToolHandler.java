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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPItemsResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.core.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.core.tool.response.MetadataSearchResult;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handler for search-metadata tool.
 */
public final class SearchMetadataToolHandler implements MCPToolHandler<MCPDatabaseHandlerContext> {
    
    private static final Set<SupportedMCPMetadataObjectType> SUPPORTED_OBJECT_TYPES = Set.of(
            SupportedMCPMetadataObjectType.DATABASE, SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE,
            SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN, SupportedMCPMetadataObjectType.INDEX, SupportedMCPMetadataObjectType.SEQUENCE);
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = MCPDescriptorRegistry.getRequiredToolDescriptor("search_metadata");
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext databaseContext, final MCPToolCall toolCall) {
        MCPToolArguments toolArguments = new MCPToolArguments(toolCall.getArguments());
        MetadataSearchRequest request = new MetadataSearchRequest(
                toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), toolArguments.getStringArgument("query"),
                toolArguments.getObjectTypes(SUPPORTED_OBJECT_TYPES),
                toolArguments.getIntegerArgument("page_size", SearchMetadataToolService.DEFAULT_PAGE_SIZE, 1, SearchMetadataToolService.MAX_PAGE_SIZE),
                toolArguments.getStringArgument("page_token"));
        MetadataSearchResult searchResult = new SearchMetadataToolService(databaseContext.getMetadataQueryFacade()).execute(request);
        return new MCPItemsResponse(searchResult.getItems(), searchResult.getNextPageToken(), createSearchPayloadMetadata(request, searchResult));
    }
    
    private Map<String, Object> createSearchPayloadMetadata(final MetadataSearchRequest request, final MetadataSearchResult searchResult) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("search_context", searchResult.getSearchContext());
        if (searchResult.getItems().isEmpty()) {
            result.put("empty_reason", request.getQuery().isEmpty() ? "no_metadata_in_scope" : "no_matches_for_query");
            result.put("next_actions", List.of(createEmptySearchNextAction(request)));
        }
        return result;
    }
    
    private Map<String, Object> createEmptySearchNextAction(final MetadataSearchRequest request) {
        if (!request.getQuery().isEmpty() || !request.getSchema().isEmpty()) {
            return MCPNextActionUtils.callTool("search_metadata", "Retry search_metadata with a broader scope.", createBroadenedSearchArguments(request), false);
        }
        return MCPNextActionUtils.readResource("shardingsphere://databases", "Read configured databases before choosing a narrower metadata search.");
    }
    
    private Map<String, Object> createBroadenedSearchArguments(final MetadataSearchRequest request) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        if (!request.getDatabase().isEmpty()) {
            result.put("database", request.getDatabase());
        }
        if (!request.getQuery().isEmpty()) {
            result.put("query", request.getQuery());
        }
        return result;
    }
}
