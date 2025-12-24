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

package org.apache.shardingsphere.proxy.backend.util;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseExportMetaDataGeneratorTest {
    
    @Test
    void assertGenerateYAMLFormatWithoutDataSourcesOrRules() {
        assertThat(new DatabaseExportMetaDataGenerator(mockEmptyDatabase()).generateYAMLFormat(), is("databaseName: empty_db" + System.lineSeparator()));
    }
    
    private ShardingSphereDatabase mockEmptyDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("empty_db");
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.emptyList());
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    @Test
    void assertGenerateYAMLFormatWithDataSourcesAndRules() {
        String actual = new DatabaseExportMetaDataGenerator(mockDatabase()).generateYAMLFormat();
        assertThat(actual, containsString("databaseName: logic_db"));
        assertThat(actual, containsString("dataSources:" + System.lineSeparator() + "  ds_0:"));
        assertThat(actual, containsString("url: jdbc:h2:mem:ds_0"));
        assertThat(actual, containsString("username: sa"));
        assertThat(actual, containsString("maxPoolSize: 30"));
        assertThat(actual, not(containsString("idleTimeout")));
        assertThat(actual, containsString("rules:"));
        assertThat(actual, containsString("defaultDataSource: write_ds"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        Map<String, StorageUnit> storageUnits = mockStorageUnits(Collections.singletonMap("ds_0", createDataSourcePoolProperties()));
        when(resourceMetaData.getStorageUnits()).thenReturn(storageUnits);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("logic_db");
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        Collection<RuleConfiguration> ruleConfigs = Arrays.asList(new SingleRuleConfiguration(), createSingleRuleConfiguration());
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(ruleMetaData.getConfigurations()).thenReturn(ruleConfigs);
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private Map<String, StorageUnit> mockStorageUnits(final Map<String, DataSourcePoolProperties> props) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(props.size(), 1F);
        for (Map.Entry<String, DataSourcePoolProperties> entry : props.entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class);
            when(storageUnit.getDataSourcePoolProperties()).thenReturn(entry.getValue());
            result.put(entry.getKey(), storageUnit);
        }
        return result;
    }
    
    private DataSourcePoolProperties createDataSourcePoolProperties() {
        ConnectionPropertySynonyms connectionSynonyms = mock(ConnectionPropertySynonyms.class);
        Map<String, Object> connectionProps = new LinkedHashMap<>(2, 1F);
        connectionProps.put("url", "jdbc:h2:mem:ds_0");
        connectionProps.put("username", "sa");
        connectionProps.put("password", "pwd");
        when(connectionSynonyms.getStandardProperties()).thenReturn(connectionProps);
        PoolPropertySynonyms poolSynonyms = mock(PoolPropertySynonyms.class);
        Map<String, Object> poolProps = new LinkedHashMap<>(2, 1F);
        poolProps.put("maxPoolSize", 30);
        poolProps.put("idleTimeout", null);
        when(poolSynonyms.getStandardProperties()).thenReturn(poolProps);
        DataSourcePoolProperties result = mock(DataSourcePoolProperties.class);
        when(result.getConnectionPropertySynonyms()).thenReturn(connectionSynonyms);
        when(result.getPoolPropertySynonyms()).thenReturn(poolSynonyms);
        return result;
    }
    
    private SingleRuleConfiguration createSingleRuleConfiguration() {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        result.setDefaultDataSource("write_ds");
        return result;
    }
}
