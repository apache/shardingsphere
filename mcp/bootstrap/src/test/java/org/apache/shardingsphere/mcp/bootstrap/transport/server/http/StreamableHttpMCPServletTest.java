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

import io.modelcontextprotocol.spec.HttpHeaders;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamableHttpMCPServletTest {
    
    @Test
    void assertDoGetWithUnauthorizedRequestFromMcpValidator() throws ServletException, IOException {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/gateway");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn(null);
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of(HttpHeaders.MCP_SESSION_ID)));
        when(request.getHeaders(HttpHeaders.MCP_SESSION_ID)).thenReturn(Collections.enumeration(List.of("missing-session")));
        HttpServletResponse response = mock(HttpServletResponse.class);
        StreamableHttpMCPServlet servlet = new StreamableHttpMCPServlet(sessionManager, MCPTransportJsonMapperFactory.create(), "127.0.0.1", "foo_token", "/gateway");
        servlet.doGet(request, response);
        verify(sessionManager, never()).hasSession("missing-session");
        verify(response).sendError(401, "Unauthorized.");
    }
}
