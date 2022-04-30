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

import org.apache.shardingsphere.encrypt.factory.EncryptAlgorithmFactory;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class SM4EncryptAlgorithmCBCTest {
    
    private EncryptAlgorithm<Object, String> encryptAlgorithm;
    
    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("sm4-key", "f201326119911788cFd30575b81059ac");
        props.setProperty("sm4-iv", "e166c3391294E69cc4c620f594fe00d7");
        props.setProperty("sm4-mode", "CBC");
        props.setProperty("sm4-padding", "PKCS7Padding");
        encryptAlgorithm = EncryptAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("SM4", props));
    }
    
    @Test
    public void assertEncrypt() {
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("dca2127b57ba8cac36a0914e0208dc11"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertEncryptWithoutKey() {
        Properties props = new Properties();
        props.setProperty("sm4-mode", "CBC");
        props.setProperty("sm4-iv", "e166c3391294E69cc4c620f594fe00d7");
        props.setProperty("sm4-padding", "PKCS7Padding");
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
        assertThat(encryptAlgorithm.encrypt("test", mock(EncryptContext.class)), is("028654f2ca4f575dee9e1faae85dadde"));
    }
    
    @Test
    public void assertEncryptWithNullPlaintext() {
        assertNull(encryptAlgorithm.encrypt(null, mock(EncryptContext.class)));
    }
    
    @Test
    public void assertDecrypt() {
        assertThat(encryptAlgorithm.decrypt("dca2127b57ba8cac36a0914e0208dc11", mock(EncryptContext.class)).toString(), is("test"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertDecryptWithoutKey() {
        Properties props = new Properties();
        props.setProperty("sm4-mode", "CBC");
        props.setProperty("sm4-iv", "e166c3391294E69cc4c620f594fe00d7");
        props.setProperty("sm4-padding", "PKCS7Padding");
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
        assertThat(encryptAlgorithm.decrypt("028654f2ca4f575dee9e1faae85dadde", mock(EncryptContext.class)).toString(), is("test"));
    }
    
    @Test
    public void assertDecryptWithNullCiphertext() {
        assertNull(encryptAlgorithm.decrypt(null, mock(EncryptContext.class)));
    }
}
