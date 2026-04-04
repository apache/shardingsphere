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

package org.apache.shardingsphere.mcp.tool;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.provider.MCPServiceCapabilityProvider;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.resource.response.MCPDatabaseCapabilityResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPMetadataResponse;
import org.apache.shardingsphere.mcp.resource.response.MCPServiceCapabilityResponse;

import java.util.Map;
import java.util.Optional;

/**
 * Resolve one MCP tool call into a transport-neutral payload.
 */
@RequiredArgsConstructor
public final class MCPToolPayloadResolver {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPToolCatalog toolCatalog = new MCPToolCatalog();
    
    /**
     * Resolve one tool call.
     *
     * @param sessionId session identifier
     * @param toolName tool name
     * @param arguments normalized tool arguments
     * @return payload
     * @throws UnsupportedToolException unsupported tool exception
     */
    public Map<String, Object> resolve(final String sessionId, final String toolName, final Map<String, Object> arguments) {
        if (toolCatalog.findToolDescriptor(toolName).isEmpty()) {
            throw new UnsupportedToolException();
        }
        switch (toolName) {
            case "get_capabilities":
                return resolveGetCapabilities(arguments);
            case "execute_query":
                return resolveExecuteQuery(sessionId, arguments);
            default:
                return resolveMetadataTool(toolName, arguments);
        }
    }
    
    private Map<String, Object> resolveGetCapabilities(final Map<String, Object> arguments) {
        String database = toolCatalog.getCapabilityDatabase(arguments);
        if (database.isEmpty()) {
            return new MCPServiceCapabilityResponse(MCPServiceCapabilityProvider.provide()).toPayload();
        }
        Optional<DatabaseCapability> capability = runtimeContext.getDatabaseCapabilityProvider().provide(database);
        return capability.map(optional -> new MCPDatabaseCapabilityResponse(optional).toPayload()).orElseThrow(DatabaseCapabilityNotFoundException::new);
    }
    
    private Map<String, Object> resolveExecuteQuery(final String sessionId, final Map<String, Object> arguments) {
        ExecutionRequest executionRequest = toolCatalog.createExecutionRequest(sessionId, arguments);
        if (executionRequest.getDatabase().isEmpty() || executionRequest.getSql().isEmpty()) {
            throw new MCPInvalidRequestException("Database and sql are required.");
        }
        return runtimeContext.getSqlExecutionFacade().execute(executionRequest).toPayload();
    }
    
    private Map<String, Object> resolveMetadataTool(final String toolName, final Map<String, Object> arguments) {
        ToolDispatchResult result = new MetadataToolDispatcher().dispatch(runtimeContext.getDatabaseMetadataSnapshots(), toolCatalog.createMetadataToolRequest(toolName, arguments));
        return new MCPMetadataResponse(result.getMetadataObjects(), result.getNextPageToken()).toPayload();
    }
}
