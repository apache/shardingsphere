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
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.MCPTransportSecurityException;
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP transport error factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPTransportErrorFactory {
    
    /**
     * Create MCP transport error.
     *
     * @param cause error cause
     * @return MCP transport error
     */
    public static McpError createError(final Throwable cause) {
        if (cause instanceof MCPTransportSecurityException) {
            MCPErrorResponse errorResponse = new MCPErrorResponse(cause.getMessage(), createTransportSecurityRecovery((MCPTransportSecurityException) cause));
            return McpError.builder(getProtocolErrorCode(cause)).message(errorResponse.getMessage()).data(errorResponse.toPayload()).build();
        }
        MCPErrorResponse errorResponse = MCPErrorConverter.convert(cause);
        return McpError.builder(getProtocolErrorCode(cause)).message(errorResponse.getMessage()).data(errorResponse.toPayload()).build();
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
        if (cause instanceof UnsupportedResourceUriException) {
            return McpSchema.ErrorCodes.RESOURCE_NOT_FOUND;
        }
        if (cause instanceof MCPTransportSecurityException || cause instanceof MCPInvalidRequestException || cause instanceof MCPUnsupportedException || cause instanceof IllegalArgumentException
                || cause instanceof UnsupportedOperationException) {
            return McpSchema.ErrorCodes.INVALID_PARAMS;
        }
        return McpSchema.ErrorCodes.INTERNAL_ERROR;
    }
}
