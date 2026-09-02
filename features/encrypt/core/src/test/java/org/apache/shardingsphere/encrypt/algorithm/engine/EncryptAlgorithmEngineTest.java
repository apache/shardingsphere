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

package org.apache.shardingsphere.encrypt.algorithm.engine;

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptAlgorithmEngineTest {
    
    @Test
    void assertEncryptWithBase64Encoder() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        when(encryptAlgorithm.encrypt(eq("plain"), any(AlgorithmSQLContext.class))).thenReturn("cipher".getBytes(StandardCharsets.UTF_8));
        Object actualCipherValue = EncryptAlgorithmEngine.encrypt(encryptAlgorithm, "plain", createAlgorithmSQLContext(), "BASE64", false);
        assertThat(actualCipherValue, is("Y2lwaGVy"));
    }
    
    @Test
    void assertEncryptWithHexEncoder() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        when(encryptAlgorithm.encrypt(eq("plain"), any(AlgorithmSQLContext.class))).thenReturn(new byte[]{0, 15, 16, (byte) 0xFF});
        Object actualCipherValue = EncryptAlgorithmEngine.encrypt(encryptAlgorithm, "plain", createAlgorithmSQLContext(), "HEX", false);
        assertThat(actualCipherValue, is("000F10FF"));
    }
    
    @Test
    void assertEncryptWithBinaryPreference() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        byte[] expectedCipherValue = "cipher".getBytes(StandardCharsets.UTF_8);
        when(encryptAlgorithm.encrypt(eq("plain"), any(AlgorithmSQLContext.class))).thenReturn(expectedCipherValue);
        Object actualCipherValue = EncryptAlgorithmEngine.encrypt(encryptAlgorithm, "plain", createAlgorithmSQLContext(), "BASE64", true);
        assertThat((byte[]) actualCipherValue, is(expectedCipherValue));
    }
    
    @Test
    void assertEncryptWithoutEncoder() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        when(encryptAlgorithm.encrypt(eq("plain"), any(AlgorithmSQLContext.class))).thenReturn("cipher".getBytes(StandardCharsets.UTF_8));
        assertThat(EncryptAlgorithmEngine.encrypt(encryptAlgorithm, "plain", createAlgorithmSQLContext(), null, false), is("cipher"));
    }
    
    @Test
    void assertEncryptWithTextAlgorithm() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(Object.class);
        when(encryptAlgorithm.encrypt(eq("plain"), any(AlgorithmSQLContext.class))).thenReturn("cipher");
        Object actualCipherValue = EncryptAlgorithmEngine.encrypt(encryptAlgorithm, "plain", createAlgorithmSQLContext(), null, false);
        assertThat(actualCipherValue, is("cipher"));
    }
    
    @Test
    void assertDecryptWithBase64Encoder() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        when(encryptAlgorithm.decrypt(any(byte[].class), any(AlgorithmSQLContext.class))).thenReturn("plain");
        Object actualPlainValue = EncryptAlgorithmEngine.decrypt(encryptAlgorithm, "Y2lwaGVy", createAlgorithmSQLContext(), "BASE64");
        ArgumentCaptor<byte[]> actualCipherValueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(encryptAlgorithm).decrypt(actualCipherValueCaptor.capture(), any(AlgorithmSQLContext.class));
        assertThat(actualCipherValueCaptor.getValue(), is("cipher".getBytes(StandardCharsets.UTF_8)));
        assertThat(actualPlainValue, is("plain"));
    }
    
    @Test
    void assertDecryptWithBase64EncoderAndBoundaryWhitespace() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        when(encryptAlgorithm.decrypt(any(byte[].class), any(AlgorithmSQLContext.class))).thenReturn("plain");
        Object actualPlainValue = EncryptAlgorithmEngine.decrypt(encryptAlgorithm, "Y2lwaGVy                          ", createAlgorithmSQLContext(), "BASE64");
        ArgumentCaptor<byte[]> actualCipherValueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(encryptAlgorithm).decrypt(actualCipherValueCaptor.capture(), any(AlgorithmSQLContext.class));
        assertThat(actualCipherValueCaptor.getValue(), is("cipher".getBytes(StandardCharsets.UTF_8)));
        assertThat(actualPlainValue, is("plain"));
    }
    
    @Test
    void assertDecryptWithHexEncoder() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        when(encryptAlgorithm.decrypt(any(byte[].class), any(AlgorithmSQLContext.class))).thenReturn("plain");
        Object actualPlainValue = EncryptAlgorithmEngine.decrypt(encryptAlgorithm, "000f10Ff", createAlgorithmSQLContext(), "HEX");
        ArgumentCaptor<byte[]> actualCipherValueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(encryptAlgorithm).decrypt(actualCipherValueCaptor.capture(), any(AlgorithmSQLContext.class));
        assertThat(actualCipherValueCaptor.getValue(), is(new byte[]{0, 15, 16, (byte) 0xFF}));
        assertThat(actualPlainValue, is("plain"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"0", "GG"})
    void assertDecryptWithInvalidHexEncoder(final String cipherValue) {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        assertThrows(IllegalArgumentException.class, () -> EncryptAlgorithmEngine.decrypt(encryptAlgorithm, cipherValue, createAlgorithmSQLContext(), null));
    }
    
    @Test
    void assertDecryptWithoutEncoder() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        when(encryptAlgorithm.decrypt(any(byte[].class), any(AlgorithmSQLContext.class))).thenReturn("plain");
        assertThat(EncryptAlgorithmEngine.decrypt(encryptAlgorithm, "cipher", createAlgorithmSQLContext(), null), is("plain"));
        ArgumentCaptor<byte[]> actualCipherValueCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(encryptAlgorithm).decrypt(actualCipherValueCaptor.capture(), any(AlgorithmSQLContext.class));
        assertThat(actualCipherValueCaptor.getValue(), is("cipher".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertDecryptWithTextAlgorithm() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(Object.class);
        when(encryptAlgorithm.decrypt(eq("cipher"), any(AlgorithmSQLContext.class))).thenReturn("plain");
        Object actualPlainValue = EncryptAlgorithmEngine.decrypt(encryptAlgorithm, "cipher", createAlgorithmSQLContext(), null);
        assertThat(actualPlainValue, is("plain"));
    }
    
    @Test
    void assertFindEncoderFromAlgorithm() {
        EncryptAlgorithm encryptAlgorithm = mockEncryptAlgorithm(byte[].class);
        when(encryptAlgorithm.getEncoder()).thenReturn(Optional.of("BASE64"));
        assertThat(EncryptAlgorithmEngine.findEncoder(encryptAlgorithm), is(Optional.of("BASE64")));
    }
    
    @Test
    void assertFindEncoderFromConfiguration() {
        Properties props = new Properties();
        props.setProperty("aes-encoder", "HEX");
        assertThat(EncryptAlgorithmEngine.findEncoder(new AlgorithmConfiguration("AES", props)), is(Optional.of("HEX")));
    }
    
    private EncryptAlgorithm mockEncryptAlgorithm(final Class<?> cipherValueType) {
        EncryptAlgorithm result = mock(EncryptAlgorithm.class);
        when(result.getMetaData()).thenReturn(new EncryptAlgorithmMetaData(true, true, false, cipherValueType));
        when(result.getType()).thenReturn("AES");
        when(result.getEncoder()).thenReturn(Optional.empty());
        when(result.toConfiguration()).thenReturn(new AlgorithmConfiguration("AES", new Properties()));
        return result;
    }
    
    private AlgorithmSQLContext createAlgorithmSQLContext() {
        return new AlgorithmSQLContext("foo_db", "foo_schema", "foo_tbl", "foo_col");
    }
}
