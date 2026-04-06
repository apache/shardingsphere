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

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.MetadataSearchRequest;
import org.apache.shardingsphere.mcp.tool.MetadataSearchResult;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.tool.response.MCPMetadataResponse;

import java.util.Arrays;
import java.util.Map;

/**
 * Handler for search-metadata tool.
 */
public final class SearchMetadataToolHandler implements ToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = new MCPToolDescriptor("search_metadata",
            Arrays.asList(
                    new MCPToolFieldDefinition("database", new MCPToolValueDefinition(Type.STRING, "Optional logical database name.", null), false),
                    new MCPToolFieldDefinition("schema", new MCPToolValueDefinition(Type.STRING, "Optional schema name.", null), false),
                    new MCPToolFieldDefinition("query", new MCPToolValueDefinition(Type.STRING, "Search query.", null), true),
                    new MCPToolFieldDefinition("object_types", new MCPToolValueDefinition(Type.ARRAY, "Optional object-type filter.",
                            new MCPToolValueDefinition(Type.STRING, "Array element value.", null)), false),
                    new MCPToolFieldDefinition("page_size", new MCPToolValueDefinition(Type.INTEGER, "Requested page size.", null), false),
                    new MCPToolFieldDefinition("page_token", new MCPToolValueDefinition(Type.STRING, "Opaque pagination token.", null), false)));
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPRuntimeContext runtimeContext, final String sessionId, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        MetadataSearchRequest request = new MetadataSearchRequest(
                toolArguments.getStringArgument("database"), toolArguments.getStringArgument("schema"), toolArguments.getStringArgument("query"),
                toolArguments.getObjectTypes(), toolArguments.getIntegerArgument("page_size", 100), toolArguments.getStringArgument("page_token"));
        MetadataSearchResult searchResult = new SearchMetadataToolService(runtimeContext.getMetadataCatalog()).execute(request);
        return new MCPMetadataResponse(searchResult.getItems(), searchResult.getNextPageToken());
    }
}
