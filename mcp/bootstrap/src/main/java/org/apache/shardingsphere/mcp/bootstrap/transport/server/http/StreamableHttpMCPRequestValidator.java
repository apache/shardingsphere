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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.util.Objects;
import java.util.Optional;

/**
 * Streamable HTTP MCP request validator.
 */
@RequiredArgsConstructor
public final class StreamableHttpMCPRequestValidator {
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private final MCPRuntimeContext runtimeContext;
    
    /**
     * Validate session header presence.
     *
     * @param sessionId session ID
     * @return response status
     */
    public Optional<ResponseStatus> validateSessionId(final String sessionId) {
        return sessionId.isEmpty() ? Optional.of(new ResponseStatus(400, "Session ID required in mcp-session-id header")) : Optional.empty();
    }
    
    /**
     * Validate session request after the session header has already been checked.
     *
     * @param request request
     * @param sessionId session ID
     * @return response status
     */
    public Optional<ResponseStatus> validateSessionRequest(final HttpServletRequest request, final String sessionId) {
        if (!runtimeContext.getSessionManager().hasSession(sessionId)) {
            return Optional.of(new ResponseStatus(404, "Session does not exist."));
        }
        String actualProtocolVersion = normalizeProtocolVersion(request.getHeader(PROTOCOL_HEADER));
        if (!MCPTransportConstants.PROTOCOL_VERSION.equals(actualProtocolVersion)) {
            return Optional.of(new ResponseStatus(400, "Protocol version mismatch."));
        }
        return Optional.empty();
    }
    
    private String normalizeProtocolVersion(final String rawProtocolVersion) {
        String protocolVersion = Objects.toString(rawProtocolVersion, "").trim();
        return protocolVersion.isEmpty() ? MCPTransportConstants.PROTOCOL_VERSION : protocolVersion;
    }
    
    @RequiredArgsConstructor
    @Getter
    static final class ResponseStatus {
        
        private final int statusCode;
        
        private final String message;
    }
}
