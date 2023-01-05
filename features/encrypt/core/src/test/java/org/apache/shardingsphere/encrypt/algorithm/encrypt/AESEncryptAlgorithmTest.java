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
import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public final class AESEncryptAlgorithmTest {
    
    private StandardEncryptAlgorithm<Object, String> encryptAlgorithm;
    
    @Before
    public void setUp() {
        encryptAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("AES", PropertiesBuilder.build(new Property("aes-key-value", "test"))), EncryptAlgorithm.class);
    }
    
    @Test(expected = EncryptAlgorithmInitializationException.class)
    public void assertCreateNewInstanceWithoutAESKey() {
        ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration("AES", new Properties()), EncryptAlgorithm.class);
    }
    
    @Test
    public void assertEncrypt() {
        Object actual = encryptAlgorithm.encrypt("test", mock(EncryptContext.class));
        assertThat(actual, is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test
    public void assertEncryptNullValue() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    public void assertDecrypt() {
        Object actual = encryptAlgorithm.decrypt("dSpPiyENQGDUXMKFMJPGWA==", mock(EncryptContext.class));
        assertThat(actual.toString(), is("test"));
    }
    
    @Test
    public void assertDecryptNullValue() {
        assertNull(encryptAlgorithm.decrypt(null, mock(EncryptContext.class)));
    }
}
