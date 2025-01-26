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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.commons.codec.binary.Base64;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.distsql.statement.ral.queryable.export.ExportMetaDataStatement;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.rule.builder.DefaultGlobalClockRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.standalone.workerid.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedClusterInfo;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExportMetaDataExecutorTest {
    
    private static final String EXPECTED_EMPTY_METADATA_VALUE = "eyJtZXRhX2RhdGEiOnsiZGF0YWJhc2VzIjp7ImVtcHR5X21ldGFkYXRhIjoiZGF0YWJhc2VOYW1lOiBlbXB0eV9tZXRhZGF0YVxuI"
            + "n0sInByb3BzIjoiIiwicnVsZXMiOiJydWxlczpcbi0gIUdMT0JBTF9DTE9DS1xuICBlbmFibGVkOiBmYWxzZVxuICBwcm92aWRlcjogbG9jYWxcbiAgdHlwZTogVFNPXG4ifX0=";
    
    private static final String EXPECTED_NOT_EMPTY_METADATA_VALUE = "eyJtZXRhX2RhdGEiOnsiZGF0YWJhc2VzIjp7Im5vcm1hbF9kYiI6ImRhdGFiYXNlTmFtZTogbm9ybWFsX2RiXG5kYXRhU291cmNlczpcbiAgZHNfMDpcbiA"
            + "gICBwYXNzd29yZDogXG4gICAgdXJsOiBqZGJjOmgyOm1lbTpkZW1vX2RzXzA7REJfQ0xPU0VfREVMQVk9LTE7REFUQUJBU0VfVE9fVVBQRVI9ZmFsc2U7TU9ERT1NeVNRTFxuICAgIHVzZXJuYW1lOiByb290XG4gICAgbWluUG9"
            + "vbFNpemU6IDFcbiAgICBtYXhQb29sU2l6ZTogNTBcbiAgZHNfMTpcbiAgICBwYXNzd29yZDogXG4gICAgdXJsOiBqZGJjOmgyOm1lbTpkZW1vX2RzXzE7REJfQ0xPU0VfREVMQVk9LTE7REFUQUJBU0VfVE9fVVBQRVI9ZmFsc2"
            + "U7TU9ERT1NeVNRTFxuICAgIHVzZXJuYW1lOiByb290XG4gICAgbWluUG9vbFNpemU6IDFcbiAgICBtYXhQb29sU2l6ZTogNTBcbiJ9LCJwcm9wcyI6InByb3BzOlxuICBzcWwtc2hvdzogdHJ1ZVxuIiwicnVsZXMiOiJydWxlczpcbi0g"
            + "IUFVVEhPUklUWVxuICBwcml2aWxlZ2U6XG4gICAgdHlwZTogQUxMX1BFUk1JVFRFRFxuICB1c2VyczpcbiAgLSBhZG1pbjogdHJ1ZVxuICAgIGF1dGhlbnRpY2F0aW9uTWV0aG9kTmFtZTogJydcbiAgIC"
            + "BwYXNzd29yZDogcm9vdFxuICAgIHVzZXI6IHJvb3RAJVxuLSAhR0xPQkFMX0NMT0NLXG4gIGVuYWJsZWQ6IGZhbHNlXG4gIHByb3ZpZGVyOiBsb2NhbFxuICB0eXBlOiBUU09cbiJ9fQ==";
    
    @Test
    void assertExecuteWithEmptyMetaData() {
        ContextManager contextManager = mockEmptyContextManager();
        ExportMetaDataStatement sqlStatement = new ExportMetaDataStatement(null);
        Collection<LocalDataQueryResultRow> actual = new ExportMetaDataExecutor().getRows(sqlStatement, contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertMetaData(row.getCell(3), EXPECTED_EMPTY_METADATA_VALUE);
    }
    
    private ContextManager mockEmptyContextManager() {
        ShardingSphereDatabase database = mockEmptyShardingSphereDatabase();
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.singleton(new GlobalClockRule(new DefaultGlobalClockRuleConfigurationBuilder().build()))),
                new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private ShardingSphereDatabase mockEmptyShardingSphereDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getName()).thenReturn("empty_metadata");
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singleton("empty_metadata"));
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        when(result.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return result;
    }
    
    @Test
    void assertExecute() {
        ContextManager contextManager = mockContextManager();
        Collection<LocalDataQueryResultRow> actual = new ExportMetaDataExecutor().getRows(new ExportMetaDataStatement(null), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertMetaData(row.getCell(3), EXPECTED_NOT_EMPTY_METADATA_VALUE);
    }
    
    private ContextManager mockContextManager() {
        ShardingSphereDatabase database = mockShardingSphereDatabase();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database),
                new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Arrays.asList(new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build()),
                        new GlobalClockRule(new DefaultGlobalClockRuleConfigurationBuilder().build()))),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true"))));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        ComputeNodeInstanceContext computeNodeInstanceContext = new ComputeNodeInstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), new EventBusContext());
        computeNodeInstanceContext.init(new StandaloneWorkerIdGenerator(), mock(LockContext.class));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getComputeNodeInstanceContext()).thenReturn(computeNodeInstanceContext);
        return result;
    }
    
    private ShardingSphereDatabase mockShardingSphereDatabase() {
        Map<String, StorageUnit> storageUnits = createStorageUnits();
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getName()).thenReturn("normal_db");
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(storageUnits.keySet());
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private Map<String, StorageUnit> createStorageUnits() {
        Map<String, DataSourcePoolProperties> propsMap = createDataSourceMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Map<String, StorageUnit> result = new LinkedHashMap<>(propsMap.size(), 1F);
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
            when(storageUnit.getDataSourcePoolProperties()).thenReturn(entry.getValue());
            result.put(entry.getKey(), storageUnit);
        }
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("ds_0", createDataSource("demo_ds_0"));
        result.put("ds_1", createDataSource("demo_ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        MockedDataSource result = new MockedDataSource();
        result.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", name));
        result.setUsername("root");
        result.setPassword("");
        result.setMaxPoolSize(50);
        result.setMinPoolSize(1);
        return result;
    }
    
    private void assertMetaData(final Object actual, final String expected) {
        assertNotNull(actual);
        assertInstanceOf(String.class, actual);
        assertMetaData(convertToExportedClusterInfo((String) actual), convertToExportedClusterInfo(expected));
    }
    
    private void assertMetaData(final ExportedClusterInfo actual, final ExportedClusterInfo expected) {
        assertServerConfig(actual.getMetaData(), expected.getMetaData());
        assertDatabaseConfig(actual.getMetaData().getDatabases(), expected.getMetaData().getDatabases());
    }
    
    private void assertServerConfig(final ExportedMetaData actual, final ExportedMetaData expected) {
        if (null == expected) {
            assertNull(actual);
            return;
        }
        YamlProxyServerConfiguration actualServerConfig = convertToYamlProxyServerConfig(actual.getRules() + System.lineSeparator() + actual.getProps());
        YamlProxyServerConfiguration expectedServerConfig = convertToYamlProxyServerConfig(expected.getRules() + System.lineSeparator() + expected.getProps());
        if (null == expectedServerConfig) {
            assertNull(actualServerConfig);
            return;
        }
        assertRules(actualServerConfig.getRules(), expectedServerConfig.getRules());
        assertProps(actualServerConfig.getProps(), expectedServerConfig.getProps());
    }
    
    private void assertRules(final Collection<YamlRuleConfiguration> actual, final Collection<YamlRuleConfiguration> expected) {
        if (null == expected) {
            assertNull(actual);
            return;
        }
        assertThat(actual.size(), is(expected.size()));
        for (YamlRuleConfiguration each : expected) {
            assertTrue(actual.stream().anyMatch(rule -> rule.getRuleConfigurationType().equals(each.getRuleConfigurationType())));
        }
    }
    
    private void assertProps(final Properties actual, final Properties expected) {
        if (null == expected) {
            assertNull(actual);
            return;
        }
        assertThat(actual.size(), is(expected.size()));
        for (Entry<Object, Object> entry : expected.entrySet()) {
            assertThat(actual.get(entry.getKey()), is(entry.getValue()));
        }
    }
    
    private void assertDatabaseConfig(final Map<String, String> actual, final Map<String, String> expected) {
        assertThat(actual.size(), is(expected.size()));
        for (Entry<String, String> entry : expected.entrySet()) {
            assertDatabaseConfig(convertToYamlProxyDatabaseConfig(actual.get(entry.getKey())), convertToYamlProxyDatabaseConfig(entry.getValue()));
        }
    }
    
    private void assertDatabaseConfig(final YamlProxyDatabaseConfiguration actual, final YamlProxyDatabaseConfiguration expected) {
        assertThat(actual.getDatabaseName(), is(expected.getDatabaseName()));
        assertDataSources(actual.getDataSources(), expected.getDataSources());
        assertRules(actual.getRules(), expected.getRules());
    }
    
    private void assertDataSources(final Map<String, YamlProxyDataSourceConfiguration> actual, final Map<String, YamlProxyDataSourceConfiguration> expected) {
        if (null == expected) {
            assertNull(actual);
            return;
        }
        assertThat(actual.size(), is(expected.size()));
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : expected.entrySet()) {
            YamlProxyDataSourceConfiguration actualDataSourceConfig = actual.get(entry.getKey());
            YamlProxyDataSourceConfiguration exceptedDataSourceConfig = entry.getValue();
            assertThat(actualDataSourceConfig.getDataSourceClassName(), is(exceptedDataSourceConfig.getDataSourceClassName()));
            assertThat(actualDataSourceConfig.getUrl(), is(exceptedDataSourceConfig.getUrl()));
            assertThat(actualDataSourceConfig.getUsername(), is(exceptedDataSourceConfig.getUsername()));
            assertThat(actualDataSourceConfig.getPassword(), is(exceptedDataSourceConfig.getPassword()));
            assertThat(actualDataSourceConfig.getConnectionTimeoutMilliseconds(), is(exceptedDataSourceConfig.getConnectionTimeoutMilliseconds()));
            assertThat(actualDataSourceConfig.getIdleTimeoutMilliseconds(), is(exceptedDataSourceConfig.getIdleTimeoutMilliseconds()));
            assertThat(actualDataSourceConfig.getMaxLifetimeMilliseconds(), is(exceptedDataSourceConfig.getMaxLifetimeMilliseconds()));
            assertThat(actualDataSourceConfig.getMaxPoolSize(), is(exceptedDataSourceConfig.getMaxPoolSize()));
            assertThat(actualDataSourceConfig.getMinPoolSize(), is(exceptedDataSourceConfig.getMinPoolSize()));
            assertThat(actualDataSourceConfig.getReadOnly(), is(exceptedDataSourceConfig.getReadOnly()));
            assertProps(actualDataSourceConfig.getCustomPoolProps(), exceptedDataSourceConfig.getCustomPoolProps());
        }
    }
    
    private ExportedClusterInfo convertToExportedClusterInfo(final String base64String) {
        return JsonUtils.fromJsonString(new String(Base64.decodeBase64(base64String)), ExportedClusterInfo.class);
    }
    
    private YamlProxyServerConfiguration convertToYamlProxyServerConfig(final String serverConfig) {
        return YamlEngine.unmarshal(serverConfig, YamlProxyServerConfiguration.class);
    }
    
    private YamlProxyDatabaseConfiguration convertToYamlProxyDatabaseConfig(final String databaseConfig) {
        return YamlEngine.unmarshal(databaseConfig, YamlProxyDatabaseConfiguration.class);
    }
}
