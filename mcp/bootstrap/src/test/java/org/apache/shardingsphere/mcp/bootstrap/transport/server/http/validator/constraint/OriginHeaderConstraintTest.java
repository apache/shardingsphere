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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint;

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.MCPTransportSecurityException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OriginHeaderConstraintTest {
    
    @Test
    void assertValidateWithoutOrigin() {
        assertDoesNotThrow(() -> new OriginHeaderConstraint(true).validate(""));
    }
    
    @Test
    void assertValidateWithLoopbackOriginOnLoopbackBinding() {
        assertDoesNotThrow(() -> new OriginHeaderConstraint(true).validate("http://127.0.0.1:8080"));
    }
    
    @Test
    void assertValidateWithLocalhostOriginOnLoopbackBinding() {
        assertDoesNotThrow(() -> new OriginHeaderConstraint(true).validate("http://localhost:8080"));
    }
    
    @Test
    void assertValidateWithBracketedIpv6LoopbackOriginOnLoopbackBinding() {
        assertDoesNotThrow(() -> new OriginHeaderConstraint(true).validate("http://[::1]:8080"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDeniedOrigins")
    void assertValidateWithDeniedOrigin(final String name, final boolean loopbackBinding, final String origin) {
        ServerTransportSecurityException actual = assertThrows(ServerTransportSecurityException.class, () -> new OriginHeaderConstraint(loopbackBinding).validate(origin));
        assertThat(actual.getStatusCode(), is(403));
        assertThat(actual.getMessage(), is("Origin is not allowed by MCP HTTP transport policy."));
        assertThat(((MCPTransportSecurityException) actual).getCategory(), is("origin_not_allowed"));
    }
    
    private static Stream<Arguments> getDeniedOrigins() {
        return Stream.of(
                Arguments.of("remote origin on loopback binding", true, "http://example.com:8080"),
                Arguments.of("loopback origin on non-loopback binding", false, "http://127.0.0.1:8080"),
                Arguments.of("invalid origin", true, "://bad-origin"));
    }
}
