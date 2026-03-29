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

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.tool.ToolDispatchResult;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
final class MCPToolCallHandler {
    
    private final MCPRuntimeContext runtimeContext;
    
    McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Collections.emptyMap());
        if (!runtimeContext.getToolCatalog().contains(request.name())) {
            return errorToolResult("invalid_request", "Unsupported tool.");
        }
        switch (request.name()) {
            case "get_capabilities":
                return handleGetCapabilities(arguments);
            case "execute_query":
                return handleExecuteQuery(exchange.sessionId(), arguments);
            default:
                return handleMetadataToolCall(request.name(), arguments);
        }
    }
    
    private McpSchema.CallToolResult handleGetCapabilities(final Map<String, Object> arguments) {
        String database = runtimeContext.getToolCatalog().getCapabilityDatabase(arguments);
        if (database.isEmpty()) {
            return successToolResult(runtimeContext.getCapabilityAssembler().assembleServiceCapability());
        }
        Optional<DatabaseCapability> capability = runtimeContext.getCapabilityAssembler().assembleDatabaseCapability(database);
        return capability.isPresent() ? successToolResult(runtimeContext.getPayloadBuilder().createDatabaseCapabilityPayload(capability.get()))
                : errorToolResult("not_found", "Database capability does not exist.");
    }
    
    private McpSchema.CallToolResult handleExecuteQuery(final String sessionId, final Map<String, Object> arguments) {
        ExecutionRequest executionRequest = runtimeContext.getToolCatalog().createExecutionRequest(sessionId, arguments, runtimeContext.getDatabaseRuntime());
        if (executionRequest.getDatabase().isEmpty() || executionRequest.getSql().isEmpty()) {
            return errorToolResult("invalid_request", "Database and sql are required.");
        }
        ExecuteQueryResponse response = runtimeContext.getExecuteQueryFacade().execute(executionRequest);
        Object payload = runtimeContext.getPayloadBuilder().createExecuteQueryPayload(response);
        return response.isSuccessful() ? successToolResult(payload)
                : errorToolResult(runtimeContext.getPayloadBuilder().toDomainErrorCode(response.getError().get().getErrorCode()), response.getError().get().getMessage(), payload);
    }
    
    private McpSchema.CallToolResult handleMetadataToolCall(final String toolName, final Map<String, Object> arguments) {
        ToolDispatchResult result = runtimeContext.getMetadataToolDispatcher().dispatch(runtimeContext.getMetadataCatalog(),
                runtimeContext.getToolCatalog().createMetadataToolRequest(toolName, arguments));
        if (!result.isSuccessful()) {
            return errorToolResult(runtimeContext.getPayloadBuilder().toDomainErrorCode(result.getErrorCode().orElse(ErrorCode.INVALID_REQUEST)), result.getMessage());
        }
        return successToolResult(runtimeContext.getPayloadBuilder().createMetadataItemsPayload(result.getMetadataObjects(), result.getNextPageToken()));
    }
    
    private McpSchema.CallToolResult successToolResult(final Object payload) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(payload)
                .addTextContent(JsonUtils.toJsonString(payload))
                .isError(Boolean.FALSE)
                .build();
    }
    
    private McpSchema.CallToolResult errorToolResult(final String errorCode, final String message) {
        return errorToolResult(errorCode, message, runtimeContext.getPayloadBuilder().createErrorPayload(errorCode, message));
    }
    
    private McpSchema.CallToolResult errorToolResult(final String errorCode, final String message, final Object payload) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(payload)
                .addTextContent(JsonUtils.toJsonString(Map.of("error_code", errorCode, "message", message)))
                .isError(Boolean.TRUE)
                .build();
    }
}
