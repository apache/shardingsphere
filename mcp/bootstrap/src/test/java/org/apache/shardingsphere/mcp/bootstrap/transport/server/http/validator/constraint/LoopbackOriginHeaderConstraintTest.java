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
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoopbackOriginHeaderConstraintTest {
    
    @Test
    void assertValidateWithoutOrigin() {
        LoopbackOriginHeaderConstraint actualConstraint = new LoopbackOriginHeaderConstraint();
        assertDoesNotThrow(() -> actualConstraint.validate(""));
    }
    
    @Test
    void assertValidateWithLoopbackOrigin() {
        LoopbackOriginHeaderConstraint actualConstraint = new LoopbackOriginHeaderConstraint();
        assertDoesNotThrow(() -> actualConstraint.validate("http://localhost:8080"));
    }
    
    @Test
    void assertValidateWithLowercaseOriginHeader() {
        LoopbackOriginHeaderConstraint actualConstraint = new LoopbackOriginHeaderConstraint();
        assertDoesNotThrow(() -> actualConstraint.validate("http://127.0.0.1:8080"));
    }
    
    @Test
    void assertValidateWithBracketedIpv6LoopbackOrigin() {
        LoopbackOriginHeaderConstraint actualConstraint = new LoopbackOriginHeaderConstraint();
        assertDoesNotThrow(() -> actualConstraint.validate("http://[::1]:8080"));
    }
    
    @Test
    void assertValidateWithRemoteOrigin() {
        LoopbackOriginHeaderConstraint actualConstraint = new LoopbackOriginHeaderConstraint();
        ServerTransportSecurityException actual = assertThrows(ServerTransportSecurityException.class, () -> actualConstraint.validate("http://example.com:8080"));
        assertThat(actual.getStatusCode(), is(403));
        assertThat(actual.getMessage(), is("Origin is not allowed for the current binding."));
    }
    
    @Test
    void assertValidateWithInvalidOrigin() {
        LoopbackOriginHeaderConstraint actualConstraint = new LoopbackOriginHeaderConstraint();
        ServerTransportSecurityException actual = assertThrows(ServerTransportSecurityException.class, () -> actualConstraint.validate("://bad-origin"));
        assertThat(actual.getStatusCode(), is(403));
        assertThat(actual.getMessage(), is("Origin is not allowed for the current binding."));
    }
}
