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

package org.apache.shardingsphere.api.config;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class EncryptorConfigurationTest {
    
    private EncryptorConfiguration encryptorConfiguration;
    
    @Before
    public void setUp() {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        encryptorConfiguration = new EncryptorConfiguration("test", "pwd1", "pwd1_index", properties);
    }
    
    @Test
    public void assertGetShardingEncryptorStrategy() {
        assertThat(encryptorConfiguration.getShardingEncryptorStrategy().getColumns().get(0), is("pwd1"));
    }
    
    @Test
    public void assertGetShardingEncryptorStrategyWithNull() {
        encryptorConfiguration = new EncryptorConfiguration("", "pwd1", "pwd1_index", new Properties());
        assertNull(encryptorConfiguration.getShardingEncryptorStrategy());
    }
    
    @Test
    public void assertGetType() {
    }
    
    @Test
    public void testGetColumns() {
    }
    
    @Test
    public void testGetAssistedQueryColumns() {
    }
    
    @Test
    public void testGetProps() {
    }
    
    @Test
    public void testSetType() {
    }
    
    @Test
    public void testSetColumns() {
    }
    
    @Test
    public void testSetAssistedQueryColumns() {
    }
    
    @Test
    public void testSetProps() {
    }
}
