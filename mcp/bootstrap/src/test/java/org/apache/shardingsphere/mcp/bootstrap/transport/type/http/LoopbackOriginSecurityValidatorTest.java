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

package org.apache.shardingsphere.mcp.bootstrap.transport.type.http;

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import io.modelcontextprotocol.server.transport.ServerTransportSecurityValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoopbackOriginSecurityValidatorTest {
    
    @Test
    void assertValidateHeadersWithoutOrigin() {
        ServerTransportSecurityValidator validator = LoopbackOriginSecurityValidator.create("127.0.0.1");
        assertDoesNotThrow(() -> validator.validateHeaders(Map.of()));
    }
    
    @Test
    void assertValidateHeadersWithLoopbackOrigin() {
        ServerTransportSecurityValidator validator = LoopbackOriginSecurityValidator.create("127.0.0.1");
        assertDoesNotThrow(() -> validator.validateHeaders(Map.of("Origin", List.of("http://localhost:8080"))));
    }
    
    @Test
    void assertValidateHeadersWithLowercaseOriginHeader() {
        ServerTransportSecurityValidator validator = LoopbackOriginSecurityValidator.create("127.0.0.1");
        assertDoesNotThrow(() -> validator.validateHeaders(Map.of("origin", List.of("http://127.0.0.1:8080"))));
    }
    
    @Test
    void assertValidateHeadersWithRemoteOrigin() {
        ServerTransportSecurityValidator validator = LoopbackOriginSecurityValidator.create("127.0.0.1");
        ServerTransportSecurityException actual = assertThrows(ServerTransportSecurityException.class,
                () -> validator.validateHeaders(Map.of("Origin", List.of("http://example.com:8080"))));
        assertThat(actual.getStatusCode(), is(403));
        assertThat(actual.getMessage(), is("Origin is not allowed for the current binding."));
    }
    
    @Test
    void assertValidateHeadersWithInvalidOrigin() {
        ServerTransportSecurityValidator validator = LoopbackOriginSecurityValidator.create("127.0.0.1");
        ServerTransportSecurityException actual = assertThrows(ServerTransportSecurityException.class,
                () -> validator.validateHeaders(Map.of("Origin", List.of("://bad-origin"))));
        assertThat(actual.getStatusCode(), is(403));
        assertThat(actual.getMessage(), is("Origin is not allowed for the current binding."));
    }
    
    @Test
    void assertCreateWithRemoteBindHost() {
        ServerTransportSecurityValidator validator = LoopbackOriginSecurityValidator.create("0.0.0.0");
        assertDoesNotThrow(() -> validator.validateHeaders(Map.of("Origin", List.of("http://example.com:8080"))));
    }
}
