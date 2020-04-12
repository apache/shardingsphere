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

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MD5EncryptorTest {
    
    private final MD5Encryptor encryptor = new MD5Encryptor();
    
    @Test
    public void assertGetType() {
        assertThat(encryptor.getType(), is("MD5"));
    }
    
    @Test
    public void assertEncode() {
        assertThat(encryptor.encrypt("test"), is("098f6bcd4621d373cade4e832627b4f6"));
    }
    
    @Test
    public void assertDecode() {
        assertThat(encryptor.decrypt("test").toString(), is("test"));
    }
    
    @Test
    public void assertProperties() {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        encryptor.setProperties(properties);
        assertThat(encryptor.getProperties().get("key1").toString(), is("value1"));
    }
}
