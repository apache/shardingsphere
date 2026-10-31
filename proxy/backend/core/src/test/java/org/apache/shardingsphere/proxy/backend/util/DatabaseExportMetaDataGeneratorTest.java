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

import org.apache.shardingsphere.distsql.handler.executor.configuration.export.DatabaseExportMetaDataGenerator;
import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.YamlDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertThat(YamlEngine.unmarshal(actual, YamlDatabaseConfiguration.class).getRules().size(), is(1));
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
        for (Entry<String, DataSourcePoolProperties> entry : props.entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class);
            when(storageUnit.getDataSourcePoolProperties()).thenReturn(entry.getValue());
            result.put(entry.getKey(), storageUnit);
        }
        return result;
    }
    
    private DataSourcePoolProperties createDataSourcePoolProperties() {
        Map<String, Object> connectionProps = new LinkedHashMap<>(2, 1F);
        connectionProps.put("url", "jdbc:h2:mem:ds_0");
        connectionProps.put("username", "sa");
        connectionProps.put("password", "pwd");
        ConnectionPropertySynonyms connectionSynonyms = mock(ConnectionPropertySynonyms.class);
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
    
    @ParameterizedTest(name = "{index}: {0}")
    @ValueSource(strings = {"foo: bar # baz\n'quoted' \"value\" \\中", "null", "true", "00123"})
    void assertGenerateYAMLFormatWithSpecialCharacters(final String value) {
        ShardingSphereDatabase database = mockEmptyDatabase();
        when(database.getName()).thenReturn(value);
        Map<String, Object> expectedProps = new LinkedHashMap<>(5, 1F);
        expectedProps.put("url", value);
        expectedProps.put("username", value);
        expectedProps.put("password", value);
        expectedProps.put("maxPoolSize", 30);
        expectedProps.put("readOnly", true);
        Map<String, Object> props = new LinkedHashMap<>(expectedProps);
        props.put("minPoolSize", null);
        props.put("connectionInitSql", "SELECT 1");
        Map<String, StorageUnit> storageUnits = mockStorageUnits(Collections.singletonMap(value, new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", props)));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        Map<?, ?> actual = YamlEngine.unmarshal(new DatabaseExportMetaDataGenerator(database).generateYAMLFormat(), Map.class);
        assertThat(actual.get("databaseName"), is(value));
        Map<?, ?> actualDataSources = (Map<?, ?>) actual.get("dataSources");
        assertThat(actualDataSources.keySet(), contains(value));
        assertThat(actualDataSources.get(value), is(expectedProps));
    }
    
    @Test
    void assertGenerateYAMLFormatWithDataSourceOrderAndPoolTypes() {
        ShardingSphereDatabase database = mockEmptyDatabase();
        Map<String, Object> props = new LinkedHashMap<>(4, 1F);
        props.put("password", "");
        props.put("maxPoolSize", 30);
        props.put("connectionTimeoutMilliseconds", 30000L);
        props.put("readOnly", false);
        Map<String, DataSourcePoolProperties> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("foo_ds", new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", props));
        dataSources.put("bar_ds", createDataSourcePoolProperties());
        Map<String, StorageUnit> storageUnits = mockStorageUnits(dataSources);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        String actualYaml = new DatabaseExportMetaDataGenerator(database).generateYAMLFormat();
        Map<?, ?> actual = YamlEngine.unmarshal(actualYaml, Map.class);
        assertThat(((Map<?, ?>) actual.get("dataSources")).keySet(), contains("foo_ds", "bar_ds"));
        YamlDatabaseConfiguration actualConfig = YamlEngine.unmarshal(actualYaml, YamlDatabaseConfiguration.class);
        assertThat(actualConfig.getDataSources().get("foo_ds").getPassword(), is(""));
        assertThat(actualConfig.getDataSources().get("foo_ds").getMaxPoolSize(), is(30));
        assertThat(actualConfig.getDataSources().get("foo_ds").getConnectionTimeoutMilliseconds(), is(30000L));
        assertFalse(actualConfig.getDataSources().get("foo_ds").getReadOnly());
    }
    
    @Test
    void assertGenerateYAMLFormatWithEmptyDataSource() {
        ShardingSphereDatabase database = mockEmptyDatabase();
        Map<String, Object> props = new LinkedHashMap<>(1, 1F);
        props.put("url", null);
        Map<String, StorageUnit> storageUnits = mockStorageUnits(Collections.singletonMap("foo_ds", new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", props)));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        Map<?, ?> actual = YamlEngine.unmarshal(new DatabaseExportMetaDataGenerator(database).generateYAMLFormat(), Map.class);
        Map<?, ?> actualDataSources = (Map<?, ?>) actual.get("dataSources");
        assertThat(actualDataSources.keySet(), contains("foo_ds"));
        assertNull(actualDataSources.get("foo_ds"));
    }
    
    @Test
    void assertGenerateYAMLFormatWithOnlyEmptyRules() {
        ShardingSphereDatabase database = mockEmptyDatabase();
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singletonList(new SingleRuleConfiguration()));
        Map<?, ?> actual = YamlEngine.unmarshal(new DatabaseExportMetaDataGenerator(database).generateYAMLFormat(), Map.class);
        assertThat(actual.keySet(), contains("databaseName"));
    }
}
