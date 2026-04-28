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
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.SessionRequiredTransportHeaderConstraint;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.TransportHeaderConstraint;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ShardingSphereServerTransportSecurityValidatorTest {
    
    @Test
    void assertValidateHeadersWithNoFailure() {
        ShardingSphereServerTransportSecurityValidator validator = new ShardingSphereServerTransportSecurityValidator(mock(), List.of());
        assertDoesNotThrow(() -> validator.validateHeaders(Map.of()));
    }
    
    @Test
    void assertValidateHeadersWithFailure() throws ServerTransportSecurityException {
        TransportHeaderConstraint first = mock(TransportHeaderConstraint.class);
        doThrow(new ServerTransportSecurityException(401, "Unauthorized.")).when(first).validate("");
        TransportHeaderConstraint second = mock(TransportHeaderConstraint.class);
        ShardingSphereServerTransportSecurityValidator validator = new ShardingSphereServerTransportSecurityValidator(mock(), List.of(first, second));
        ServerTransportSecurityException actual = assertThrows(ServerTransportSecurityException.class, () -> validator.validateHeaders(Map.of()));
        assertThat(actual.getStatusCode(), is(401));
        assertThat(actual.getMessage(), is("Unauthorized."));
        verifyNoInteractions(second);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertValidateHeadersWithSessionConstraintArguments")
    void assertValidateHeadersWithSessionConstraint(final String name, final Map<String, List<String>> headers,
                                                    final String expectedSessionId, final boolean sessionExists, final String expectedConstraintValue) throws ServerTransportSecurityException {
        MCPSessionManager sessionManager = mock(MCPSessionManager.class);
        SessionRequiredTransportHeaderConstraint constraint = mock(SessionRequiredTransportHeaderConstraint.class);
        ShardingSphereServerTransportSecurityValidator validator = new ShardingSphereServerTransportSecurityValidator(sessionManager, List.of(constraint));
        if (null != expectedSessionId) {
            when(sessionManager.hasSession(expectedSessionId)).thenReturn(sessionExists);
        }
        if (null != expectedConstraintValue) {
            when(constraint.getConstraintKey()).thenReturn("Authorization");
        }
        assertDoesNotThrow(() -> validator.validateHeaders(headers));
        if (null == expectedSessionId) {
            verifyNoInteractions(sessionManager);
        } else {
            verify(sessionManager).hasSession(expectedSessionId);
        }
        if (null == expectedConstraintValue) {
            verifyNoInteractions(constraint);
        } else {
            verify(constraint).getConstraintKey();
            verify(constraint).validate(expectedConstraintValue);
        }
    }
    
    private static Stream<Arguments> assertValidateHeadersWithSessionConstraintArguments() {
        return Stream.of(
                Arguments.of("skip without session id", Map.of("Authorization", List.of("Bearer foo_token")), null, false, null),
                Arguments.of("skip with empty session header", Map.of("Mcp-Session-Id", List.<String>of()), null, false, null),
                Arguments.of("skip for unknown session", Map.of("Mcp-Session-Id", List.of("session-id")), "session-id", false, null),
                Arguments.of("validate with existing session",
                        Map.of("mcp-session-id", List.of(" session-id "), "authorization", List.of(" Bearer foo_token ")), "session-id", true, "Bearer foo_token"));
    }
}
