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

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.spec.HttpHeaders;
import org.apache.shardingsphere.mcp.api.session.MCPSessionAttribution;
import org.apache.shardingsphere.mcp.bootstrap.config.SessionAttributionSourceConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.SessionAttributionResolver;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShardingSphereServerTransportSecurityValidatorTest {
    
    @Test
    void assertValidateHeadersWithMatchedSessionAttribution() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession("session-1");
        sessionManager.bindSessionAttribution("session-1", new MCPSessionAttribution("subject", "gateway", Map.of()));
        ShardingSphereServerTransportSecurityValidator actual = new ShardingSphereServerTransportSecurityValidator(sessionManager, List.of(),
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        assertDoesNotThrow(() -> actual.validateHeaders(Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), "X-Test-Subject", List.of("subject"),
                "X-Test-Source", List.of("gateway"))));
    }
    
    @Test
    void assertValidateHeadersWithMismatchedSessionAttribution() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession("session-1");
        sessionManager.bindSessionAttribution("session-1", new MCPSessionAttribution("subject", "gateway", Map.of()));
        ShardingSphereServerTransportSecurityValidator actual = new ShardingSphereServerTransportSecurityValidator(sessionManager, List.of(),
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        ServerTransportSecurityException exception = assertThrows(ServerTransportSecurityException.class, () -> actual.validateHeaders(
                Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), "X-Test-Subject", List.of("other"), "X-Test-Source", List.of("gateway"))));
        assertThat(exception.getMessage(), is("Session attribution does not match existing binding for session `session-1`."));
    }
    
    @Test
    void assertValidateHeadersWithUnboundSessionAttribution() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession("session-1");
        ShardingSphereServerTransportSecurityValidator actual = new ShardingSphereServerTransportSecurityValidator(sessionManager, List.of(),
                new SessionAttributionResolver(new SessionAttributionSourceConfiguration("X-Test-Subject", "X-Test-Source", "X-Test-Attr-")));
        ServerTransportSecurityException exception = assertThrows(ServerTransportSecurityException.class, () -> actual.validateHeaders(
                Map.of(HttpHeaders.MCP_SESSION_ID, List.of("session-1"), "X-Test-Subject", List.of("subject"), "X-Test-Source", List.of("gateway"))));
        assertThat(exception.getStatusCode(), is(400));
        assertThat(exception.getMessage(), is("Session attribution is not bound for session `session-1`."));
        assertThat(sessionManager.findSessionAttribution("session-1"), is(Optional.empty()));
    }
}
