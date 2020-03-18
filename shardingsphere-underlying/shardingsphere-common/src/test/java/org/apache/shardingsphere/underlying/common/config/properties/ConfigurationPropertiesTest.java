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

package org.apache.shardingsphere.underlying.common.config.properties;

import org.apache.shardingsphere.underlying.common.config.exception.ShardingSphereConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConfigurationPropertiesTest {
    
    private final Properties prop = new Properties();
    
    private ConfigurationProperties configurationProperties;
    
    @Before
    public void setUp() {
        prop.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        prop.put(ConfigurationPropertyKey.EXECUTOR_SIZE.getKey(), "10");
        configurationProperties = new ConfigurationProperties(prop);
    }
    
    @Test
    public void assertGetValueForDefaultValue() {
        ConfigurationProperties properties = new ConfigurationProperties(new Properties());
        boolean actualSQLShow = properties.getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertThat(actualSQLShow, is(Boolean.valueOf(ConfigurationPropertyKey.SQL_SHOW.getDefaultValue())));
        int executorMaxSize = properties.getValue(ConfigurationPropertyKey.EXECUTOR_SIZE);
        assertThat(executorMaxSize, is(Integer.valueOf(ConfigurationPropertyKey.EXECUTOR_SIZE.getDefaultValue())));
    }
    
    @Test
    public void assertGetValueForBoolean() {
        boolean showSql = configurationProperties.getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSql);
    }
    
    @Test
    public void assertGetValueForInteger() {
        int actualExecutorMaxSize = configurationProperties.getValue(ConfigurationPropertyKey.EXECUTOR_SIZE);
        assertThat(actualExecutorMaxSize, is(10));
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertValidateFailure() {
        Properties prop = new Properties();
        prop.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), "error");
        prop.put(ConfigurationPropertyKey.EXECUTOR_SIZE.getKey(), "error");
        prop.put("other", "other");
        new ConfigurationProperties(prop);
    }
}
