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

package org.apache.shardingsphere.mcp.protocol.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.protocol.error.MCPError.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.exception.MCPProtocolException;

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
    public static MCPError convert(final Throwable cause) {
        if (cause instanceof MCPProtocolException) {
            MCPProtocolException protocolException = (MCPProtocolException) cause;
            return createError(protocolException.getErrorCode(), protocolException);
        }
        if (cause instanceof SQLSyntaxErrorException) {
            return createError(MCPErrorCode.INVALID_REQUEST, cause);
        }
        if (cause instanceof SQLTimeoutException) {
            return createError(MCPErrorCode.TIMEOUT, cause);
        }
        if (cause instanceof SQLFeatureNotSupportedException) {
            return createError(MCPErrorCode.UNSUPPORTED, cause);
        }
        if (cause instanceof UnsupportedOperationException) {
            return createError(MCPErrorCode.UNSUPPORTED, cause);
        }
        if (cause instanceof SQLException) {
            return createError(MCPErrorCode.QUERY_FAILED, cause);
        }
        if (cause instanceof IllegalArgumentException) {
            return createError(MCPErrorCode.INVALID_REQUEST, cause);
        }
        if (cause instanceof IllegalStateException) {
            return createError(MCPErrorCode.TRANSACTION_STATE_ERROR, cause);
        }
        return createError(MCPErrorCode.UNAVAILABLE, cause);
    }
    
    private static MCPError createError(final MCPErrorCode errorCode, final Throwable cause) {
        return new MCPError(errorCode, Objects.toString(cause.getMessage(), errorCode.getDefaultMessage()).trim());
    }
}
