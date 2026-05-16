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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpBearerAuthorizationHandlerTest {
    
    @Test
    void assertAuthorizeWhenAccessTokenIsBlank() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertTrue(new HttpBearerAuthorizationHandler(createConfig("")).authorize(mock(HttpServletRequest.class), response));
        verify(response, never()).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
    
    @Test
    void assertAuthorizeWhenBearerTokenMatches() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("bearer test-token");
        assertTrue(new HttpBearerAuthorizationHandler(createConfig("test-token")).authorize(request, mock(HttpServletResponse.class)));
    }
    
    @Test
    void assertRejectMalformedBearerToken() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("BearerX test-token");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1:18088/mcp"));
        when(request.getRequestURI()).thenReturn("/mcp");
        assertFalse(new HttpBearerAuthorizationHandler(createConfig("X test-token")).authorize(request, mock(HttpServletResponse.class)));
    }
    
    @Test
    void assertRejectQueryAccessTokenWithoutAuthorizationHeader() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getParameter("access_token")).thenReturn("test-token");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1:18088/mcp"));
        when(request.getRequestURI()).thenReturn("/mcp");
        HttpServletResponse response = mock(HttpServletResponse.class);
        boolean actual = new HttpBearerAuthorizationHandler(createConfig("test-token")).authorize(request, response);
        assertFalse(actual);
        verify(request, never()).getParameter("access_token");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
    
    @Test
    void assertRejectUnauthorizedRequestWithProtectedResourceMetadataChallenge() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer wrong-token");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1:18088/mcp"));
        when(request.getRequestURI()).thenReturn("/mcp");
        HttpServletResponse response = mock(HttpServletResponse.class);
        boolean actual = new HttpBearerAuthorizationHandler(createConfig("test-token")).authorize(request, response);
        assertFalse(actual);
        verify(response).setHeader("WWW-Authenticate", "Bearer resource_metadata=\"http://127.0.0.1:18088/.well-known/oauth-protected-resource/mcp\", scope=\"mcp.read\"");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
    
    @Test
    void assertRejectUnauthorizedRequestWithoutProtectedResourceMetadataChallenge() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        boolean actual = new HttpBearerAuthorizationHandler("test-token", "/mcp", List.of(), false).authorize(request, response);
        assertFalse(actual);
        verify(response).setHeader("WWW-Authenticate", "Bearer");
    }
    
    @Test
    void assertAuthorizeWithOAuthToken() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer foo_token");
        HttpServletResponse response = mock(HttpServletResponse.class);
        boolean actual = createOAuthHandler(createOAuthResponse("mcp.read")).authorize(request, response);
        assertTrue(actual);
        verify(response, never()).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
    
    @Test
    void assertRejectMalformedOAuthBearerToken() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("BearerX foo_token");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1:18088/mcp"));
        when(request.getRequestURI()).thenReturn("/mcp");
        HttpServletResponse response = mock(HttpServletResponse.class);
        boolean actual = createOAuthHandler(createOAuthResponse("mcp.read")).authorize(request, response);
        assertFalse(actual);
        verify(response).setHeader("WWW-Authenticate",
                "Bearer resource_metadata=\"http://127.0.0.1:18088/.well-known/oauth-protected-resource/mcp\", error=\"invalid_token\", scope=\"mcp.read\"");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
    
    @Test
    void assertRejectOAuthTokenWithInsufficientScope() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer foo_token");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1:18088/mcp"));
        when(request.getRequestURI()).thenReturn("/mcp");
        HttpServletResponse response = mock(HttpServletResponse.class);
        boolean actual = createOAuthHandler(createOAuthResponse("mcp.write")).authorize(request, response);
        assertFalse(actual);
        verify(response).setHeader("WWW-Authenticate",
                "Bearer resource_metadata=\"http://127.0.0.1:18088/.well-known/oauth-protected-resource/mcp\", error=\"insufficient_scope\", scope=\"mcp.read\"");
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }
    
    private HttpTransportConfiguration createConfig(final String accessToken) {
        return accessToken.isEmpty()
                ? new HttpTransportConfiguration(true, "127.0.0.1", false, "", 18088, "/mcp", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "",
                        new OAuthIntrospectionConfiguration())
                : new HttpTransportConfiguration(true, "127.0.0.1", false, accessToken, 18088, "/mcp", Collections.emptyList(), List.of("https://auth.example.test"), List.of("mcp.read"), "",
                        new OAuthIntrospectionConfiguration());
    }
    
    private HttpBearerAuthorizationHandler createOAuthHandler(final Map<String, Object> introspectionResponse) {
        HttpTransportConfiguration config = new HttpTransportConfiguration(true, "127.0.0.1", false, "", 18088, "/mcp", Collections.emptyList(), List.of("https://auth.example.test"),
                List.of("mcp.read"), "http://127.0.0.1:18088/mcp", new OAuthIntrospectionConfiguration("https://auth.example.test/introspect", "foo_client", "foo_secret", "", 0L));
        return new HttpBearerAuthorizationHandler("", "/mcp", List.of("mcp.read"), true, new OAuthTokenValidator(config, token -> introspectionResponse,
                () -> 1800000000000L));
    }
    
    private Map<String, Object> createOAuthResponse(final String scope) {
        return Map.of("active", true, "iss", "https://auth.example.test", "aud", List.of("http://127.0.0.1:18088/mcp"), "exp", 1800000060L, "nbf", 1799999999L,
                "scope", scope);
    }
}
