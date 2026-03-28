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
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Streamable HTTP MCP request inspector.
 */
@RequiredArgsConstructor
final class StreamableHttpMCPRequestInspector {
    
    private static final String SESSION_HEADER = "MCP-Session-Id";
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private static final String ORIGIN_HEADER = "Origin";
    
    private final MCPRuntimeContext runtimeContext;
    
    private final String bindHost;
    
    Map<String, String> extractHeaders(final HttpServletRequest request) {
        Map<String, String> result = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String each = headerNames.nextElement();
            String value = request.getHeader(each);
            if (null != value && !value.trim().isEmpty()) {
                result.put(each, value.trim());
            }
        }
        return result;
    }
    
    String getHeader(final Map<String, String> headers, final String headerName) {
        String result = headers.get(headerName);
        if (null != result) {
            return result.trim();
        }
        for (Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(headerName)) {
                return entry.getValue().trim();
            }
        }
        return "";
    }
    
    Optional<ResponseStatus> validateInitializeRequest(final Map<String, String> headers) {
        return validateOrigin(headers);
    }
    
    Optional<ResponseStatus> validateFollowUpRequest(final Map<String, String> headers) {
        String sessionId = getHeader(headers, SESSION_HEADER);
        if (sessionId.isEmpty()) {
            return Optional.of(new ResponseStatus(400, "Session ID required in mcp-session-id header"));
        }
        Optional<ResponseStatus> originFailure = validateOrigin(headers);
        if (originFailure.isPresent()) {
            return originFailure;
        }
        if (!runtimeContext.getSessionManager().hasSession(sessionId)) {
            return Optional.of(new ResponseStatus(404, "Session does not exist."));
        }
        String actualProtocolVersion = normalizeProtocolVersion(getHeader(headers, PROTOCOL_HEADER));
        if (!MCPTransportConstants.PROTOCOL_VERSION.equals(actualProtocolVersion)) {
            return Optional.of(new ResponseStatus(400, "Protocol version mismatch."));
        }
        return Optional.empty();
    }
    
    private Optional<ResponseStatus> validateOrigin(final Map<String, String> headers) {
        if (!isLoopbackHost(bindHost)) {
            return Optional.empty();
        }
        String origin = getHeader(headers, ORIGIN_HEADER);
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
    
    private boolean isLoopbackHost(final String host) {
        String actualHost = null == host ? "" : host.trim().toLowerCase(Locale.ENGLISH);
        return "127.0.0.1".equals(actualHost) || "localhost".equals(actualHost) || "::1".equals(actualHost);
    }
    
    private String normalizeProtocolVersion(final String rawProtocolVersion) {
        String actualProtocolVersion = null == rawProtocolVersion ? "" : rawProtocolVersion.trim();
        return actualProtocolVersion.isEmpty() ? MCPTransportConstants.PROTOCOL_VERSION : actualProtocolVersion;
    }
    
    @RequiredArgsConstructor
    @Getter
    static final class ResponseStatus {
        
        private final int statusCode;
        
        private final String message;
    }
}
