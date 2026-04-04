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

package org.apache.shardingsphere.mcp.protocol.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.protocol.MCPError;
import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.exception.MCPProtocolException;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.util.Objects;

/**
 * Converter for MCP protocol errors.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPProtocolErrorConverter {
    
    /**
     * Convert throwable to MCP error.
     *
     * @param cause throwable
     * @return MCP error
     */
    public static MCPError toError(final Throwable cause) {
        if (cause instanceof MCPProtocolException) {
            MCPProtocolException protocolException = (MCPProtocolException) cause;
            return new MCPError(protocolException.getErrorCode(), toMessage(protocolException.getMessage(), protocolException.getErrorCode()));
        }
        if (cause instanceof SQLTimeoutException) {
            return new MCPError(MCPErrorCode.TIMEOUT, toMessage(cause.getMessage(), MCPErrorCode.TIMEOUT));
        }
        if (cause instanceof SQLFeatureNotSupportedException) {
            return new MCPError(MCPErrorCode.UNSUPPORTED, toMessage(cause.getMessage(), MCPErrorCode.UNSUPPORTED));
        }
        if (cause instanceof SQLSyntaxErrorException) {
            return new MCPError(MCPErrorCode.INVALID_REQUEST, toMessage(cause.getMessage(), MCPErrorCode.INVALID_REQUEST));
        }
        if (cause instanceof SQLException) {
            return new MCPError(MCPErrorCode.QUERY_FAILED, toMessage(cause.getMessage(), MCPErrorCode.QUERY_FAILED));
        }
        if (cause instanceof IllegalArgumentException) {
            return new MCPError(MCPErrorCode.INVALID_REQUEST, toMessage(cause.getMessage(), MCPErrorCode.INVALID_REQUEST));
        }
        if (cause instanceof UnsupportedOperationException) {
            return new MCPError(MCPErrorCode.UNSUPPORTED, toMessage(cause.getMessage(), MCPErrorCode.UNSUPPORTED));
        }
        if (cause instanceof IllegalStateException) {
            return new MCPError(MCPErrorCode.TRANSACTION_STATE_ERROR, toMessage(cause.getMessage(), MCPErrorCode.TRANSACTION_STATE_ERROR));
        }
        return new MCPError(MCPErrorCode.UNAVAILABLE, toMessage(null == cause ? "" : cause.getMessage(), MCPErrorCode.UNAVAILABLE));
    }
    
    private static String toMessage(final String message, final MCPErrorCode errorCode) {
        String actualMessage = Objects.toString(message, "").trim();
        if (actualMessage.isEmpty()) {
            return MCPErrorCode.UNAVAILABLE == errorCode ? "Service is temporarily unavailable." : "MCP operation failed.";
        }
        return actualMessage;
    }
}
