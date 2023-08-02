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

package org.apache.shardingsphere.encrypt.algorithm.assisted;

import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.api.encrypt.assisted.AssistedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class MD5AssistedEncryptAlgorithmTest {
    
    private AssistedEncryptAlgorithm<Object, String> encryptAlgorithm;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        encryptAlgorithm = (AssistedEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "MD5");
    }
    
    @Test
    void assertEncrypt() {
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("098f6bcd4621d373cade4e832627b4f6"));
    }
    
    @Test
    void assertEncryptWithNullPlaintext() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    void assertEncryptWhenConfigSalt() {
        encryptAlgorithm.init(PropertiesBuilder.build(new Property("salt", "202cb962ac5907")));
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("0c243d2934937738f36514035d95344a"));
    }
}
