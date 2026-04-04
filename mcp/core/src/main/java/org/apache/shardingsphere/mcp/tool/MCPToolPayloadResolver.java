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
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.protocol.MCPError;
import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.response.ExecuteQueryResponse;
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
     * @return payload result
     */
    public MCPToolPayloadResult resolve(final String sessionId, final String toolName, final Map<String, Object> arguments) {
        if (toolCatalog.findToolDescriptor(toolName).isEmpty()) {
            return MCPToolPayloadResult.error(new MCPError(MCPErrorCode.INVALID_REQUEST, "Unsupported tool."));
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
    
    private MCPToolPayloadResult resolveGetCapabilities(final Map<String, Object> arguments) {
        String database = toolCatalog.getCapabilityDatabase(arguments);
        if (database.isEmpty()) {
            return MCPToolPayloadResult.success(new MCPServiceCapabilityResponse(runtimeContext.getCapabilityBuilder().buildServiceCapability()).toPayload());
        }
        Optional<DatabaseCapability> capability = runtimeContext.getCapabilityBuilder().buildDatabaseCapability(database);
        return capability.map(optional -> MCPToolPayloadResult.success(new MCPDatabaseCapabilityResponse(optional).toPayload()))
                .orElseGet(() -> MCPToolPayloadResult.error(new MCPError(MCPErrorCode.NOT_FOUND, "Database capability does not exist.")));
    }
    
    private MCPToolPayloadResult resolveExecuteQuery(final String sessionId, final Map<String, Object> arguments) {
        ExecutionRequest executionRequest = toolCatalog.createExecutionRequest(sessionId, arguments);
        if (executionRequest.getDatabase().isEmpty() || executionRequest.getSql().isEmpty()) {
            return MCPToolPayloadResult.error(new MCPError(MCPErrorCode.INVALID_REQUEST, "Database and sql are required."));
        }
        ExecuteQueryResponse response = runtimeContext.getSqlExecutionFacade().execute(executionRequest);
        Map<String, Object> payload = response.toPayload();
        return response.isSuccessful()
                ? MCPToolPayloadResult.success(payload)
                : MCPToolPayloadResult.error(payload, response.getError().get());
    }
    
    private MCPToolPayloadResult resolveMetadataTool(final String toolName, final Map<String, Object> arguments) {
        ToolDispatchResult result = new MetadataToolDispatcher().dispatch(runtimeContext.getDatabaseMetadataSnapshots(), toolCatalog.createMetadataToolRequest(toolName, arguments));
        return result.isSuccessful()
                ? MCPToolPayloadResult.success(new MCPMetadataResponse(result.getMetadataObjects(), result.getNextPageToken()).toPayload())
                : MCPToolPayloadResult.error(result.getError());
    }
    
}
