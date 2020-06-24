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

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class AESEncryptAlgorithmTest {
    
    private final AESEncryptAlgorithm encryptAlgorithm = new AESEncryptAlgorithm();
    
    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("aes.key.value", "test");
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
    }
    
    @Test
    public void assertGetType() {
        assertThat(encryptAlgorithm.getType(), is("AES"));
    }
    
    @Test
    public void assertEncode() {
        assertThat(encryptAlgorithm.encrypt("test"), is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertEncodeWithoutKey() {
        Properties props = new Properties();
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
        assertThat(encryptAlgorithm.encrypt("test"), is("dSpPiyENQGDUXMKFMJPGWA=="));
    }
    
    @Test
    public void assertDecode() {
        assertThat(encryptAlgorithm.decrypt("dSpPiyENQGDUXMKFMJPGWA==").toString(), is("test"));
    }
    
    @Test
    public void assertDecodeWithNull() {
        assertNull(encryptAlgorithm.decrypt(null));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertDecodeWithoutKey() {
        Properties props = new Properties();
        encryptAlgorithm.setProps(props);
        encryptAlgorithm.init();
        assertThat(encryptAlgorithm.decrypt("dSpPiyENQGDUXMKFMJPGWA==").toString(), is("test"));
    }
    
    @Test
    public void assertGetProperties() {
        assertThat(encryptAlgorithm.getProps().get("aes.key.value").toString(), is("test"));
    }
}
