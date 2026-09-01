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

package org.apache.shardingsphere.infra.algorithm.cryptographic.core;

import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicAlgorithm;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CryptographicAlgorithmEngineTest {
    
    @Test
    void assertEncryptWithBase64Encoder() {
        CryptographicAlgorithm cryptographicAlgorithm = mock(CryptographicAlgorithm.class);
        when(cryptographicAlgorithm.encrypt("plain", CryptographicContext.DEFAULT)).thenReturn("cipher".getBytes(StandardCharsets.UTF_8));
        Object actualCipherValue = CryptographicAlgorithmEngine.encrypt(cryptographicAlgorithm, "plain", CryptographicContext.DEFAULT, "BASE64", false);
        assertThat(actualCipherValue, is("Y2lwaGVy"));
    }
    
    @Test
    void assertEncryptWithHexEncoder() {
        CryptographicAlgorithm cryptographicAlgorithm = mock(CryptographicAlgorithm.class);
        when(cryptographicAlgorithm.encrypt("plain", CryptographicContext.DEFAULT)).thenReturn("cipher".getBytes(StandardCharsets.UTF_8));
        Object actualCipherValue = CryptographicAlgorithmEngine.encrypt(cryptographicAlgorithm, "plain", CryptographicContext.DEFAULT, "HEX", false);
        assertThat(actualCipherValue, is("636970686572"));
    }
    
    @Test
    void assertEncryptWithPreferBinaryType() {
        CryptographicAlgorithm cryptographicAlgorithm = mock(CryptographicAlgorithm.class);
        byte[] expectedCipherValue = "cipher".getBytes(StandardCharsets.UTF_8);
        when(cryptographicAlgorithm.encrypt("plain", CryptographicContext.DEFAULT)).thenReturn(expectedCipherValue);
        Object actualCipherValue = CryptographicAlgorithmEngine.encrypt(cryptographicAlgorithm, "plain", CryptographicContext.DEFAULT, "BASE64", true);
        assertArrayEquals(expectedCipherValue, (byte[]) actualCipherValue);
    }
    
    @Test
    void assertEncryptWithNullResult() {
        CryptographicAlgorithm cryptographicAlgorithm = mock(CryptographicAlgorithm.class);
        when(cryptographicAlgorithm.encrypt("plain", CryptographicContext.DEFAULT)).thenReturn(null);
        assertNull(CryptographicAlgorithmEngine.encrypt(cryptographicAlgorithm, "plain", CryptographicContext.DEFAULT, "BASE64", false));
    }
    
    @Test
    void assertDecryptWithBase64Encoder() {
        CryptographicAlgorithm cryptographicAlgorithm = mock(CryptographicAlgorithm.class);
        when(cryptographicAlgorithm.decrypt(any(byte[].class), same(CryptographicContext.DEFAULT))).thenReturn("plain");
        Object actualPlainValue = CryptographicAlgorithmEngine.decrypt(cryptographicAlgorithm, "Y2lwaGVy", CryptographicContext.DEFAULT, "BASE64");
        ArgumentCaptor<byte[]> actualCipherValueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(cryptographicAlgorithm).decrypt(actualCipherValueCaptor.capture(), same(CryptographicContext.DEFAULT));
        assertArrayEquals("cipher".getBytes(StandardCharsets.UTF_8), actualCipherValueCaptor.getValue());
        assertThat(actualPlainValue, is("plain"));
    }
    
    @Test
    void assertDecryptWithHexEncoder() {
        CryptographicAlgorithm cryptographicAlgorithm = mock(CryptographicAlgorithm.class);
        when(cryptographicAlgorithm.decrypt(any(byte[].class), same(CryptographicContext.DEFAULT))).thenReturn("plain");
        Object actualPlainValue = CryptographicAlgorithmEngine.decrypt(cryptographicAlgorithm, "636970686572", CryptographicContext.DEFAULT, "HEX");
        ArgumentCaptor<byte[]> actualCipherValueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(cryptographicAlgorithm).decrypt(actualCipherValueCaptor.capture(), same(CryptographicContext.DEFAULT));
        assertArrayEquals("cipher".getBytes(StandardCharsets.UTF_8), actualCipherValueCaptor.getValue());
        assertThat(actualPlainValue, is("plain"));
    }
    
    @Test
    void assertDecryptWithBinaryCipherValue() {
        CryptographicAlgorithm cryptographicAlgorithm = mock(CryptographicAlgorithm.class);
        when(cryptographicAlgorithm.decrypt(any(byte[].class), same(CryptographicContext.DEFAULT))).thenReturn("plain");
        byte[] expectedCipherValue = "cipher".getBytes(StandardCharsets.UTF_8);
        Object actualPlainValue = CryptographicAlgorithmEngine.decrypt(cryptographicAlgorithm, expectedCipherValue, CryptographicContext.DEFAULT, "BASE64");
        ArgumentCaptor<byte[]> actualCipherValueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(cryptographicAlgorithm).decrypt(actualCipherValueCaptor.capture(), same(CryptographicContext.DEFAULT));
        assertArrayEquals(expectedCipherValue, actualCipherValueCaptor.getValue());
        assertThat(actualPlainValue, is("plain"));
    }
    
    @Test
    void assertDecryptWithNullValue() {
        CryptographicAlgorithm cryptographicAlgorithm = mock(CryptographicAlgorithm.class);
        assertNull(CryptographicAlgorithmEngine.decrypt(cryptographicAlgorithm, null, CryptographicContext.DEFAULT, "BASE64"));
        verifyNoInteractions(cryptographicAlgorithm);
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "base64-empty, 0, BASE64, 0",
            "base64-one-byte, 1, BASE64, 4",
            "base64-two-bytes, 2, BASE64, 4",
            "base64-three-bytes, 3, BASE64, 4",
            "base64-four-bytes, 4, BASE64, 8",
            "hex-three-bytes, 3, HEX, 6"
    })
    void assertCalculateEncodedLength(final String name, final int byteLength, final String encoder, final int expectedLength) {
        assertThat(CryptographicAlgorithmEngine.calculateEncodedLength(byteLength, encoder), is(expectedLength));
    }
}
