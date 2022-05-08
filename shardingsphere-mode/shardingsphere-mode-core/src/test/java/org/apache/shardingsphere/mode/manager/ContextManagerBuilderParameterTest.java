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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

public final class ContextManagerBuilderParameterTest {
    
    @Test
    public void assertIsEmpty() {
        Map<String, DatabaseConfiguration> mockDatabaseConfigs = mockSchemaConfigurations(true, true);
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.emptyList())
                .props(new Properties())
                .databaseConfigs(mockDatabaseConfigs)
                .build();
        assertTrue(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenAllFieldIsEmpty() {
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.emptyList())
                .props(new Properties())
                .databaseConfigs(Collections.emptyMap())
                .build();
        assertTrue(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenAllFieldIsNotEmpty() {
        Map<String, DatabaseConfiguration> mockDatabaseConfigs = mockSchemaConfigurations(false, false);
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.emptyList())
                .props(new Properties())
                .databaseConfigs(mockDatabaseConfigs)
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenPropsIsNotEmpty() {
        Properties props = createProperties();
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.emptyList())
                .props(props)
                .databaseConfigs(Collections.emptyMap())
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenGlobalRuleConfigsIsNotEmpty() {
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.singleton(mock(RuleConfiguration.class)))
                .props(new Properties())
                .databaseConfigs(Collections.emptyMap())
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenSchemaConfigsIsNotEmpty() {
        Map<String, DatabaseConfiguration> mockDatabaseConfigs = mockSchemaConfigurations(false, false);
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.emptyList())
                .props(new Properties())
                .databaseConfigs(mockDatabaseConfigs)
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenDataSourcesIsNotEmpty() {
        Map<String, DatabaseConfiguration> mockDatabaseConfigs = mockSchemaConfigurations(false, true);
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.emptyList())
                .props(new Properties())
                .databaseConfigs(mockDatabaseConfigs)
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenRuleConfigurationsIsNotEmpty() {
        Map<String, DatabaseConfiguration> databaseConfigs = mockSchemaConfigurations(true, false);
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.emptyList())
                .props(new Properties())
                .databaseConfigs(databaseConfigs)
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenOnlyPropsIsEmpty() {
        Map<String, DatabaseConfiguration> mockDatabaseConfigs = mockSchemaConfigurations(true, true);
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.singleton(mock(RuleConfiguration.class)))
                .props(new Properties())
                .databaseConfigs(mockDatabaseConfigs)
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenOnlyGlobalRuleConfigsIsEmpty() {
        Map<String, DatabaseConfiguration> mockDatabaseConfigs = mockSchemaConfigurations(true, true);
        Properties props = createProperties();
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.emptyList())
                .props(props)
                .databaseConfigs(mockDatabaseConfigs)
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    @Test
    public void assertIsEmptyWhenOnlySchemaConfigsIsEmpty() {
        Properties props = createProperties();
        ContextManagerBuilderParameter contextManagerBuilderParameter = ContextManagerBuilderParameter.builder()
                .globalRuleConfigs(Collections.singleton(mock(RuleConfiguration.class)))
                .props(props)
                .databaseConfigs(Collections.emptyMap())
                .build();
        assertFalse(contextManagerBuilderParameter.isEmpty());
    }
    
    private Map<String, DatabaseConfiguration> mockSchemaConfigurations(final boolean dataSourcesIsEmpty, final boolean ruleConfigurationsIsEmpty) {
        DatabaseConfiguration result = mock(DatabaseConfiguration.class, RETURNS_DEEP_STUBS);
        lenient().when(result.getDataSources().isEmpty()).thenReturn(dataSourcesIsEmpty);
        lenient().when(result.getRuleConfigurations().isEmpty()).thenReturn(ruleConfigurationsIsEmpty);
        return Collections.singletonMap("logic", result);
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("foo", "foo_value");
        return result;
    }
}
