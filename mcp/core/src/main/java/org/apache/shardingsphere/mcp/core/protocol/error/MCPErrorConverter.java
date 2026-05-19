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
import org.apache.shardingsphere.mcp.api.protocol.error.MCPErrorCode;
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
            return createError(MCPErrorCode.NOT_FOUND, cause, "Unsupported tool.");
        }
        if (cause instanceof UnsupportedResourceUriException) {
            return createError(MCPErrorCode.NOT_FOUND, cause, "Unsupported resource URI.");
        }
        if (cause instanceof MCPInvalidRequestException) {
            return createError(MCPErrorCode.INVALID_REQUEST, cause, "Invalid request.");
        }
        if (cause instanceof MCPNotFoundException) {
            return createError(MCPErrorCode.NOT_FOUND, cause, "MCP operation not found.");
        }
        if (cause instanceof MCPUnsupportedException) {
            return createError(MCPErrorCode.UNSUPPORTED, cause, "Unsupported MCP operation.");
        }
        if (cause instanceof MCPTimeoutException) {
            return createError(MCPErrorCode.TIMEOUT, cause, "MCP operation timeout.");
        }
        if (cause instanceof MCPTransactionStateException) {
            return createError(MCPErrorCode.TRANSACTION_STATE_ERROR, cause, "MCP transaction operation failed.");
        }
        if (cause instanceof MCPQueryFailedException) {
            return createError(MCPErrorCode.QUERY_FAILED, cause, "MCP query failed.");
        }
        if (cause instanceof MCPToolCallLimitExceededException) {
            return createError(MCPErrorCode.RATE_LIMITED, cause, "MCP tool call limit exceeded.");
        }
        if (cause instanceof MCPUnavailableException) {
            return createError(MCPErrorCode.UNAVAILABLE, cause, "Service is temporarily unavailable.");
        }
        if (cause instanceof RuntimeDatabaseConnectionException) {
            return createError(MCPErrorCode.UNAVAILABLE, cause, "Runtime database connection failed.");
        }
        if (cause instanceof SQLSyntaxErrorException) {
            return createError(MCPErrorCode.INVALID_REQUEST, cause, "Invalid request.");
        }
        if (cause instanceof SQLTimeoutException) {
            return createError(MCPErrorCode.TIMEOUT, cause, "MCP operation timeout.");
        }
        if (cause instanceof SQLFeatureNotSupportedException) {
            return createError(MCPErrorCode.UNSUPPORTED, cause, "Unsupported MCP operation.");
        }
        if (cause instanceof UnsupportedOperationException) {
            return createError(MCPErrorCode.UNSUPPORTED, cause, "Unsupported MCP operation.");
        }
        if (cause instanceof SQLException) {
            return createError(MCPErrorCode.QUERY_FAILED, cause, "MCP query failed.");
        }
        if (cause instanceof IllegalArgumentException) {
            return createError(MCPErrorCode.INVALID_REQUEST, cause, "Invalid request.");
        }
        if (cause instanceof IllegalStateException) {
            return createError(MCPErrorCode.TRANSACTION_STATE_ERROR, cause, "MCP transaction operation failed.");
        }
        return createError(MCPErrorCode.UNAVAILABLE, cause, "Service is temporarily unavailable.");
    }
    
    private static MCPErrorResponse createError(final MCPErrorCode errorCode, final Throwable cause, final String defaultMessage) {
        String message = Objects.toString(cause.getMessage(), defaultMessage).trim();
        return new MCPErrorResponse(errorCode, message, MCPRecoveryPayloadFactory.create(cause));
    }
}
