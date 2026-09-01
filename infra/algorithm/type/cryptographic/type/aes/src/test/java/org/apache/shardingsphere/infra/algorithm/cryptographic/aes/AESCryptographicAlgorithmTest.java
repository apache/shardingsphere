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

package org.apache.shardingsphere.infra.algorithm.cryptographic.aes;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.cryptographic.core.CryptographicAlgorithmEngine;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicAlgorithm;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AESCryptographicAlgorithmTest {
    
    private CryptographicAlgorithm cryptographicAlgorithm;
    
    @BeforeEach
    void setUp() {
        cryptographicAlgorithm = TypedSPILoader.getService(CryptographicAlgorithm.class, "AES",
                PropertiesBuilder.build(new Property("aes-key-value", "test"), new Property("digest-algorithm-name", "SHA-1")));
    }
    
    @Test
    void assertCreateNewInstanceWithoutAESKey() {
        assertThrows(AlgorithmInitializationException.class, () -> TypedSPILoader.getService(CryptographicAlgorithm.class, "AES"));
    }
    
    @Test
    void assertCreateNewInstanceWithEmptyAESKey() {
        assertThrows(AlgorithmInitializationException.class, () -> cryptographicAlgorithm.init(PropertiesBuilder.build(new Property("aes-key-value", ""))));
    }
    
    @Test
    void assertCreateNewInstanceWithEmptyDigestAlgorithm() {
        assertThrows(AlgorithmInitializationException.class, () -> cryptographicAlgorithm.init(
                PropertiesBuilder.build(new Property("aes-key-value", "123456abc"), new Property("digest-algorithm-name", ""))));
    }
    
    @Test
    void assertEncrypt() {
        assertThat(encryptBase64("test"), is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test
    void assertEncryptBytes() {
        assertThat(encryptBase64("test".getBytes(StandardCharsets.UTF_8)), is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test
    void assertEncryptNullValue() {
        assertNull(cryptographicAlgorithm.encrypt(null, CryptographicContext.DEFAULT));
    }
    
    @Test
    void assertDecrypt() {
        assertThat(CryptographicAlgorithmEngine.decrypt(cryptographicAlgorithm, "dSpPiyENQGDUXMKFMJPGWA==", CryptographicContext.DEFAULT, "BASE64"), is("test"));
    }
    
    @Test
    void assertDecryptWithBase64Whitespace() {
        assertThat(CryptographicAlgorithmEngine.decrypt(cryptographicAlgorithm, "dSpPiyENQGDUX\r\nMKFMJPGWA==", CryptographicContext.DEFAULT, "BASE64"), is("test"));
    }
    
    @Test
    void assertDecryptNullValue() {
        assertNull(CryptographicAlgorithmEngine.decrypt(cryptographicAlgorithm, null, CryptographicContext.DEFAULT, "BASE64"));
    }
    
    @Test
    void assertDecryptWithBinaryPlainValue() {
        byte[] cipherValue = cryptographicAlgorithm.encrypt(new byte[]{(byte) 0xFF}, CryptographicContext.DEFAULT);
        assertArrayEquals(new byte[]{(byte) 0xFF}, (byte[]) cryptographicAlgorithm.decrypt(cipherValue, new CryptographicContext(StandardCharsets.UTF_8, true)));
    }
    
    private String encryptBase64(final Object plainValue) {
        return (String) CryptographicAlgorithmEngine.encrypt(cryptographicAlgorithm, plainValue, CryptographicContext.DEFAULT, "BASE64", false);
    }
}
