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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import org.apache.shardingsphere.mcp.bootstrap.transport.resource.MCPResourceSpecificationFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.tool.MCPToolSpecificationFactory;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.util.Optional;

/**
 * MCP sync server factory.
 */
public final class MCPSyncServerFactory {
    
    private final McpJsonMapper jsonMapper;
    
    private final MCPToolSpecificationFactory toolSpecificationFactory;
    
    private final MCPResourceSpecificationFactory resourceSpecificationFactory;
    
    public MCPSyncServerFactory(final MCPRuntimeContext runtimeContext, final McpJsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        MCPTransportPayloadBuilder payloadBuilder = new MCPTransportPayloadBuilder();
        toolSpecificationFactory = new MCPToolSpecificationFactory(runtimeContext, payloadBuilder);
        resourceSpecificationFactory = new MCPResourceSpecificationFactory(runtimeContext, payloadBuilder);
    }
    
    /**
     * Create one sync server for single-session transports.
     *
     * @param transportProvider MCP server transport provider
     * @return sync server
     */
    public McpSyncServer create(final McpServerTransportProvider transportProvider) {
        return create(McpServer.sync(transportProvider));
    }
    
    /**
     * Create one sync server for streamable transports.
     *
     * @param transportProvider MCP streamable transport provider
     * @return sync server
     */
    public McpSyncServer create(final McpStreamableServerTransportProvider transportProvider) {
        return create(McpServer.sync(transportProvider));
    }
    
    private McpSyncServer create(final McpServer.SyncSpecification<?> specification) {
        return specification.jsonMapper(jsonMapper)
                .serverInfo(MCPTransportConstants.SERVER_NAME, resolveServerVersion())
                .instructions(MCPTransportConstants.SERVER_INSTRUCTIONS)
                .capabilities(createServerCapabilities())
                .tools(toolSpecificationFactory.createToolSpecifications())
                .resources(resourceSpecificationFactory.createResourceSpecifications())
                .resourceTemplates(resourceSpecificationFactory.createResourceTemplateSpecifications())
                .build();
    }
    
    private ServerCapabilities createServerCapabilities() {
        return McpSchema.ServerCapabilities.builder().resources(Boolean.FALSE, Boolean.FALSE).tools(Boolean.FALSE).build();
    }
    
    private String resolveServerVersion() {
        return Optional.ofNullable(MCPSyncServerFactory.class.getPackage().getImplementationVersion()).orElse("development");
    }
}
