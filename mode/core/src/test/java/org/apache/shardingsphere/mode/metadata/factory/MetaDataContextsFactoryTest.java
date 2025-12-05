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

package org.apache.shardingsphere.mode.metadata.factory;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabaseFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.manager.resource.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.fixture.rule.MockedRule;
import org.apache.shardingsphere.test.infra.fixture.rule.MockedRuleConfiguration;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ShardingSphereDatabasesFactory.class, GlobalRulesBuilder.class, DatabaseTypeEngine.class,
        ShardingSphereDatabaseFactory.class, ShardingSphereStatisticsFactory.class, StorageUnitNodeMapCreator.class, DatabaseTypeFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MetaDataContextsFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistFacade metaDataPersistFacade;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() throws SQLException {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                databaseType, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        when(ShardingSphereDatabasesFactory.create(anyMap(), anyMap(), any(), any())).thenReturn(Collections.singleton(database));
        when(ShardingSphereDatabasesFactory.create(anyMap(), any(ConfigurationProperties.class), any(ComputeNodeInstanceContext.class))).thenReturn(Collections.singleton(database));
        when(ShardingSphereDatabaseFactory.create(anyString(), any(DatabaseType.class), any(DatabaseConfiguration.class), any(ConfigurationProperties.class), any(ComputeNodeInstanceContext.class)))
                .thenAnswer(invocation -> createDatabaseFromConfiguration(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2), Collections.emptyList()));
        when(GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.singleton(new MockedRule()));
        when(DatabaseTypeEngine.getProtocolType(any(DatabaseConfiguration.class), any(ConfigurationProperties.class))).thenReturn(databaseType);
        when(DatabaseTypeFactory.get(anyString())).thenReturn(databaseType);
        when(metaDataPersistFacade.getRepository()).thenReturn(repository);
    }
    
    private ShardingSphereDatabase createDatabaseFromConfiguration(final String databaseName, final DatabaseType protocolType,
                                                                   final DatabaseConfiguration databaseConfiguration, final Collection<ShardingSphereSchema> schemas) {
        return new ShardingSphereDatabase(databaseName, protocolType,
                new ResourceMetaData(databaseConfiguration.getDataSources(), databaseConfiguration.getStorageUnits()), new RuleMetaData(Collections.emptyList()), schemas);
    }
    
    @Test
    void assertCreateWithJDBCInstanceMetaData() throws SQLException {
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(computeNodeInstanceContext.getInstance().getMetaData()).thenReturn(mock(JDBCInstanceMetaData.class));
        when(repository.getChildrenKeys("/metadata")).thenReturn(Collections.singletonList("foo_db"));
        when(repository.getChildrenKeys("/metadata/foo_db/data_sources/units")).thenReturn(Collections.emptyList());
        when(repository.getChildrenKeys("/rules")).thenReturn(Collections.singletonList("global_fixture"));
        when(repository.query("/rules/global_fixture/active_version")).thenReturn(String.valueOf(0));
        when(repository.query("/rules/global_fixture/versions/0")).thenReturn("name: global_name");
        when(repository.getChildrenKeys("/statistics/databases")).thenReturn(Collections.emptyList());
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, computeNodeInstanceContext).create(createContextManagerBuilderParameter());
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().size(), is(1));
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), isA(MockedRule.class));
        assertTrue(actual.getMetaData().containsDatabase("foo_db"));
        assertThat(actual.getMetaData().getAllDatabases().size(), is(1));
    }
    
    @Test
    void assertCreateWithoutRegisteredDatabasesUsesLocalFactory() throws SQLException {
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, mock()).create(createContextManagerBuilderParameter());
        assertTrue(actual.getMetaData().containsDatabase("foo_db"));
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), isA(MockedRule.class));
    }
    
    @Test
    void assertCreateBySwitchResourceFiltersStaleResources() throws SQLException {
        StorageNode staleNode = new StorageNode("stale_ds");
        StorageNode activeNode = new StorageNode("active_ds");
        Map<StorageNode, DataSource> currentStorageNodes = new LinkedHashMap<>(2, 1F);
        currentStorageNodes.put(staleNode, new MockedDataSource());
        currentStorageNodes.put(activeNode, new MockedDataSource());
        Map<String, StorageUnit> currentStorageUnits = new LinkedHashMap<>(2, 1F);
        currentStorageUnits.put("stale_ds", createStorageUnit("stale_ds"));
        currentStorageUnits.put("active_ds", createStorageUnit("active_ds"));
        ResourceMetaData resourceMetaData = new ResourceMetaData(currentStorageNodes, currentStorageUnits);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, resourceMetaData, new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database),
                new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaData, new ShardingSphereStatistics());
        Map<StorageNode, DataSource> newDataSources = Collections.singletonMap(new StorageNode("new_ds"), new MockedDataSource());
        SwitchingResource switchingResource = new SwitchingResource(newDataSources, Collections.singletonMap(staleNode, new MockedDataSource()),
                Collections.singleton("stale_ds"), createDataSourcePoolPropertiesMap("active_ds", "new_ds"));
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, mock()).createBySwitchResource("foo_db", false, switchingResource, originalMetaDataContexts);
        ResourceMetaData actualResourceMetaData = actual.getMetaData().getDatabase("foo_db").getResourceMetaData();
        assertFalse(actualResourceMetaData.getDataSources().containsKey(staleNode));
        assertTrue(actualResourceMetaData.getDataSources().containsKey(activeNode));
        assertTrue(actualResourceMetaData.getDataSources().containsKey(new StorageNode("new_ds")));
        assertFalse(actualResourceMetaData.getStorageUnits().containsKey("stale_ds"));
        assertTrue(actualResourceMetaData.getStorageUnits().containsKey("active_ds"));
        assertTrue(actualResourceMetaData.getStorageUnits().containsKey("new_ds"));
    }
    
    @Test
    void assertCreateBySwitchResourceKeepsExistingNodesWhenNoNewDataSources() throws SQLException {
        ResourceMetaData resourceMetaData = createResourceMetaDataWithSingleUnit();
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, resourceMetaData, new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database),
                new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaData, new ShardingSphereStatistics());
        SwitchingResource switchingResource = new SwitchingResource(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), createDataSourcePoolPropertiesMap("foo_ds"));
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, mock()).createBySwitchResource("foo_db", false, switchingResource, originalMetaDataContexts);
        ResourceMetaData actualResourceMetaData = actual.getMetaData().getDatabase("foo_db").getResourceMetaData();
        assertTrue(actualResourceMetaData.getDataSources().containsKey(new StorageNode("foo_ds")));
        assertTrue(actualResourceMetaData.getStorageUnits().containsKey("foo_ds"));
    }
    
    @Test
    void assertCreateByAlterRuleLoadsSchemasFromRepositoryWhenPersistenceDisabled() throws SQLException {
        Collection<ShardingSphereSchema> loadedSchemas = Arrays.asList(createSchemaWithTable("foo_schema", "persisted_table"), createSchemaWithTable("new_schema", "new_table"));
        when(metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().load("foo_db")).thenReturn(loadedSchemas);
        ShardingSphereSchema originSchema = createSchemaWithTable("foo_schema", "origin_table");
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, createResourceMetaDataWithSingleUnit(),
                new RuleMetaData(Collections.emptyList()), Collections.singleton(originSchema));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED.getKey(), Boolean.FALSE.toString()))));
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaData, new ShardingSphereStatistics());
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, mock())
                .createByAlterRule("foo_db", true, Collections.singleton(new MockedRuleConfiguration("alter_rule")), originalMetaDataContexts);
        ShardingSphereSchema mergedSchema = loadedSchemas.stream().filter(each -> "foo_schema".equals(each.getName())).findFirst().orElseThrow(IllegalStateException::new);
        assertThat(mergedSchema.getAllTables().size(), is(2));
        ShardingSphereSchema untouchedSchema = loadedSchemas.stream().filter(each -> "new_schema".equals(each.getName())).findFirst().orElseThrow(IllegalStateException::new);
        assertThat(untouchedSchema.getAllTables().size(), is(1));
        assertTrue(actual.getMetaData().containsDatabase("foo_db"));
    }
    
    @Test
    void assertCreateByAlterRuleKeepsPersistedSchemasWhenEnabled() throws SQLException {
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", databaseType, createResourceMetaDataWithSingleUnit(), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED.getKey(), Boolean.TRUE.toString()))));
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaData, new ShardingSphereStatistics());
        Collection<ShardingSphereSchema> loadedSchemas = Collections.singleton(createSchemaWithTable("persisted_schema", "persisted_table"));
        when(metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().load("foo_db")).thenReturn(loadedSchemas);
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, mock())
                .createByAlterRule("foo_db", true, Collections.singleton(new MockedRuleConfiguration("persist_rule")), originalMetaDataContexts);
        ShardingSphereSchema persistedSchema = loadedSchemas.iterator().next();
        assertThat(persistedSchema.getAllTables().size(), is(1));
        assertTrue(actual.getMetaData().containsDatabase("foo_db"));
    }
    
    @Test
    void assertCreateWithProxyInstanceMetaData() throws SQLException {
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS);
        when(computeNodeInstanceContext.getInstance().getMetaData()).thenReturn(mock(ProxyInstanceMetaData.class));
        when(repository.getChildrenKeys("/metadata")).thenReturn(Collections.singletonList("foo_db"));
        when(repository.getChildrenKeys("/metadata/foo_db/data_sources/units")).thenReturn(Collections.emptyList());
        when(repository.getChildrenKeys("/rules")).thenReturn(Collections.singletonList("global_fixture"));
        when(repository.query("/rules/global_fixture/active_version")).thenReturn(String.valueOf(0));
        when(repository.query("/rules/global_fixture/versions/0")).thenReturn("name: global_name");
        when(repository.getChildrenKeys("/statistics/databases")).thenReturn(Collections.emptyList());
        MetaDataContexts actual = new MetaDataContextsFactory(metaDataPersistFacade, computeNodeInstanceContext).create(createContextManagerBuilderParameter());
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().size(), is(1));
        assertThat(actual.getMetaData().getGlobalRuleMetaData().getRules().iterator().next(), isA(MockedRule.class));
        assertTrue(actual.getMetaData().containsDatabase("foo_db"));
        assertThat(actual.getMetaData().getAllDatabases().size(), is(1));
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo", new MockedDataSource()), Collections.emptyList());
        return new ContextManagerBuilderParameter(null, Collections.singletonMap("foo_db", databaseConfig), Collections.emptyMap(),
                Collections.emptyList(), new Properties(), Collections.emptyList(), null);
    }
    
    private Map<String, DataSourcePoolProperties> createDataSourcePoolPropertiesMap(final String... storageUnitNames) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(storageUnitNames.length, 1F);
        for (String each : storageUnitNames) {
            Map<String, Object> props = new LinkedHashMap<>(2, 1F);
            props.put("url", "jdbc:mysql://localhost:3306/" + each);
            props.put("username", "root");
            result.put(each, new DataSourcePoolProperties("HikariCP", props));
        }
        return result;
    }
    
    private StorageUnit createStorageUnit(final String storageUnitName) {
        Map<String, Object> props = new LinkedHashMap<>(2, 1F);
        props.put("url", "jdbc:mock://127.0.0.1/" + storageUnitName);
        props.put("username", "root");
        StorageUnit result = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(result.getDataSourcePoolProperties()).thenReturn(new DataSourcePoolProperties("HikariCP", props));
        when(result.getDataSource()).thenReturn(new MockedDataSource());
        return result;
    }
    
    private ResourceMetaData createResourceMetaDataWithSingleUnit() {
        Map<StorageNode, DataSource> dataSources = Collections.singletonMap(new StorageNode("foo_ds"), new MockedDataSource());
        Map<String, StorageUnit> storageUnits = Collections.singletonMap("foo_ds", createStorageUnit("foo_ds"));
        return new ResourceMetaData(dataSources, storageUnits);
    }
    
    private ShardingSphereSchema createSchemaWithTable(final String schemaName, final String tableName) {
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("id", 0, true, false, "", false, true, false, true));
        ShardingSphereTable table = new ShardingSphereTable(tableName, columns, Collections.emptyList(), Collections.emptyList());
        return new ShardingSphereSchema(schemaName, Collections.singleton(table), Collections.emptyList());
    }
}
