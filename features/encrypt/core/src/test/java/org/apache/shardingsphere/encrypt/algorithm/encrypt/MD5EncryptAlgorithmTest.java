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

package org.apache.shardingsphere.encrypt.algorithm.encrypt;

import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

public final class MD5EncryptAlgorithmTest {
    
    private StandardEncryptAlgorithm<Object, String> encryptAlgorithm;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        encryptAlgorithm = (StandardEncryptAlgorithm<Object, String>) TypedSPILoader.getService(EncryptAlgorithm.class, "MD5");
    }
    
    @Test
    public void assertEncrypt() {
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("098f6bcd4621d373cade4e832627b4f6"));
    }
    
    @Test
    public void assertEncryptWithNullPlaintext() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    public void assertEncryptWhenConfigSalt() {
        encryptAlgorithm.init(PropertiesBuilder.build(new Property("salt", "202cb962ac5907")));
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("0c243d2934937738f36514035d95344a"));
    }
    
    @Test
    public void assertDecrypt() {
        assertThat(encryptAlgorithm.decrypt("test", mock(EncryptContext.class)).toString(), is("test"));
    }
}
