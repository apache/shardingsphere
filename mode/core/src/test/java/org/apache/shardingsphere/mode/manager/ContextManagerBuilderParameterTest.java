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

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContextManagerBuilderParameterTest {
    
    @Test
    void assertIsEmptyWithoutAllParameters() {
        assertTrue(new ContextManagerBuilderParameter(null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null, false).isEmpty());
    }
    
    @Test
    void assertIsEmptyWithDatabaseAndWithoutConfigurations() {
        assertTrue(new ContextManagerBuilderParameter(null, mockDatabaseConfigurations(true, true), Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null, false).isEmpty());
    }
    
    @Test
    void assertIsNotEmptyWhenGlobalRuleIsNotEmpty() {
        assertFalse(new ContextManagerBuilderParameter(null, Collections.emptyMap(), Collections.emptyMap(), Collections.singleton(mock(RuleConfiguration.class)),
                new Properties(), null, null, false).isEmpty());
    }
    
    @Test
    void assertIsNotEmptyWhenPropsIsNotEmpty() {
        assertFalse(new ContextManagerBuilderParameter(null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), PropertiesBuilder.build(new Property("foo", "foo_value")),
                null, null, false).isEmpty());
    }
    
    @Test
    void assertIsEmptyWhenDataSourceIsNotEmpty() {
        assertFalse(new ContextManagerBuilderParameter(null, mockDatabaseConfigurations(false, true), Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null, false).isEmpty());
    }
    
    @Test
    void assertIsEmptyWhenDatabaseRuleIsNotEmpty() {
        assertFalse(new ContextManagerBuilderParameter(null, mockDatabaseConfigurations(true, false), Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null, false).isEmpty());
    }
    
    private Map<String, DatabaseConfiguration> mockDatabaseConfigurations(final boolean isEmptyDataSources, final boolean isEmptyRuleConfigs) {
        DatabaseConfiguration databaseConfig = mock(DatabaseConfiguration.class, RETURNS_DEEP_STUBS);
        when(databaseConfig.getDataSources().isEmpty()).thenReturn(isEmptyDataSources);
        when(databaseConfig.getRuleConfigurations().isEmpty()).thenReturn(isEmptyRuleConfigs);
        return Collections.singletonMap("foo_ds", databaseConfig);
    }
    
    @Test
    void assertGetDefaultModeConfiguration() {
        ContextManagerBuilderParameter param = new ContextManagerBuilderParameter(null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null, false);
        assertThat(param.getModeConfiguration().getType(), is("Standalone"));
        assertNull(param.getModeConfiguration().getRepository());
    }
    
    @Test
    void assertGetModeConfiguration() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", mock(PersistRepositoryConfiguration.class));
        ContextManagerBuilderParameter param =
                new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), new Properties(), null, null, false);
        assertThat(param.getModeConfiguration().getType(), is("Cluster"));
        assertNotNull(param.getModeConfiguration().getRepository());
    }
}
