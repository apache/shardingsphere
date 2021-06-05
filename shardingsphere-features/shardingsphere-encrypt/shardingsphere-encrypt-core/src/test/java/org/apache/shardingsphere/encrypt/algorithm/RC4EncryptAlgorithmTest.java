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

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class RC4EncryptAlgorithmTest {
    
    static {
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }
    
    private EncryptAlgorithm encryptAlgorithm;
    
    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("rc4-key-value", "test-sharding");
        encryptAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new ShardingSphereAlgorithmConfiguration("Rc4", props), EncryptAlgorithm.class);
    }
    
    @Test
    public void assertEncode() {
        assertThat(encryptAlgorithm.encrypt("test"), is("qn36NQ=="));
    }
    
    @Test
    public void assertEncryptWithNullPlaintext() {
        assertNull(encryptAlgorithm.encrypt(null));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertKeyIsToLong() {
        Properties props = new Properties();
        StringBuilder keyBuffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            keyBuffer.append("test");
        }
        props.setProperty("rc4-key-value", keyBuffer.toString());
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
    }
    
    @Test
    public void assertDecode() {
        assertThat(encryptAlgorithm.decrypt("qn36NQ==").toString(), is("test"));
    }
    
    @Test
    public void assertDecryptWithNullCiphertext() {
        assertNull(encryptAlgorithm.decrypt(null));
    }
    
    @Test
    public void assertGetProperties() {
        assertThat(encryptAlgorithm.getProps().getProperty("rc4-key-value"), is("test-sharding"));
    }
}
