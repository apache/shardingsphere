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
import org.apache.shardingsphere.mcp.core.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;

/**
 * MCP transport error factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPTransportErrorFactory {
    
    /**
     * Create invalid parameters error.
     *
     * @param cause invalid request exception
     * @return MCP transport error
     */
    public static McpError createInvalidParamsError(final MCPInvalidRequestException cause) {
        MCPErrorResponse errorResponse = MCPErrorConverter.convert(cause);
        return McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS).message(errorResponse.getMessage()).data(errorResponse.toPayload()).build();
    }
}
