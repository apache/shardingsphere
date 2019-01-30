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

package org.apache.shardingsphere.core.yaml.sharding;

import org.apache.shardingsphere.api.config.EncryptorConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class YamlEncryptorConfigurationTest {
    
    private YamlEncryptorConfiguration yamlEncryptorConfiguration;
    
    @Before
    public void setUp() {
        yamlEncryptorConfiguration = new YamlEncryptorConfiguration();
        yamlEncryptorConfiguration.setType("test");
        yamlEncryptorConfiguration.setColumns("pwd1, pwd2");
        yamlEncryptorConfiguration.setAssistedQueryColumns("pwd1_index, pwd2_index");
    }
    
    @Test
    public void assertGetEncryptorConfiguration() {
        assertThat(yamlEncryptorConfiguration.getEncryptorConfiguration().getType(), is("test"));
    }
    
    @Test
    public void assertGetType() {
        assertThat(yamlEncryptorConfiguration.getType(), is("test"));
    }
    
    @Test
    public void assertGetColumns() {
        assertThat(yamlEncryptorConfiguration.getColumns(), is("pwd1, pwd2"));
    }
    
    @Test
    public void assertGetAssistedQueryColumns() {
        assertThat(yamlEncryptorConfiguration.getAssistedQueryColumns(), is("pwd1_index, pwd2_index"));
    }
    
    @Test
    public void assertGetProps() {
        assertNull(yamlEncryptorConfiguration.getProps().getProperty("test"));
    }
    
    @Test
    public void assertSetType() {
        yamlEncryptorConfiguration.setType("new_test");
        assertThat(yamlEncryptorConfiguration.getType(), is("new_test"));
    }
    
    @Test
    public void assertSetColumns() {
        yamlEncryptorConfiguration.setColumns("pwd1");
    }
    
    @Test
    public void assertSetAssistedQueryColumns() {
        yamlEncryptorConfiguration.setAssistedQueryColumns("pwd1_index");
        assertThat(yamlEncryptorConfiguration.getAssistedQueryColumns(), is("pwd1_index"));
    }
    
    @Test
    public void assertSetProps() {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        yamlEncryptorConfiguration.setProps(properties);
        assertThat(yamlEncryptorConfiguration.getProps().getProperty("key1"), is("value1"));
    }
    
    @Test
    public void assertNewYamlEncryptorConfiguration() {
        EncryptorConfiguration encryptorConfiguration = new EncryptorConfiguration();
        yamlEncryptorConfiguration = new YamlEncryptorConfiguration(encryptorConfiguration);
        assertNull(yamlEncryptorConfiguration.getType());
    }
}
