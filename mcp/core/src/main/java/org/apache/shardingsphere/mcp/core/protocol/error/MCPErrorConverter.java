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

package org.apache.shardingsphere.mcp.core.protocol.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPNotFoundException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPToolCallLimitExceededException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.util.Objects;

/**
 * MCP error converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPErrorConverter {
    
    /**
     * Convert throwable to MCP error.
     *
     * @param cause throwable
     * @return MCP error
     */
    public static MCPErrorResponse convert(final Throwable cause) {
        if (cause instanceof UnsupportedToolException) {
            return createError(cause, "Unsupported tool.");
        }
        if (cause instanceof UnsupportedResourceUriException) {
            return createError(cause, "Unsupported resource URI.");
        }
        if (cause instanceof MCPInvalidRequestException) {
            return createError(cause, "Invalid request.");
        }
        if (cause instanceof MCPNotFoundException) {
            return createError(cause, "MCP operation not found.");
        }
        if (cause instanceof MCPUnsupportedException) {
            return createError(cause, "Unsupported MCP operation.");
        }
        if (cause instanceof MCPTimeoutException) {
            return createError(cause, "MCP operation timeout.");
        }
        if (cause instanceof MCPTransactionStateException) {
            return createError(cause, "MCP transaction operation failed.");
        }
        if (cause instanceof MCPQueryFailedException) {
            return createError(cause, "MCP query failed.");
        }
        if (cause instanceof MCPToolCallLimitExceededException) {
            return createError(cause, "MCP tool call limit exceeded.");
        }
        if (cause instanceof MCPUnavailableException) {
            return createError(cause, "Service is temporarily unavailable.");
        }
        if (cause instanceof RuntimeDatabaseConnectionException) {
            return createError(cause, "Runtime database connection failed.");
        }
        if (cause instanceof SQLSyntaxErrorException) {
            return createError(cause, "Invalid request.");
        }
        if (cause instanceof SQLTimeoutException) {
            return createError(cause, "MCP operation timeout.");
        }
        if (cause instanceof SQLFeatureNotSupportedException) {
            return createError(cause, "Unsupported MCP operation.");
        }
        if (cause instanceof UnsupportedOperationException) {
            return createError(cause, "Unsupported MCP operation.");
        }
        if (cause instanceof SQLException) {
            return createError(cause, "MCP query failed.");
        }
        if (cause instanceof IllegalArgumentException) {
            return createError(cause, "Invalid request.");
        }
        if (cause instanceof IllegalStateException) {
            return createError(cause, "MCP transaction operation failed.");
        }
        return createError(cause, "Service is temporarily unavailable.");
    }
    
    private static MCPErrorResponse createError(final Throwable cause, final String defaultMessage) {
        String message = Objects.toString(cause.getMessage(), defaultMessage).trim();
        return new MCPErrorResponse(message, MCPRecoveryPayloadFactory.create(cause));
    }
}
