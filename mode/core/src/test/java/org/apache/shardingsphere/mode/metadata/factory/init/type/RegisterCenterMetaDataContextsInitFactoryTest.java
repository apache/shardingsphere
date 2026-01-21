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

package org.apache.shardingsphere.mode.metadata.factory.init.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.config.global.PropertiesPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ShardingSphereDatabasesFactory.class, GlobalRulesBuilder.class, ShardingSphereStatisticsFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterCenterMetaDataContextsInitFactoryTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    void assertCreateWithPersistedSchemas() throws SQLException {
        when(GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.emptyList());
        when(ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(new ShardingSphereStatistics());
        ComputeNodeInstanceContext instanceContext = mockComputeNodeInstanceContext(mock(ProxyInstanceMetaData.class));
        when(ShardingSphereDatabasesFactory.create(anyMap(), anyMap(), any(ConfigurationProperties.class), eq(instanceContext), any(DatabaseType.class))).thenReturn(
                Arrays.asList(createDatabase("with_units", Collections.emptyList()), createDatabase("without_units", Collections.emptyList())));
        Map<String, DatabaseConfiguration> databaseConfigs = createDatabaseConfigsWithAndWithoutStorageUnits();
        Collection<String> databaseNames = Arrays.asList("with_units", "without_units", "missing_config");
        try (
                MockedConstruction<PropertiesPersistService> ignoredService = mockConstruction(PropertiesPersistService.class, (mock, context) -> when(mock.load()).thenReturn(new Properties()));
                MockedConstruction<MetaDataPersistFacade> ignoredFacade = mockConstruction(MetaDataPersistFacade.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                        (mock, context) -> {
                            when(mock.getPropsService().load()).thenReturn(new Properties());
                            when(mock.loadDataSourceConfigurations(anyString())).thenReturn(Collections.emptyMap());
                            when(mock.getDatabaseMetaDataFacade().getDatabase().loadAllDatabaseNames()).thenReturn(databaseNames);
                            when(mock.getStatisticsService().load(any())).thenReturn(new ShardingSphereStatistics());
                        });
                MockedConstruction<DataSourcePoolDestroyer> destroyerMocked = mockConstruction(DataSourcePoolDestroyer.class)) {
            MetaDataContexts actual = new RegisterCenterMetaDataContextsInitFactory(repository, instanceContext).create(createContextManagerBuilderParameter(databaseConfigs));
            assertThat(actual.getMetaData().getAllDatabases(), hasSize(2));
            assertThat(destroyerMocked.constructed(), hasSize(1));
            verify(destroyerMocked.constructed().get(0)).asyncDestroy();
        }
    }
    
    @Test
    void assertCreateMergesViewsWhenSchemasNotPersisted() throws SQLException {
        when(GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.emptyList());
        when(ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(new ShardingSphereStatistics());
        ShardingSphereDatabase fooDatabase = createDatabase("foo_db",
                Collections.singleton(new ShardingSphereSchema("foo_schema", Collections.emptyList(), Collections.singleton(new ShardingSphereView("local_view", "select 1")))));
        ShardingSphereDatabase barDatabase = createDatabase("bar_db", Collections.emptyList());
        ComputeNodeInstanceContext instanceContext = mockComputeNodeInstanceContext(mock(JDBCInstanceMetaData.class));
        when(ShardingSphereDatabasesFactory.create(anyMap(), any(ConfigurationProperties.class), eq(instanceContext), any(DatabaseType.class))).thenReturn(Arrays.asList(fooDatabase, barDatabase));
        Map<String, DatabaseConfiguration> databaseConfigs = createDatabaseConfigsWithoutStorageUnits();
        Properties props = PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED.getKey(), Boolean.FALSE.toString()));
        Collection<ShardingSphereSchema> persistedSchemas = Arrays.asList(
                new ShardingSphereSchema("foo_schema", Collections.emptyList(), Collections.singleton(new ShardingSphereView("persisted_view", "select 2"))),
                new ShardingSphereSchema("missing_schema", Collections.emptyList(), Collections.singleton(new ShardingSphereView("ignored_view", "select 3"))));
        try (
                MockedConstruction<PropertiesPersistService> ignoredService = mockConstruction(PropertiesPersistService.class, (mock, context) -> when(mock.load()).thenReturn(props));
                MockedConstruction<MetaDataPersistFacade> ignoredFacade = mockConstruction(MetaDataPersistFacade.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                        (mock, context) -> {
                            when(mock.getPropsService().load()).thenReturn(new Properties());
                            when(mock.loadDataSourceConfigurations(anyString())).thenReturn(Collections.emptyMap());
                            when(mock.getDatabaseMetaDataFacade().getSchema().load("foo_db")).thenReturn(persistedSchemas);
                            when(mock.getStatisticsService().load(any())).thenReturn(new ShardingSphereStatistics());
                        });
                MockedConstruction<DataSourcePoolDestroyer> destroyerMocked = mockConstruction(DataSourcePoolDestroyer.class)) {
            MetaDataContexts actual = new RegisterCenterMetaDataContextsInitFactory(repository, instanceContext).create(createContextManagerBuilderParameter(databaseConfigs));
            assertThat(actual.getMetaData().getAllDatabases(), hasSize(2));
            assertTrue(fooDatabase.getSchema("foo_schema").containsView("persisted_view"));
            assertTrue(fooDatabase.getSchema("foo_schema").containsView("local_view"));
            assertThat(destroyerMocked.constructed(), hasSize(0));
        }
    }
    
    private ComputeNodeInstanceContext mockComputeNodeInstanceContext(final InstanceMetaData metaData) {
        ComputeNodeInstanceContext result = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getInstance().getMetaData()).thenReturn(metaData);
        return result;
    }
    
    private Map<String, DatabaseConfiguration> createDatabaseConfigsWithAndWithoutStorageUnits() {
        DatabaseConfiguration withUnits = mock(DatabaseConfiguration.class, RETURNS_DEEP_STUBS);
        when(withUnits.getStorageUnits()).thenReturn(Collections.singletonMap("with_units", mock(StorageUnit.class)));
        when(withUnits.getDataSources()).thenReturn(Collections.singletonMap(new StorageNode("with_units"), new MockedDataSource()));
        when(withUnits.getRuleConfigurations()).thenReturn(Collections.emptyList());
        Map<String, DatabaseConfiguration> result = new LinkedHashMap<>(2, 1F);
        result.put("with_units", withUnits);
        result.put("without_units", createDatabaseConfigWithoutStorageUnits());
        return result;
    }
    
    private Map<String, DatabaseConfiguration> createDatabaseConfigsWithoutStorageUnits() {
        Map<String, DatabaseConfiguration> result = new LinkedHashMap<>(2, 1F);
        result.put("foo_db", createDatabaseConfigWithoutStorageUnits());
        result.put("bar_db", createDatabaseConfigWithoutStorageUnits());
        return result;
    }
    
    private DatabaseConfiguration createDatabaseConfigWithoutStorageUnits() {
        DatabaseConfiguration result = mock(DatabaseConfiguration.class);
        when(result.getStorageUnits()).thenReturn(Collections.emptyMap());
        when(result.getDataSources()).thenReturn(Collections.emptyMap());
        when(result.getRuleConfigurations()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName, final Collection<ShardingSphereSchema> schemas) {
        return new ShardingSphereDatabase(databaseName,
                mock(DatabaseType.class), new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), schemas);
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter(final Map<String, DatabaseConfiguration> databaseConfigs) {
        return new ContextManagerBuilderParameter(null, databaseConfigs, Collections.emptyMap(), Collections.emptyList(), new Properties(), Collections.emptyList(), null);
    }
}
