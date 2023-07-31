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

import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class RC4EncryptAlgorithmTest {
    
    private StandardEncryptAlgorithm<Object, String> encryptAlgorithm;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        encryptAlgorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "RC4", PropertiesBuilder.build(new Property("rc4-key-value", "test-sharding")));
    }
    
    @Test
    void assertEncode() {
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("4Tn7lQ=="));
    }
    
    @Test
    void assertEncryptNullValue() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    void assertKeyIsTooLong() {
        assertThrows(EncryptAlgorithmInitializationException.class,
                () -> encryptAlgorithm.init(PropertiesBuilder.build(new Property("rc4-key-value", IntStream.range(0, 100).mapToObj(each -> "test").collect(Collectors.joining())))));
    }
    
    @Test
    void assertKeyIsTooShort() {
        assertThrows(EncryptAlgorithmInitializationException.class,
                () -> encryptAlgorithm.init(PropertiesBuilder.build(new Property("rc4-key-value", "test"))));
    }
    
    @Test
    void assertDecode() {
        assertThat(encryptAlgorithm.decrypt("4Tn7lQ==", mock(EncryptContext.class)).toString(), is("test"));
    }
    
    @Test
    void assertDecryptNullValue() {
        assertNull(encryptAlgorithm.decrypt(null, mock(EncryptContext.class)));
    }
}
