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

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPNotFoundException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.ShardingSphereMCPException;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.MCPTransportSecurityException;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorPayload;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP transport error factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class MCPTransportErrorFactory {
    
    /**
     * Create MCP transport error.
     *
     * @param cause error cause
     * @return MCP transport error
     */
    public static McpError createError(final Throwable cause) {
        if (cause instanceof MCPTransportSecurityException) {
            return createProtocolError(
                    new MCPErrorPayload(cause.getMessage(), createTransportSecurityRecovery((MCPTransportSecurityException) cause)), getProtocolErrorCode(cause));
        }
        if (!isApplicationError(cause)) {
            return createInternalError(cause);
        }
        return createProtocolError(MCPErrorConverter.convert(cause), getProtocolErrorCode(cause));
    }
    
    /**
     * Create MCP resource error.
     *
     * @param cause error cause
     * @return MCP resource error
     */
    public static McpError createResourceError(final Throwable cause) {
        if (!isApplicationError(cause)) {
            return createInternalError(cause);
        }
        return createProtocolError(MCPErrorConverter.convert(cause), getResourceProtocolErrorCode(cause));
    }
    
    private static McpError createInternalError(final Throwable cause) {
        MCPErrorPayload errorPayload = new MCPErrorPayload("Service is temporarily unavailable.");
        Map<String, Object> payload = errorPayload.toPayload();
        log.error("Unexpected MCP request failure, request ID: {}.", payload.get("request_id"), cause);
        return createProtocolError(errorPayload, payload, McpSchema.ErrorCodes.INTERNAL_ERROR);
    }
    
    private static Map<String, Object> createTransportSecurityRecovery(final MCPTransportSecurityException cause) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("response_mode", "recovery");
        result.put("recovery_category", "transport_security");
        result.put("recoverable", false);
        result.put("category", cause.getCategory());
        result.put("model_action", createTransportSecurityModelAction(cause.getCategory()));
        result.put("next_actions", List.of(Map.of("order", 1, "type", "terminal", "title", "Stop", "reason", "Transport security policy rejected the request.")));
        return result;
    }
    
    private static String createTransportSecurityModelAction(final String category) {
        if (MCPTransportSecurityException.CATEGORY_ORIGIN_NOT_ALLOWED.equals(category)) {
            return "Use an allowed loopback Origin or omit Origin for trusted local MCP HTTP requests.";
        }
        return "Start a new MCP session or align trusted session attribution headers before retrying.";
    }
    
    private static int getProtocolErrorCode(final Throwable cause) {
        if (cause instanceof MCPTransportSecurityException || cause instanceof MCPInvalidRequestException || cause instanceof MCPUnsupportedException) {
            return McpSchema.ErrorCodes.INVALID_PARAMS;
        }
        return McpSchema.ErrorCodes.INTERNAL_ERROR;
    }
    
    private static int getResourceProtocolErrorCode(final Throwable cause) {
        if (cause instanceof MCPNotFoundException) {
            return McpSchema.ErrorCodes.RESOURCE_NOT_FOUND;
        }
        if (cause instanceof MCPInvalidRequestException || cause instanceof MCPUnsupportedException) {
            return McpSchema.ErrorCodes.INVALID_PARAMS;
        }
        return McpSchema.ErrorCodes.INTERNAL_ERROR;
    }
    
    private static boolean isApplicationError(final Throwable cause) {
        return cause instanceof ShardingSphereMCPException || cause instanceof RuntimeDatabaseConnectionException || cause instanceof SQLException;
    }
    
    private static McpError createProtocolError(final MCPErrorPayload errorPayload, final int code) {
        return createProtocolError(errorPayload, errorPayload.toPayload(), code);
    }
    
    private static McpError createProtocolError(final MCPErrorPayload errorPayload, final Map<String, Object> payload, final int code) {
        return McpError.builder(code).message(errorPayload.getMessage()).data(payload).build();
    }
}
