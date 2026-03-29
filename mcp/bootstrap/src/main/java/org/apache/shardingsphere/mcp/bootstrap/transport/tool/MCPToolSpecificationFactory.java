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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification.Builder;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP tool specification factory.
 */
public final class MCPToolSpecificationFactory {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPToolCallHandler toolCallHandler;
    
    private final MCPToolInputSchemaFactory toolInputSchemaFactory;
    
    /**
     * Create MCP tool specification factory.
     *
     * @param runtimeContext runtime context
     */
    public MCPToolSpecificationFactory(final MCPRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
        toolCallHandler = new MCPToolCallHandler(runtimeContext);
        toolInputSchemaFactory = new MCPToolInputSchemaFactory();
    }
    
    /**
     * Create MCP tool specifications.
     *
     * @return tool specifications
     */
    public List<SyncToolSpecification> createToolSpecifications() {
        return runtimeContext.getToolCatalog().getSupportedTools().stream()
                .map(each -> new Builder().tool(createToolDefinition(each)).callHandler(toolCallHandler::handle).build()).collect(Collectors.toList());
    }
    
    private McpSchema.Tool createToolDefinition(final String toolName) {
        return McpSchema.Tool.builder()
                .name(toolName)
                .title(runtimeContext.getToolCatalog().getTitle(toolName))
                .description("ShardingSphere MCP tool: " + toolName)
                .inputSchema(toolInputSchemaFactory.createInputSchema(toolName))
                .build();
    }
}
