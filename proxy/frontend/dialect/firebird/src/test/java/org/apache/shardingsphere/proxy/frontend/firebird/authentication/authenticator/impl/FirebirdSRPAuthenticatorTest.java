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

package org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.impl;

import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdSRPAuthenticationData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdSRPAuthenticatorTest {
    
    private final FirebirdSRPAuthenticator authenticator = new FirebirdSRPAuthenticator();
    
    private final ShardingSphereUser user = new ShardingSphereUser("foo", "password", "");
    
    @Test
    void assertAuthenticationMethodName() {
        assertThat(authenticator.getAuthenticationMethodName(), is("Srp"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("authenticateSuccessArguments")
    void assertAuthenticateSuccess(final String name, final String clientProof) {
        assertTrue(authenticator.authenticate(user, new Object[]{null, mockAuthenticationData(), clientProof}));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("authenticateFailureArguments")
    void assertAuthenticateFailure(final String name, final String clientProof) {
        assertFalse(authenticator.authenticate(user, new Object[]{null, mockAuthenticationData(), clientProof}));
    }
    
    private FirebirdSRPAuthenticationData mockAuthenticationData() {
        FirebirdSRPAuthenticationData result = mock(FirebirdSRPAuthenticationData.class);
        when(result.serverProof("foo")).thenReturn(new byte[]{0, 10, 105});
        return result;
    }
    
    private static Stream<Arguments> authenticateSuccessArguments() {
        return Stream.of(
                Arguments.of("hex_text_with_leading_zeros", "000A69"),
                Arguments.of("hex_text_without_leading_zeros", "A69"),
                Arguments.of("lowercase_hex_text", "a69"));
    }
    
    private static Stream<Arguments> authenticateFailureArguments() {
        return Stream.of(
                Arguments.of("different_number", "A6A"),
                Arguments.of("non_hex_text", "wrong"),
                Arguments.of("empty_text", ""),
                Arguments.of("absent_proof", null));
    }
}
