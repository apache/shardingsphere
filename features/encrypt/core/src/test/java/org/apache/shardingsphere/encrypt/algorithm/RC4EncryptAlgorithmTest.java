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

package org.apache.shardingsphere.encrypt.algorithm;

import org.apache.shardingsphere.encrypt.exception.algorithm.EncryptAlgorithmInitializationException;
import org.apache.shardingsphere.encrypt.factory.EncryptAlgorithmFactory;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class RC4EncryptAlgorithmTest {
    
    private EncryptAlgorithm<Object, String> encryptAlgorithm;
    
    @Before
    public void setUp() {
        encryptAlgorithm = EncryptAlgorithmFactory.newInstance(new AlgorithmConfiguration("Rc4", createProperties()));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("rc4-key-value", "test-sharding");
        return result;
    }
    
    @Test
    public void assertEncode() {
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("4Tn7lQ=="));
    }
    
    @Test
    public void assertEncryptNullValue() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test(expected = EncryptAlgorithmInitializationException.class)
    public void assertKeyIsToLong() {
        encryptAlgorithm.init(createInvalidProperties());
    }
    
    private Properties createInvalidProperties() {
        Properties result = new Properties();
        StringBuilder keyBuffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            keyBuffer.append("test");
        }
        result.setProperty("rc4-key-value", keyBuffer.toString());
        return result;
    }
    
    @Test
    public void assertDecode() {
        assertThat(encryptAlgorithm.decrypt("4Tn7lQ==", mock(EncryptContext.class)).toString(), is("test"));
    }
    
    @Test
    public void assertDecryptNullValue() {
        assertNull(encryptAlgorithm.decrypt(null, mock(EncryptContext.class)));
    }
}
