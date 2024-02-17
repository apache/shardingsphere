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
import org.apache.shardingsphere.globalclock.core.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.core.rule.builder.DefaultGlobalClockRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedClusterInfo;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ExportMetaDataExecutorTest {
    
    private static final String EXPECTED_EMPTY_METADATA_VALUE = "eyJtZXRhX2RhdGEiOnsiZGF0YWJhc2VzIjp7ImVtcHR5X21ldGFkYXRhIjoiZGF0YWJhc2VOYW1lOiBudWxsXG5kYXRhU291cmNlczpcbn"
            + "J1bGVzOlxuIn0sInByb3BzIjoiIiwicnVsZXMiOiJydWxlczpcbi0gIUdMT0JBTF9DTE9DS1xuICBlbmFibGVkOiBmYWxzZVxuICBwcm92aWRlcjogbG9jYWxcbiAgdHlwZTogVFNPXG4ifX0=";
    
    private static final String EXPECTED_NOT_EMPTY_METADATA_VALUE = "eyJtZXRhX2RhdGEiOnsiZGF0YWJhc2VzIjp7Im5vcm1hbF9kYiI6ImRhdGFiYXNlTmFtZTogbm9ybWFsX2RiXG5kYXRhU291cm"
            + "NlczpcbiAgZHNfMDpcbiAgICBwYXNzd29yZDogXG4gICAgdXJsOiBqZGJjOm9wZW5nYXVzczovLzEyNy4wLjAuMTo1NDMyL2RlbW9fZHNfMFxuICAgIHVzZXJuYW1lOiByb290XG4gICAgbWluUG9vb"
            + "FNpemU6IDFcbiAgICBtYXhQb29sU2l6ZTogNTBcbiAgZHNfMTpcbiAgICBwYXNzd29yZDogXG4gICAgdXJsOiBqZGJjOm9wZW5nYXVzczovLzEyNy4wLjAuMTo1NDMyL2RlbW9fZHNfMVxuICAgIHVzZ"
            + "XJuYW1lOiByb290XG4gICAgbWluUG9vbFNpemU6IDFcbiAgICBtYXhQb29sU2l6ZTogNTBcbiJ9LCJwcm9wcyI6InByb3BzOlxuICBzcWwtc2hvdzogdHJ1ZVxuIiwicnVsZXMiOiJydWxlczpcbi0g"
            + "IUFVVEhPUklUWVxuICBwcml2aWxlZ2U6XG4gICAgdHlwZTogQUxMX1BFUk1JVFRFRFxuICB1c2VyczpcbiAgLSBhdXRoZW50aWNhdGlvbk1ldGhvZE5hbWU6ICcnXG4gICAgcGFzc3dvcmQ6IHJvb3Rc"
            + "biAgICB1c2VyOiByb290QCVcbi0gIUdMT0JBTF9DTE9DS1xuICBlbmFibGVkOiBmYWxzZVxuICBwcm92aWRlcjogbG9jYWxcbiAgdHlwZTogVFNPXG4ifX0=";
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    }
    
    @Test
    void assertExecuteWithEmptyMetaData() {
        ContextManager contextManager = mockEmptyContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_metadata"));
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singleton("empty_metadata"));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        ExportMetaDataStatement sqlStatement = new ExportMetaDataStatement(null);
        Collection<LocalDataQueryResultRow> actual = new ExportMetaDataExecutor().getRows(sqlStatement, contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertMetaData(row.getCell(3), EXPECTED_EMPTY_METADATA_VALUE);
    }
    
    private ContextManager mockEmptyContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(new HashMap<>(),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.singletonList(
                        new GlobalClockRule(new DefaultGlobalClockRuleConfigurationBuilder().build(), Collections.emptyMap()))),
                new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    @Test
    void assertExecute() {
        when(database.getName()).thenReturn("normal_db");
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singleton("empty_metadata"));
        Map<String, StorageUnit> storageUnits = createStorageUnits();
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("normal_db"));
        Collection<LocalDataQueryResultRow> actual = new ExportMetaDataExecutor().getRows(new ExportMetaDataStatement(null), contextManager);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertMetaData(row.getCell(3), EXPECTED_NOT_EMPTY_METADATA_VALUE);
    }
    
    private Map<String, StorageUnit> createStorageUnits() {
        Map<String, DataSourcePoolProperties> propsMap = createDataSourceMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Map<String, StorageUnit> result = new LinkedHashMap<>();
        for (Entry<String, DataSourcePoolProperties> entry : propsMap.entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
            when(storageUnit.getDataSourcePoolProperties()).thenReturn(entry.getValue());
            result.put(entry.getKey(), storageUnit);
        }
        return result;
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(Collections.singletonMap(database.getName(), database),
                new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Arrays.asList(new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build()),
                        new GlobalClockRule(new DefaultGlobalClockRuleConfigurationBuilder().build(), Collections.singletonMap(database.getName(), database)))),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true")))));
        InstanceContext instanceContext = new InstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new StandaloneWorkerIdGenerator(), new ModeConfiguration("Standalone", null),
                mock(ModeContextManager.class), mock(LockContext.class), new EventBusContext());
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getInstanceContext()).thenReturn(instanceContext);
        when(result.getDatabase("normal_db")).thenReturn(database);
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
        result.setUrl(String.format("jdbc:opengauss://127.0.0.1:5432/%s", name));
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
