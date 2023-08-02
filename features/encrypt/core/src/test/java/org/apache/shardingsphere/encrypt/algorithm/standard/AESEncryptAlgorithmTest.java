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

package org.apache.shardingsphere.encrypt.algorithm.standard;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class AESEncryptAlgorithmTest {
    
    private StandardEncryptAlgorithm<Object, String> encryptAlgorithm;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        encryptAlgorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "AES", PropertiesBuilder.build(new Property("aes-key-value", "test")));
    }
    
    @Test
    void assertDefaultDigestAlgorithm() throws NoSuchAlgorithmException {
        MockedStatic<DigestUtils> digestUtilsMockedStatic = mockStatic(DigestUtils.class);
        digestUtilsMockedStatic.when(() -> DigestUtils.getDigest("SHA-1")).thenReturn(MessageDigest.getInstance("SHA-1"));
        TypedSPILoader.getService(EncryptAlgorithm.class, "AES", PropertiesBuilder.build(new Property("aes-key-value", "test")));
        digestUtilsMockedStatic.verify(() -> DigestUtils.getDigest("SHA-1"), times(1));
        digestUtilsMockedStatic.close();
    }
    
    @Test
    void assertSHA512DigestAlgorithm() throws NoSuchAlgorithmException {
        MockedStatic<DigestUtils> digestUtilsMockedStatic = mockStatic(DigestUtils.class);
        digestUtilsMockedStatic.when(() -> DigestUtils.getDigest("SHA-512")).thenReturn(MessageDigest.getInstance("SHA-512"));
        TypedSPILoader.getService(EncryptAlgorithm.class, "AES", PropertiesBuilder.build(new Property("aes-key-value", "test"), new Property("digest-algorithm-name", "SHA-512")));
        digestUtilsMockedStatic.verify(() -> DigestUtils.getDigest("SHA-512"), times(1));
        digestUtilsMockedStatic.close();
    }
    
    @Test
    void assertCreateNewInstanceWithoutAESKey() {
        assertThrows(EncryptAlgorithmInitializationException.class, () -> TypedSPILoader.getService(EncryptAlgorithm.class, "AES"));
    }
    
    @Test
    void assertCreateNewInstanceWithEmptyAESKey() {
        assertThrows(EncryptAlgorithmInitializationException.class, () -> encryptAlgorithm.init(PropertiesBuilder.build(new Property("aes-key-value", ""))));
    }
    
    @Test
    void assertEncrypt() {
        Object actual = encryptAlgorithm.encrypt("test", mock(EncryptContext.class));
        assertThat(actual, is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test
    void assertEncryptNullValue() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    void assertDecrypt() {
        Object actual = encryptAlgorithm.decrypt("dSpPiyENQGDUXMKFMJPGWA==", mock(EncryptContext.class));
        assertThat(actual.toString(), is("test"));
    }
    
    @Test
    void assertDecryptNullValue() {
        assertNull(encryptAlgorithm.decrypt(null, mock(EncryptContext.class)));
    }
}
