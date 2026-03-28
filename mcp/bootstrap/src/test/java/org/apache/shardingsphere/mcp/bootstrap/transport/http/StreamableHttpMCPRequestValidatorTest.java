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
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreamableHttpMCPRequestValidatorTest {
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private static final String ORIGIN_HEADER = "Origin";
    
    @Test
    void assertValidateInitialization() {
        StreamableHttpMCPRequestValidator validator = new StreamableHttpMCPRequestValidator(mock(MCPRuntimeContext.class), "127.0.0.1");
        HttpServletRequest request = mock(HttpServletRequest.class);
        Optional<StreamableHttpMCPRequestValidator.ResponseStatus> actual = validator.validateInitialization(request);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertValidateInitializationWithInvalidOrigin() {
        StreamableHttpMCPRequestValidator validator = new StreamableHttpMCPRequestValidator(mock(MCPRuntimeContext.class), "127.0.0.1");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(ORIGIN_HEADER)).thenReturn("https://example.com");
        StreamableHttpMCPRequestValidator.ResponseStatus actual = validator.validateInitialization(request).orElseThrow();
        assertThat(actual.getStatusCode(), is(403));
        assertThat(actual.getMessage(), is("Origin is not allowed for the current binding."));
    }
    
    @Test
    void assertValidateSessionRequestWithMissingSessionId() {
        StreamableHttpMCPRequestValidator validator = new StreamableHttpMCPRequestValidator(mock(MCPRuntimeContext.class), "127.0.0.1");
        HttpServletRequest request = mock(HttpServletRequest.class);
        StreamableHttpMCPRequestValidator.ResponseStatus actual = validator.validateSessionRequest(request, "").orElseThrow();
        assertThat(actual.getStatusCode(), is(400));
        assertThat(actual.getMessage(), is("Session ID required in mcp-session-id header"));
    }
    
    @Test
    void assertValidateSessionRequestWithUnknownSession() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(false);
        StreamableHttpMCPRequestValidator validator = new StreamableHttpMCPRequestValidator(createRuntimeContext(sessionManager), "127.0.0.1");
        HttpServletRequest request = mock(HttpServletRequest.class);
        StreamableHttpMCPRequestValidator.ResponseStatus actual = validator.validateSessionRequest(request, "session-id").orElseThrow();
        assertThat(actual.getStatusCode(), is(404));
        assertThat(actual.getMessage(), is("Session does not exist."));
    }
    
    @Test
    void assertValidateSessionRequestWithProtocolMismatch() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(true);
        StreamableHttpMCPRequestValidator validator = new StreamableHttpMCPRequestValidator(createRuntimeContext(sessionManager), "127.0.0.1");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(PROTOCOL_HEADER)).thenReturn("2024-11-05");
        StreamableHttpMCPRequestValidator.ResponseStatus actual = validator.validateSessionRequest(request, "session-id").orElseThrow();
        assertThat(actual.getStatusCode(), is(400));
        assertThat(actual.getMessage(), is("Protocol version mismatch."));
    }
    
    @Test
    void assertValidateSessionRequest() {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        when(sessionManager.hasSession("session-id")).thenReturn(true);
        StreamableHttpMCPRequestValidator validator = new StreamableHttpMCPRequestValidator(createRuntimeContext(sessionManager), "127.0.0.1");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(PROTOCOL_HEADER)).thenReturn(MCPTransportConstants.PROTOCOL_VERSION);
        Optional<StreamableHttpMCPRequestValidator.ResponseStatus> actual = validator.validateSessionRequest(request, "session-id");
        assertTrue(actual.isEmpty());
    }
    
    private MCPRuntimeContext createRuntimeContext(final MCPSessionManager sessionManager) {
        MCPRuntimeContext result = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
        when(result.getSessionManager()).thenReturn(sessionManager);
        return result;
    }
}
