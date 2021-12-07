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
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class SM4EncryptAlgorithmTest {
    
    static {
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }
    
    private EncryptAlgorithm encryptAlgorithm;
    
    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("sm4-key", "4D744E003D713D054E7E407C350E447E");
        props.setProperty("sm4-mode", "ECB");
        props.setProperty("sm4-padding", "PKCS5Padding");
        encryptAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new ShardingSphereAlgorithmConfiguration("SM4", props), EncryptAlgorithm.class);
    }
    
    @Test
    public void assertEncryptWithECBAndPKCS5Padding() {
        assertThat(encryptAlgorithm.encrypt("test"), is("028654f2ca4f575dee9e1faae85dadde"));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertEncryptWithoutKey() {
        Properties props = new Properties();
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
        props.setProperty("sm4-mode", "ECB");
        props.setProperty("sm4-padding", "PKCS5Padding");
        assertThat(encryptAlgorithm.encrypt("test"), is("028654f2ca4f575dee9e1faae85dadde"));
    }
    
    @Test
    public void assertEncryptWithNullPlaintext() {
        assertNull(encryptAlgorithm.encrypt(null));
    }
    
    @Test
    public void assertDecrypt() {
        assertThat(encryptAlgorithm.decrypt("028654f2ca4f575dee9e1faae85dadde").toString(), is("test"));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertDecryptWithoutKey() {
        Properties props = new Properties();
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
        props.setProperty("sm4-mode", "ECB");
        props.setProperty("sm4-padding", "PKCS5Padding");
        assertThat(encryptAlgorithm.decrypt("028654f2ca4f575dee9e1faae85dadde").toString(), is("test"));
    }
    
    @Test
    public void assertDecryptWithNullCiphertext() {
        assertNull(encryptAlgorithm.decrypt(null));
    }
}
