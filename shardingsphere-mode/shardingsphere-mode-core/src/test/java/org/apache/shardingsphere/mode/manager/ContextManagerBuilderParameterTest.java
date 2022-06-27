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

package org.apache.shardingsphere.mode.manager;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ContextManagerBuilderParameterTest {
    
    @Test
    public void assertIsEmptyWithoutAllParameters() {
        assertTrue(new ContextManagerBuilderParameter(null, Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null).isEmpty());
    }
    
    @Test
    public void assertIsEmptyWithDatabaseAndWithoutConfigurations() {
        assertTrue(new ContextManagerBuilderParameter(null, mockDatabaseConfigurations(true, true), Collections.emptyList(), new Properties(), null, null).isEmpty());
    }
    
    @Test
    public void assertIsNotEmptyWhenGlobalRuleIsNotEmpty() {
        assertFalse(new ContextManagerBuilderParameter(null, Collections.emptyMap(), Collections.singleton(mock(RuleConfiguration.class)), new Properties(), null, null).isEmpty());
    }
    
    @Test
    public void assertIsNotEmptyWhenPropsIsNotEmpty() {
        assertFalse(new ContextManagerBuilderParameter(null, Collections.emptyMap(), Collections.emptyList(), createProperties(), null, null).isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenDataSourceIsNotEmpty() {
        assertFalse(new ContextManagerBuilderParameter(null, mockDatabaseConfigurations(false, true), Collections.emptyList(), new Properties(), null, null).isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenDatabaseRuleIsNotEmpty() {
        assertFalse(new ContextManagerBuilderParameter(null, mockDatabaseConfigurations(true, false), Collections.emptyList(), new Properties(), null, null).isEmpty());
    }
    
    private Map<String, DatabaseConfiguration> mockDatabaseConfigurations(final boolean isEmptyDataSources, final boolean isEmptyRuleConfigs) {
        DatabaseConfiguration databaseConfig = mock(DatabaseConfiguration.class, RETURNS_DEEP_STUBS);
        when(databaseConfig.getDataSources().isEmpty()).thenReturn(isEmptyDataSources);
        when(databaseConfig.getRuleConfigurations().isEmpty()).thenReturn(isEmptyRuleConfigs);
        return Collections.singletonMap("foo_ds", databaseConfig);
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("foo", "foo_value");
        return result;
    }
}
