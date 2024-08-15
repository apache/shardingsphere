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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.cryptographic.core.CryptographicAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class AESCryptographicAlgorithmTest {
    
    private CryptographicAlgorithm cryptographicAlgorithm;
    
    @BeforeEach
    void setUp() {
        cryptographicAlgorithm =
                TypedSPILoader.getService(CryptographicAlgorithm.class, "AES", PropertiesBuilder.build(new Property("aes-key-value", "test"), new Property("digest-algorithm-name", "SHA-1")));
    }
    
    @Test
    void assertDigestAlgorithm() {
        MockedStatic<DigestUtils> digestUtilsMockedStatic = mockStatic(DigestUtils.class, Answers.CALLS_REAL_METHODS);
        TypedSPILoader.getService(CryptographicAlgorithm.class, "AES", PropertiesBuilder.build(new Property("aes-key-value", "test"), new Property("digest-algorithm-name", "SHA-1")));
        digestUtilsMockedStatic.verify(() -> DigestUtils.getDigest("SHA-1"), times(1));
        digestUtilsMockedStatic.close();
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
        assertThrows(AlgorithmInitializationException.class, () -> cryptographicAlgorithm.init(PropertiesBuilder.build(new Property("aes-key-value", "123456abc"),
                new Property("digest-algorithm-name", ""))));
    }
    
    @Test
    void assertEncrypt() {
        Object actual = cryptographicAlgorithm.encrypt("test");
        assertThat(actual, is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test
    void assertEncryptNullValue() {
        assertNull(cryptographicAlgorithm.encrypt(null));
    }
    
    @Test
    void assertDecrypt() {
        Object actual = cryptographicAlgorithm.decrypt("dSpPiyENQGDUXMKFMJPGWA==");
        assertThat(actual.toString(), is("test"));
    }
    
    @Test
    void assertDecryptNullValue() {
        assertNull(cryptographicAlgorithm.decrypt(null));
    }
}
