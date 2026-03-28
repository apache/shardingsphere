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
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportPayloadBuilder;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP tool specification factory.
 */
public final class MCPToolSpecificationFactory {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPToolCallHandler toolCallHandler;
    
    /**
     * Create MCP tool specification factory.
     *
     * @param runtimeContext runtime context
     * @param payloadBuilder payload builder
     */
    public MCPToolSpecificationFactory(final MCPRuntimeContext runtimeContext, final MCPTransportPayloadBuilder payloadBuilder) {
        this.runtimeContext = runtimeContext;
        toolCallHandler = new MCPToolCallHandler(runtimeContext, payloadBuilder);
    }
    
    /**
     * Create MCP tool specifications.
     *
     * @return tool specifications
     */
    public List<SyncToolSpecification> createToolSpecifications() {
        List<SyncToolSpecification> result = new ArrayList<>();
        for (String each : runtimeContext.getCapabilityAssembler().assembleServiceCapability().getSupportedTools()) {
            MCPToolDefinition toolDefinition = MCPToolDefinition.findByName(each)
                    .orElseThrow(() -> new IllegalStateException(String.format("Unknown MCP tool `%s`.", each)));
            result.add(new SyncToolSpecification.Builder()
                    .tool(createToolDefinition(toolDefinition))
                    .callHandler(toolCallHandler::handle)
                    .build());
        }
        return result;
    }
    
    private McpSchema.Tool createToolDefinition(final MCPToolDefinition toolDefinition) {
        return McpSchema.Tool.builder()
                .name(toolDefinition.getName())
                .title(toolDefinition.getTitle())
                .description("ShardingSphere MCP tool: " + toolDefinition.getName())
                .inputSchema(toolDefinition.createInputSchema())
                .build();
    }
}
