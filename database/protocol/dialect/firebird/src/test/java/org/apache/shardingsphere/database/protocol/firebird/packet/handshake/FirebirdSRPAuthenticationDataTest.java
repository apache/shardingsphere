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

package org.apache.shardingsphere.database.protocol.firebird.packet.handshake;

import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdSRPAuthenticationDataTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("normalizeLoginArguments")
    void assertNormalizeLogin(final String name, final String login, final String expectedLogin) {
        assertThat(FirebirdSRPAuthenticationData.normalizeLogin(login), is(expectedLogin));
    }
    
    @Test
    void assertFirebirdSRPAuthenticationData() {
        FirebirdSRPAuthenticationData actual = new FirebirdSRPAuthenticationData("SHA-1", "alice", "password", "4");
        assertThat(actual.getClientPublicKey(), is(new BigInteger("4")));
        assertThat(actual.getClientProofHashAlgorithm(), is("SHA-1"));
        assertNotNull(actual.getPrivateKey());
        assertNotNull(actual.getPublicKey());
        assertNotNull(actual.getSalt());
        assertThat(actual.getSalt().length, is(32));
        assertNotNull(actual.getVerifier());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("serverProofArguments")
    void assertServerProof(final String name, final String userPublicKey, final String user) {
        FirebirdSRPAuthenticationData actual = new FirebirdSRPAuthenticationData("SHA-1", "alice", "password", userPublicKey);
        byte[] actualServerProof = actual.serverProof(user);
        assertThat(actualServerProof.length, is(20));
        assertNotNull(actual.getSessionKey());
        assertThat(actual.getSessionKey().length, is(20));
    }
    
    @Test
    void assertServerProofWithUnknownHashAlgorithm() {
        FirebirdSRPAuthenticationData actual = new FirebirdSRPAuthenticationData("SHA-0", "alice", "password", "04");
        assertThat(assertThrows(FirebirdProtocolException.class, () -> actual.serverProof("alice")).getMessage(), is("Unrecognised hash algorithm `SHA-0`."));
    }
    
    @Test
    void assertGetPublicKeyHex() {
        FirebirdSRPAuthenticationData actual = new FirebirdSRPAuthenticationData("SHA-1", "alice", "password", "04");
        String actualPublicKeyHex = actual.getPublicKeyHex();
        assertFalse(actualPublicKeyHex.isEmpty());
        assertTrue(actualPublicKeyHex.matches("[0-9A-F]+"));
        assertThat(actualPublicKeyHex.length() % 2, is(0));
        assertTrue(actualPublicKeyHex.length() <= 256);
    }
    
    private static Stream<Arguments> normalizeLoginArguments() {
        return Stream.of(
                Arguments.of("null", null, null),
                Arguments.of("uppercase_unquoted", "abc", "ABC"),
                Arguments.of("leading_quote_only", "\"Abc", "\"ABC"),
                Arguments.of("quoted_escaped_quote", "\"Ab\"\"c\"", "Ab\"c"),
                Arguments.of("quoted_trailing_quote", "\"Abc\"\"", "Abc"),
                Arguments.of("quoted_unpaired_quote", "\"Ab\"c\"", "Ab"),
                Arguments.of("empty", "", ""),
                Arguments.of("quote_only", "\"\"", "\"\""));
    }
    
    private static Stream<Arguments> serverProofArguments() {
        return Stream.of(
                Arguments.of("long_client_public_key", IntStream.range(0, 130).mapToObj(i -> "AB").collect(Collectors.joining()), "CSNB"),
                Arguments.of("zero_client_public_key", "0", "alice"),
                Arguments.of("regular_client_public_key", "04", "alice"));
    }
}
