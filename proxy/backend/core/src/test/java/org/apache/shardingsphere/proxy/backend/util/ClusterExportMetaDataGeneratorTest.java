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

import org.apache.shardingsphere.globalclock.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedClusterInfo;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClusterExportMetaDataGeneratorTest {
    
    @Test
    void assertGenerateJsonFormatWithoutData() {
        ContextManager contextManager = mockContextManager(Collections.emptyList(), mockGlobalClockRule(false, null), Collections.singleton(mockDatabaseWithoutSources("empty_db")), new Properties());
        ExportedClusterInfo actual = JsonUtils.fromJsonString(new ClusterExportMetaDataGenerator(contextManager).generateJsonFormat(), ExportedClusterInfo.class);
        ExportedMetaData actualMetaData = actual.getMetaData();
        assertTrue(actualMetaData.getDatabases().isEmpty());
        assertThat(actualMetaData.getProps(), is(""));
        assertThat(actualMetaData.getRules(), is(""));
        assertNull(actual.getSnapshotInfo());
    }
    
    @Test
    void assertGenerateJsonFormatWithSnapshotAndData() {
        Properties props = new Properties();
        props.setProperty("sql-show", "true");
        GlobalClockProvider provider = mock(GlobalClockProvider.class);
        when(provider.getCurrentTimestamp()).thenReturn(123L);
        ContextManager contextManager = mockContextManager(Collections.singletonList(new GlobalClockRuleConfiguration("TSO", "LOCAL", true, new Properties())),
                mockGlobalClockRule(true, provider), Arrays.asList(mockDatabaseWithoutSources("skip_db"), mockDatabaseWithStorage()), props);
        ExportedClusterInfo actual = JsonUtils.fromJsonString(new ClusterExportMetaDataGenerator(contextManager).generateJsonFormat(), ExportedClusterInfo.class);
        ExportedMetaData metaData = actual.getMetaData();
        assertTrue(metaData.getDatabases().containsKey("logic_db"));
        assertThat(metaData.getDatabases().size(), is(1));
        assertThat(metaData.getProps(), containsString("props:"));
        assertThat(metaData.getRules(), containsString("rules:"));
        assertThat(actual.getSnapshotInfo(), is(notNullValue()));
        assertThat(actual.getSnapshotInfo().getCsn(), is("123"));
        assertNotNull(actual.getSnapshotInfo().getCreateTime());
    }
    
    @Test
    void assertGenerateJsonFormatWithSnapshotFallback() {
        ContextManager contextManager = mockContextManager(Collections.singletonList(new GlobalClockRuleConfiguration("TSO", "LOCAL", true, new Properties())),
                mockGlobalClockRule(true, null), Collections.singleton(mockDatabaseWithStorage()), new Properties());
        ExportedClusterInfo actual = JsonUtils.fromJsonString(new ClusterExportMetaDataGenerator(contextManager).generateJsonFormat(), ExportedClusterInfo.class);
        assertThat(actual.getSnapshotInfo().getCsn(), is("0"));
    }
    
    private ContextManager mockContextManager(final Collection<RuleConfiguration> globalRules, final GlobalClockRule globalClockRule,
                                              final Collection<ShardingSphereDatabase> databases, final Properties props) {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getAllDatabases()).thenReturn(databases);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        when(globalRuleMetaData.getConfigurations()).thenReturn(globalRules);
        when(globalRuleMetaData.getSingleRule(GlobalClockRule.class)).thenReturn(globalClockRule);
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(props));
        return result;
    }
    
    private GlobalClockRule mockGlobalClockRule(final boolean enabled, final GlobalClockProvider provider) {
        GlobalClockRule result = mock(GlobalClockRule.class);
        GlobalClockRuleConfiguration ruleConfig = mock(GlobalClockRuleConfiguration.class);
        when(ruleConfig.isEnabled()).thenReturn(enabled);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        when(result.getGlobalClockProvider()).thenReturn(Optional.ofNullable(provider));
        return result;
    }
    
    private ShardingSphereDatabase mockDatabaseWithoutSources(final String databaseName) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn(databaseName);
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private ShardingSphereDatabase mockDatabaseWithStorage() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("logic_db");
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("ds_0"));
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(mockStorageUnits(Collections.singletonMap("ds_0", createDataSourcePoolProperties())));
        when(result.getRuleMetaData().getConfigurations()).thenReturn(Collections.singletonList(new SingleRuleConfiguration()));
        return result;
    }
    
    private Map<String, StorageUnit> mockStorageUnits(final Map<String, DataSourcePoolProperties> props) {
        return props.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey, entry -> new StorageUnit(new StorageNode(entry.getKey()), entry.getValue(), mock(DataSource.class))));
    }
    
    private DataSourcePoolProperties createDataSourcePoolProperties() {
        Map<String, Object> props = new LinkedHashMap<>(3, 1F);
        props.put("url", "jdbc:h2:mem:ds_0");
        props.put("username", "sa");
        props.put("maxPoolSize", 30);
        return new DataSourcePoolProperties("HikariCP", props);
    }
}
