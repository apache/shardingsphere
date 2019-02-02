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

package org.apache.shardingsphere.core.yaml.swapper.impl;

import org.apache.shardingsphere.api.config.encryptor.EncryptorConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlEncryptorConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class EncryptorConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        YamlEncryptorConfiguration actual = new EncryptorConfigurationYamlSwapper().swap(new EncryptorConfiguration("MD5", "pwd", "pwd_query", new Properties()));
        assertThat(actual.getType(), is("MD5"));
        assertThat(actual.getColumns(), is("pwd"));
        assertThat(actual.getAssistedQueryColumns(), is("pwd_query"));
        assertThat(actual.getProps(), is(new Properties()));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlEncryptorConfiguration yamlConfiguration = new YamlEncryptorConfiguration();
        yamlConfiguration.setType("MD5");
        yamlConfiguration.setColumns("pwd");
        yamlConfiguration.setAssistedQueryColumns("pwd_query");
        EncryptorConfiguration actual = new EncryptorConfigurationYamlSwapper().swap(yamlConfiguration);
        assertThat(actual.getType(), is("MD5"));
        assertThat(actual.getColumns(), is("pwd"));
        assertThat(actual.getAssistedQueryColumns(), is("pwd_query"));
        assertThat(actual.getProps(), is(new Properties()));
    }
}
