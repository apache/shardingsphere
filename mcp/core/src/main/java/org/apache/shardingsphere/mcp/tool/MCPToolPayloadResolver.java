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
import org.apache.shardingsphere.mcp.protocol.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;

import java.util.Map;
import java.util.Optional;

/**
 * Resolve one MCP tool call into a transport-neutral payload.
 */
@RequiredArgsConstructor
public final class MCPToolPayloadResolver {
    
    private final MCPRuntimeContext runtimeContext;
    
    /**
     * Resolve one tool call.
     *
     * @param sessionId session identifier
     * @param toolName tool name
     * @param arguments normalized tool arguments
     * @return payload result
     */
    public MCPToolPayloadResult resolve(final String sessionId, final String toolName, final Map<String, Object> arguments) {
        if (!runtimeContext.getToolCatalog().contains(toolName)) {
            return error("invalid_request", "Unsupported tool.");
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
        String database = runtimeContext.getToolCatalog().getCapabilityDatabase(arguments);
        if (database.isEmpty()) {
            return MCPToolPayloadResult.success(runtimeContext.getCapabilityAssembler().assembleServiceCapability());
        }
        Optional<DatabaseCapability> capability = runtimeContext.getCapabilityAssembler().assembleDatabaseCapability(database);
        return capability.isPresent() ? MCPToolPayloadResult.success(runtimeContext.getPayloadBuilder().createDatabaseCapabilityPayload(capability.get()))
                : error("not_found", "Database capability does not exist.");
    }
    
    private MCPToolPayloadResult resolveExecuteQuery(final String sessionId, final Map<String, Object> arguments) {
        ExecutionRequest executionRequest = runtimeContext.getToolCatalog().createExecutionRequest(sessionId, arguments, runtimeContext.getDatabaseRuntime());
        if (executionRequest.getDatabase().isEmpty() || executionRequest.getSql().isEmpty()) {
            return error("invalid_request", "Database and sql are required.");
        }
        ExecuteQueryResponse response = runtimeContext.getExecuteQueryFacade().execute(executionRequest);
        Object payload = runtimeContext.getPayloadBuilder().createExecuteQueryPayload(response);
        return response.isSuccessful() ? MCPToolPayloadResult.success(payload)
                : MCPToolPayloadResult.error(runtimeContext.getPayloadBuilder().toDomainErrorCode(response.getError().get().getErrorCode()),
                        response.getError().get().getMessage(), payload);
    }
    
    private MCPToolPayloadResult resolveMetadataTool(final String toolName, final Map<String, Object> arguments) {
        ToolDispatchResult result = runtimeContext.getMetadataToolDispatcher().dispatch(
                runtimeContext.getDatabaseMetadataSnapshots(), runtimeContext.getToolCatalog().createMetadataToolRequest(toolName, arguments));
        if (!result.isSuccessful()) {
            return error(runtimeContext.getPayloadBuilder().toDomainErrorCode(result.getErrorCode().orElse(MCPErrorCode.INVALID_REQUEST)), result.getMessage());
        }
        return MCPToolPayloadResult.success(runtimeContext.getPayloadBuilder().createMetadataItemsPayload(result.getMetadataObjects(), result.getNextPageToken()));
    }
    
    private MCPToolPayloadResult error(final String errorCode, final String message) {
        return MCPToolPayloadResult.error(errorCode, message, runtimeContext.getPayloadBuilder().createErrorPayload(errorCode, message));
    }
}
