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

package org.apache.shardingsphere.encrypt.sm.algorithm;

import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SM3EncryptAlgorithmTest {
    
    private StandardEncryptAlgorithm<Object, String> encryptAlgorithm;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        encryptAlgorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "SM3", PropertiesBuilder.build(new Property("sm3-salt", "test1234")));
    }
    
    @Test
    void assertEncrypt() {
        Object actual = encryptAlgorithm.encrypt("test1234", mock(EncryptContext.class));
        assertThat(actual, is("9587fe084ee4b53fe629c6ae5519ee4d55def8ed4badc8588d3be9b99bd84aba"));
    }
    
    @Test
    void assertEncryptWithoutSalt() {
        encryptAlgorithm.init(new Properties());
        assertThat(encryptAlgorithm.encrypt("test1234", mock(EncryptContext.class)), is("ab847c6f2f6a53be88808c5221bd6ee0762e1af1def82b21d2061599b6cf5c79"));
    }
    
    @Test
    void assertEncryptWithNullPlaintext() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    void assertDecrypt() {
        Object actual = encryptAlgorithm.decrypt("ab847c6f2f6a53be88808c5221bd6ee0762e1af1def82b21d2061599b6cf5c79", mock(EncryptContext.class));
        assertThat(actual.toString(), is("ab847c6f2f6a53be88808c5221bd6ee0762e1af1def82b21d2061599b6cf5c79"));
    }
    
    @Test
    void assertDecryptWithoutSalt() {
        encryptAlgorithm.init(new Properties());
        Object actual = encryptAlgorithm.decrypt("ab847c6f2f6a53be88808c5221bd6ee0762e1af1def82b21d2061599b6cf5c79", mock(EncryptContext.class));
        assertThat(actual.toString(), is("ab847c6f2f6a53be88808c5221bd6ee0762e1af1def82b21d2061599b6cf5c79"));
    }
    
    @Test
    void assertDecryptWithNullCiphertext() {
        assertNull(encryptAlgorithm.decrypt(null, mock(EncryptContext.class)));
    }
}
