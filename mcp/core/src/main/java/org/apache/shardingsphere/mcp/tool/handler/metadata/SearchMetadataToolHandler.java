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

package org.apache.shardingsphere.mcp.tool.handler.metadata;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPItemsResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.database.MCPDatabaseContext;
import org.apache.shardingsphere.mcp.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.tool.request.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.tool.response.MetadataSearchResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Handler for search-metadata tool.
 */
public final class SearchMetadataToolHandler implements MCPToolHandler<MCPDatabaseContext> {
    
    private static final Set<SupportedMCPMetadataObjectType> SUPPORTED_OBJECT_TYPES = Set.of(
            SupportedMCPMetadataObjectType.DATABASE, SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE,
            SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN, SupportedMCPMetadataObjectType.INDEX, SupportedMCPMetadataObjectType.SEQUENCE);
    
    private static final List<String> SUPPORTED_OBJECT_TYPE_NAMES = List.of("DATABASE", "SCHEMA", "TABLE", "VIEW", "COLUMN", "INDEX", "SEQUENCE");
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = new MCPToolDescriptor("search_metadata", "Search Metadata",
            "Search logical database metadata by object type, name, or pagination arguments.",
            Arrays.asList(
                    new MCPToolFieldDefinition("database", new MCPToolValueDefinition(Type.STRING, "Optional logical database name.", null), false),
                    new MCPToolFieldDefinition("schema", new MCPToolValueDefinition(Type.STRING, "Optional schema name.", null), false),
                    new MCPToolFieldDefinition("query", new MCPToolValueDefinition(Type.STRING, "Search query.", null), true),
                    new MCPToolFieldDefinition("object_types",
                            new MCPToolValueDefinition(Type.ARRAY, "Optional object-type filter. Allowed values: DATABASE, SCHEMA, TABLE, VIEW, COLUMN, INDEX, SEQUENCE.",
                                    new MCPToolValueDefinition(Type.STRING, "Allowed values: DATABASE, SCHEMA, TABLE, VIEW, COLUMN, INDEX, SEQUENCE.", null, SUPPORTED_OBJECT_TYPE_NAMES)),
                            false),
                    new MCPToolFieldDefinition("page_size", new MCPToolValueDefinition(Type.INTEGER, "Requested page size.", null), false),
                    new MCPToolFieldDefinition("page_token", new MCPToolValueDefinition(Type.STRING, "Opaque pagination token.", null), false)));
    
    @Override
    public Class<MCPDatabaseContext> getContextType() {
        return MCPDatabaseContext.class;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseContext databaseContext, final MCPToolCall toolCall) {
        MCPToolArguments toolArguments = new MCPToolArguments(toolCall.getArguments());
        MetadataSearchRequest request = new MetadataSearchRequest(
                toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), toolArguments.getStringArgument("query"),
                toolArguments.getObjectTypes(SUPPORTED_OBJECT_TYPES), toolArguments.getIntegerArgument("page_size", 100), toolArguments.getStringArgument("page_token"));
        MetadataSearchResult searchResult = new SearchMetadataToolService(databaseContext.getMetadataQueryFacade()).execute(request);
        return new MCPItemsResponse(searchResult.getItems(), searchResult.getNextPageToken());
    }
}
