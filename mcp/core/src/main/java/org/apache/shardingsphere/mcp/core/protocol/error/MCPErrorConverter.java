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
import org.apache.shardingsphere.mcp.core.tool.handler.execute.ExplainSQLSyntaxException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.util.List;
import java.util.Objects;

/**
 * MCP error converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPErrorConverter {
    
    private static final List<ErrorMapping> ERROR_MAPPINGS = List.of(
            new ErrorMapping(UnsupportedToolException.class, "Unsupported tool."),
            new ErrorMapping(UnsupportedResourceUriException.class, "Unsupported resource URI."),
            new ErrorMapping(ExplainSQLSyntaxException.class, "Generated explain_sql is not valid for the target database."),
            new ErrorMapping(MCPInvalidRequestException.class, "Invalid request."),
            new ErrorMapping(MCPNotFoundException.class, "MCP operation not found."),
            new ErrorMapping(MCPUnsupportedException.class, "Unsupported MCP operation."),
            new ErrorMapping(MCPTimeoutException.class, "MCP operation timeout."),
            new ErrorMapping(MCPTransactionStateException.class, "MCP transaction operation failed."),
            new ErrorMapping(MCPQueryFailedException.class, "MCP query failed."),
            new ErrorMapping(MCPToolCallLimitExceededException.class, "MCP tool call limit exceeded."),
            new ErrorMapping(MCPUnavailableException.class, "Service is temporarily unavailable."),
            new ErrorMapping(RuntimeDatabaseConnectionException.class, "Runtime database connection failed."),
            new ErrorMapping(SQLSyntaxErrorException.class, "Invalid request."),
            new ErrorMapping(SQLTimeoutException.class, "MCP operation timeout."),
            new ErrorMapping(SQLFeatureNotSupportedException.class, "Unsupported MCP operation."),
            new ErrorMapping(UnsupportedOperationException.class, "Unsupported MCP operation."),
            new ErrorMapping(SQLException.class, "MCP query failed."),
            new ErrorMapping(IllegalArgumentException.class, "Invalid request."),
            new ErrorMapping(IllegalStateException.class, "MCP operation failed."));
    
    /**
     * Convert throwable to MCP error.
     *
     * @param cause throwable
     * @return MCP error
     */
    public static MCPErrorResponse convert(final Throwable cause) {
        for (ErrorMapping each : ERROR_MAPPINGS) {
            if (each.matches(cause)) {
                return createError(cause, each.defaultMessage());
            }
        }
        return createError(cause, "Service is temporarily unavailable.");
    }
    
    private static MCPErrorResponse createError(final Throwable cause, final String defaultMessage) {
        String message = MCPQueryRecoveryPayloadFactory.isQueryFailure(cause) ? defaultMessage : Objects.toString(cause.getMessage(), defaultMessage).trim();
        return new MCPErrorResponse(message, MCPRecoveryPayloadFactory.create(cause));
    }
    
    private record ErrorMapping(Class<? extends Throwable> causeType, String defaultMessage) {
        
        private boolean matches(final Throwable cause) {
            return causeType.isInstance(cause);
        }
    }
}
