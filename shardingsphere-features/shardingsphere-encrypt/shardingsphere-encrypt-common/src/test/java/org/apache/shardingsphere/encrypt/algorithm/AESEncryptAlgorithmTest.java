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
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class AESEncryptAlgorithmTest {
    
    static {
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }
    
    private EncryptAlgorithm encryptAlgorithm;
    
    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("aes.key.value", "test");
        encryptAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new ShardingSphereAlgorithmConfiguration("AES", props), EncryptAlgorithm.class);
    }
    
    @Test
    public void assertEncrypt() {
        assertThat(encryptAlgorithm.encrypt("test"), is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertEncryptWithoutKey() {
        Properties props = new Properties();
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
        assertThat(encryptAlgorithm.encrypt("test"), is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test
    public void assertEncryptWithNullPlaintext() {
        assertNull(encryptAlgorithm.encrypt(null));
    }
    
    @Test
    public void assertDecrypt() {
        assertThat(encryptAlgorithm.decrypt("dSpPiyENQGDUXMKFMJPGWA==").toString(), is("test"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertDecryptWithoutKey() {
        Properties props = new Properties();
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
        assertThat(encryptAlgorithm.decrypt("dSpPiyENQGDUXMKFMJPGWA==").toString(), is("test"));
    }
    
    @Test
    public void assertDecryptWithNullCiphertext() {
        assertNull(encryptAlgorithm.decrypt(null));
    }
}
