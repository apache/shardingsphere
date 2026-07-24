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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator;

import io.modelcontextprotocol.spec.HttpHeaders;
import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.bootstrap.config.SessionAttributionSourceConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.SessionAttributionResolver;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.OriginHeaderConstraint;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.ProtocolVersionHeaderConstraint;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ShardingSphereServerTransportSecurityValidatorTest {
    
    @Test
    void assertValidateHeadersWithMatchedSessionIdentity() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "subject", "gateway", Map.of()));
        ShardingSphereServerTransportSecurityValidator actual = createValidator(sessionManager,
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        assertDoesNotThrow(() -> actual.validateHeaders(Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), "X-Test-Subject", List.of("subject"),
                "X-Test-Source", List.of("gateway"))));
    }
    
    @Test
    void assertValidateHeadersWithMismatchedSessionIdentity() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "subject", "gateway", Map.of()));
        ShardingSphereServerTransportSecurityValidator actual = createValidator(sessionManager,
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        MCPTransportSecurityException exception = assertThrows(MCPTransportSecurityException.class, () -> actual.validateHeaders(
                Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), "X-Test-Subject", List.of("other"), "X-Test-Source", List.of("gateway"))));
        assertThat(exception.getStatusCode(), is(400));
        assertThat(exception.getMessage(), is("Session attribution does not match this MCP session."));
        assertThat(exception.getCategory(), is("session_attribution_mismatch"));
    }
    
    @Test
    void assertValidateHeadersWithChangedSource() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "subject", "gateway", Map.of()));
        ShardingSphereServerTransportSecurityValidator actual = createValidator(sessionManager,
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        assertThrows(MCPTransportSecurityException.class, () -> actual.validateHeaders(Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"),
                "X-Test-Subject", List.of("subject"), "X-Test-Source", List.of("other"))));
    }
    
    @Test
    void assertValidateHeadersWithMissingSubject() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "subject", "gateway", Map.of()));
        ShardingSphereServerTransportSecurityValidator actual = createValidator(sessionManager,
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        MCPTransportSecurityException exception = assertThrows(MCPTransportSecurityException.class, () -> actual.validateHeaders(
                Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), "X-Test-Source", List.of("gateway"))));
        assertThat(exception.getStatusCode(), is(400));
        assertThat(exception.getMessage(), is("Session attribution does not match this MCP session."));
        assertThat(exception.getCategory(), is("session_attribution_mismatch"));
    }
    
    @Test
    void assertValidateHeadersWithChangedAttributes() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "subject", "gateway", Map.of("region", "ap-south")));
        ShardingSphereServerTransportSecurityValidator actual = createValidator(sessionManager,
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        assertThrows(MCPTransportSecurityException.class, () -> actual.validateHeaders(Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"),
                "X-Test-Subject", List.of("subject"), "X-Test-Source", List.of("gateway"), "X-Test-Attr-Region", List.of("eu-west"))));
    }
    
    @Test
    void assertValidateHeadersWithSubjectAddedToUnattributedSession() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        ShardingSphereServerTransportSecurityValidator actual = createValidator(sessionManager,
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        assertThrows(MCPTransportSecurityException.class, () -> actual.validateHeaders(
                Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), "X-Test-Subject", List.of("subject"), "X-Test-Source", List.of("gateway"))));
    }
    
    @Test
    void assertValidateHeadersWithMissingSession() {
        ShardingSphereServerTransportSecurityValidator actual = createValidator(new MCPSessionManager(Map.of()),
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        assertDoesNotThrow(() -> actual.validateHeaders(
                Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), "X-Test-Subject", List.of("subject"), "X-Test-Source", List.of("gateway"))));
    }
    
    @Test
    void assertValidateHeadersWithUnknownSessionSkipsProtocolValidation() {
        ProtocolVersionHeaderConstraint protocolVersionHeaderConstraint = mock(ProtocolVersionHeaderConstraint.class);
        ShardingSphereServerTransportSecurityValidator actual = new ShardingSphereServerTransportSecurityValidator(new MCPSessionManager(Map.of()),
                mock(OriginHeaderConstraint.class), protocolVersionHeaderConstraint, new SessionAttributionResolver(null));
        assertDoesNotThrow(() -> actual.validateHeaders(Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), HttpHeaders.PROTOCOL_VERSION, List.of("version"))));
        verifyNoInteractions(protocolVersionHeaderConstraint);
    }
    
    @Test
    void assertValidateOriginBeforeSessionIdentity() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "subject", "gateway", Map.of()));
        ShardingSphereServerTransportSecurityValidator actual = new ShardingSphereServerTransportSecurityValidator(sessionManager, new OriginHeaderConstraint(true),
                mock(ProtocolVersionHeaderConstraint.class),
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        MCPTransportSecurityException exception = assertThrows(MCPTransportSecurityException.class, () -> actual.validateHeaders(Map.of(
                "Origin", List.of("https://example.com"), HttpHeaders.MCP_SESSION_ID, List.of("session-1"),
                "X-Test-Subject", List.of("other"), "X-Test-Source", List.of("gateway"))));
        assertThat(exception.getStatusCode(), is(403));
        assertThat(exception.getCategory(), is(MCPTransportSecurityException.CATEGORY_ORIGIN_NOT_ALLOWED));
    }
    
    private ShardingSphereServerTransportSecurityValidator createValidator(final MCPSessionManager sessionManager,
                                                                           final SessionAttributionResolver sessionAttributionResolver) {
        return new ShardingSphereServerTransportSecurityValidator(sessionManager, mock(OriginHeaderConstraint.class), mock(ProtocolVersionHeaderConstraint.class),
                sessionAttributionResolver);
    }
}
