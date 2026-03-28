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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
final class MCPToolCallHandler {
    
    private final MCPRuntimeContext runtimeContext;
    
    private final MCPTransportPayloadBuilder payloadBuilder;
    
    McpSchema.CallToolResult handle(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Collections.emptyMap());
        Optional<MCPToolDefinition> toolDefinition = MCPToolDefinition.findByName(request.name());
        if (toolDefinition.isEmpty()) {
            return errorToolResult("invalid_request", "Unsupported tool.");
        }
        switch (toolDefinition.get()) {
            case GET_CAPABILITIES:
                return handleGetCapabilities(arguments);
            case EXECUTE_QUERY:
                return handleExecuteQuery(exchange.sessionId(), arguments);
            default:
                return handleMetadataToolCall(toolDefinition.get(), arguments);
        }
    }
    
    private McpSchema.CallToolResult handleMetadataToolCall(final MCPToolDefinition toolDefinition, final Map<String, Object> arguments) {
        ToolDispatchResult result = runtimeContext.getMetadataToolDispatcher().dispatch(runtimeContext.getMetadataCatalog(), toolDefinition.createMetadataToolRequest(arguments));
        if (!result.isSuccessful()) {
            return errorToolResult(payloadBuilder.toDomainErrorCode(result.getErrorCode().orElse(ErrorCode.INVALID_REQUEST)), result.getMessage());
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", result.getMetadataObjects());
        if (!result.getNextPageToken().isEmpty()) {
            payload.put("next_page_token", result.getNextPageToken());
        }
        return successToolResult(payload);
    }
    
    private McpSchema.CallToolResult handleGetCapabilities(final Map<String, Object> arguments) {
        String database = getStringArgument(arguments, "database");
        if (database.isEmpty()) {
            return successToolResult(runtimeContext.getCapabilityAssembler().assembleServiceCapability());
        }
        Optional<DatabaseCapability> capability = runtimeContext.getCapabilityAssembler().assembleDatabaseCapability(database);
        return capability.isPresent() ? successToolResult(payloadBuilder.createDatabaseCapabilityPayload(capability.get())) : errorToolResult("not_found", "Database capability does not exist.");
    }
    
    private McpSchema.CallToolResult handleExecuteQuery(final String sessionId, final Map<String, Object> arguments) {
        String database = getStringArgument(arguments, "database");
        String sql = getStringArgument(arguments, "sql");
        if (database.isEmpty() || sql.isEmpty()) {
            return errorToolResult("invalid_request", "Database and sql are required.");
        }
        ExecutionRequest executionRequest = new ExecutionRequest(sessionId, database, getStringArgument(arguments, "schema"),
                sql, getIntegerArgument(arguments, "max_rows", 0), getIntegerArgument(arguments, "timeout_ms", 0), runtimeContext.getDatabaseRuntime());
        ExecuteQueryResponse response = runtimeContext.getExecuteQueryFacade().execute(executionRequest);
        return response.isSuccessful() ? successToolResult(toExecuteQueryPayload(response))
                : errorToolResult(payloadBuilder.toDomainErrorCode(response.getError().get().getErrorCode()), response.getError().get().getMessage(), toExecuteQueryPayload(response));
    }
    
    private Map<String, Object> toExecuteQueryPayload(final ExecuteQueryResponse response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("result_kind", response.getResultKind().name().toLowerCase(Locale.ENGLISH));
        payload.put("statement_type", response.getStatementType());
        payload.put("status", response.getStatus());
        if (!response.getColumns().isEmpty()) {
            payload.put("columns", response.getColumns());
        }
        if (!response.getRows().isEmpty()) {
            payload.put("rows", response.getRows());
        }
        if (0 != response.getAffectedRows()) {
            payload.put("affected_rows", response.getAffectedRows());
        }
        if (!response.getMessage().isEmpty()) {
            payload.put("message", response.getMessage());
        }
        payload.put("truncated", response.isTruncated());
        if (response.getError().isPresent()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error_code", payloadBuilder.toDomainErrorCode(response.getError().get().getErrorCode()));
            error.put("message", response.getError().get().getMessage());
            payload.put("error", error);
        }
        return payload;
    }
    
    private McpSchema.CallToolResult successToolResult(final Object payload) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(payload)
                .addTextContent(JsonUtils.toJsonString(payload))
                .isError(Boolean.FALSE)
                .build();
    }
    
    private McpSchema.CallToolResult errorToolResult(final String errorCode, final String message) {
        return errorToolResult(errorCode, message, payloadBuilder.createErrorPayload(errorCode, message));
    }
    
    private McpSchema.CallToolResult errorToolResult(final String errorCode, final String message, final Object payload) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(payload)
                .addTextContent(JsonUtils.toJsonString(Map.of("error_code", errorCode, "message", message)))
                .isError(Boolean.TRUE)
                .build();
    }
    
    private String getStringArgument(final Map<String, Object> arguments, final String name) {
        Object result = arguments.get(name);
        return null == result ? "" : result.toString().trim();
    }
    
    private int getIntegerArgument(final Map<String, Object> arguments, final String name, final int defaultValue) {
        Object result = arguments.get(name);
        if (null == result) {
            return defaultValue;
        }
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        String actualValue = result.toString().trim();
        if (actualValue.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(actualValue);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
