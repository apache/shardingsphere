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

package org.apache.shardingsphere.encrypt.strategy.impl;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class RC4EncryptorTest {
    
    private final RC4Encryptor encryptor = new RC4Encryptor();
    
    @Before
    public void setUp() {
        Properties properties = new Properties();
        properties.setProperty("rc4.key.value", "test-sharding");
        encryptor.setProperties(properties);
        encryptor.init();
    }
    
    @Test
    public void assertGetType() {
        assertThat(encryptor.getType(), is("RC4"));
    }
    
    @Test
    public void assertEncode() {
        assertThat(encryptor.encrypt("test"), is("qn36NQ=="));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertKeyIsToLong() {
        Properties properties = new Properties();
        StringBuilder keyBuffer = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            keyBuffer.append("test");
        }
        properties.setProperty("rc4.key.value", keyBuffer.toString());
        encryptor.setProperties(properties);
        encryptor.init();
    }
    
    @Test
    public void assertDecode() {
        assertThat(encryptor.decrypt("qn36NQ==").toString(), is("test"));
    }
    
    @Test
    public void assertDecodeWithNull() {
        assertNull(encryptor.decrypt(null));
    }
    
    @Test
    public void assertGetProperties() {
        assertThat(encryptor.getProperties().get("rc4.key.value").toString(), is("test-sharding"));
    }
}
