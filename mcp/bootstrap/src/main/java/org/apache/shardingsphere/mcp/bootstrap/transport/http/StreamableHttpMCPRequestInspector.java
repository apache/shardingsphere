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

package org.apache.shardingsphere.mcp.bootstrap.transport.http;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Streamable HTTP MCP request inspector.
 */
@RequiredArgsConstructor
public final class StreamableHttpMCPRequestInspector {
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private static final String ORIGIN_HEADER = "Origin";
    
    private final MCPRuntimeContext runtimeContext;
    
    private final String bindHost;
    
    /**
     * Validate request.
     * 
     * @param request request
     * @param sessionId session ID
     * @return response status
     */
    public Optional<ResponseStatus> validate(final HttpServletRequest request, final String sessionId) {
        if (sessionId.isEmpty()) {
            return Optional.of(new ResponseStatus(400, "Session ID required in mcp-session-id header"));
        }
        Optional<ResponseStatus> originFailure = validateOrigin(request);
        if (originFailure.isPresent()) {
            return originFailure;
        }
        if (!runtimeContext.getSessionManager().hasSession(sessionId)) {
            return Optional.of(new ResponseStatus(404, "Session does not exist."));
        }
        String actualProtocolVersion = normalizeProtocolVersion(request.getHeader(PROTOCOL_HEADER));
        if (!MCPTransportConstants.PROTOCOL_VERSION.equals(actualProtocolVersion)) {
            return Optional.of(new ResponseStatus(400, "Protocol version mismatch."));
        }
        return Optional.empty();
    }
    
    /**
     * Validate origin.
     * 
     * @param request request
     * @return response status
     */
    public Optional<ResponseStatus> validateOrigin(final HttpServletRequest request) {
        if (!isLoopbackHost(bindHost)) {
            return Optional.empty();
        }
        String origin = Objects.toString(request.getHeader(ORIGIN_HEADER), "").trim();
        if (origin.isEmpty()) {
            return Optional.empty();
        }
        try {
            String host = Optional.ofNullable(URI.create(origin).getHost()).orElse("");
            return isLoopbackHost(host) ? Optional.empty() : Optional.of(new ResponseStatus(403, "Origin is not allowed for the current binding."));
        } catch (final IllegalArgumentException ignored) {
            return Optional.of(new ResponseStatus(403, "Origin is not allowed for the current binding."));
        }
    }
    
    private boolean isLoopbackHost(final String rawHost) {
        String host = Objects.toString((rawHost), "").trim().toLowerCase(Locale.ENGLISH);
        return "127.0.0.1".equals(host) || "localhost".equals(host) || "::1".equals(host);
    }
    
    private String normalizeProtocolVersion(final String rawProtocolVersion) {
        String protocolVersion = Objects.toString((rawProtocolVersion), "").trim();
        return protocolVersion.isEmpty() ? MCPTransportConstants.PROTOCOL_VERSION : protocolVersion;
    }
    
    @RequiredArgsConstructor
    @Getter
    static final class ResponseStatus {
        
        private final int statusCode;
        
        private final String message;
    }
}
