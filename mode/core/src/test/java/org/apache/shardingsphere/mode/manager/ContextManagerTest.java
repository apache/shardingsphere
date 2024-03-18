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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeAggregator;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.fixture.ModeRuleConfigurationFixture;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContextManagerTest {
    
    private MetaDataContexts metaDataContexts;
    
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ShardingSphereDatabase database = mockDatabase();
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getDatabase("foo_db").containsSchema("foo_schema")).thenReturn(true);
        when(metaDataContexts.getMetaData().getDatabases().values()).thenReturn(Collections.singleton(database));
        InstanceContext instanceContext = mock(InstanceContext.class);
        when(instanceContext.getModeContextManager()).thenReturn(mock(ModeContextManager.class));
        contextManager = new ContextManager(metaDataContexts, instanceContext);
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(ruleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(mock(DataNode.class)));
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        when(result.getSchemas()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", new ShardingSphereSchema())));
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/foo_db"));
        Map<String, StorageUnit> storageUnits = Collections.singletonMap("foo_ds", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps, new MockedDataSource()));
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
    
    @Test
    void assertGetDataSourceMap() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource()));
        ShardingSphereDatabase database =
                new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), resourceMetaData, mock(RuleMetaData.class), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(database);
        assertThat(contextManager.getStorageUnits(DefaultDatabase.LOGIC_NAME).size(), is(1));
    }
    
    @Test
    void assertRenewMetaDataContexts() {
        MetaDataContexts contexts = mock(MetaDataContexts.class);
        contextManager.renewMetaDataContexts(contexts);
        assertThat(contextManager.getMetaDataContexts(), is(contexts));
    }
    
    @Test
    void assertAddDatabase() {
        contextManager.getResourceMetaDataContextManager().addDatabase("new_db");
        verify(metaDataContexts.getMetaData()).addDatabase(eq("new_db"), any(DatabaseType.class), any(ConfigurationProperties.class));
    }
    
    @Test
    void assertAddExistedDatabase() {
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        contextManager.getResourceMetaDataContextManager().addDatabase("foo_db");
        verify(metaDataContexts.getMetaData(), times(0)).addDatabase(eq("foo_db"), any(DatabaseType.class), any(ConfigurationProperties.class));
    }
    
    @Test
    void assertDropDatabase() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getName()).thenReturn("foo_db");
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        contextManager.getResourceMetaDataContextManager().dropDatabase("foo_db");
        verify(metaDataContexts.getMetaData()).dropDatabase("foo_db");
    }
    
    @Test
    void assertDropNotExistedDatabase() {
        contextManager.getResourceMetaDataContextManager().dropDatabase("not_existed_db");
        verify(metaDataContexts.getMetaData(), times(0)).dropDatabase("not_existed_db");
    }
    
    @Test
    void assertAddSchema() {
        contextManager.getResourceMetaDataContextManager().addSchema("foo_db", "bar_schema");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db")).addSchema(anyString(), any(ShardingSphereSchema.class));
    }
    
    @Test
    void assertAddExistedSchema() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").containsSchema("foo_schema")).thenReturn(true);
        contextManager.getResourceMetaDataContextManager().addSchema("foo_db", "foo_schema");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db"), times(0)).addSchema(anyString(), any(ShardingSphereSchema.class));
    }
    
    @Test
    void assertAlterSchemaForTableAltered() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_schema", toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        ShardingSphereColumn toBeChangedColumn = new ShardingSphereColumn("foo_col", Types.VARCHAR, false, false, false, true, false, false);
        ShardingSphereTable toBeChangedTable = new ShardingSphereTable("foo_tbl", Collections.singleton(toBeChangedColumn), Collections.emptyList(), Collections.emptyList());
        contextManager.getResourceMetaDataContextManager().alterSchema("foo_db", "foo_schema", toBeChangedTable, null);
        ShardingSphereTable table = contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTables().get("foo_tbl");
        assertThat(table.getColumnValues().size(), is(1));
        assertTrue(table.containsColumn("foo_col"));
    }
    
    @Test
    void assertAlterSchemaForViewAltered() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_schema", toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        ShardingSphereView toBeChangedView = new ShardingSphereView("foo_view", "select `foo_view`.`foo_view`.`id` AS `id` from `foo_view`.`foo_view`");
        contextManager.getResourceMetaDataContextManager().alterSchema("foo_db", "foo_schema", null, toBeChangedView);
        ShardingSphereView view = contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").getView("foo_view");
        assertThat(view.getName(), is("foo_view"));
        assertThat(view.getViewDefinition(), is("select `foo_view`.`foo_view`.`id` AS `id` from `foo_view`.`foo_view`"));
    }
    
    @Test
    void assertAlterSchemaForTableDropped() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_schema", createToBeAlteredSchema()));
        contextManager.getResourceMetaDataContextManager().alterSchema("foo_db", "foo_schema", "foo_tbl", null);
        assertFalse(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTables().containsKey("foo_tbl"));
    }
    
    @Test
    void assertAlterSchemaForViewDropped() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_schema", createToBeAlteredSchema()));
        contextManager.getResourceMetaDataContextManager().alterSchema("foo_db", "foo_schema", "foo_view", null);
        assertFalse(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").getViews().containsKey("foo_view"));
    }
    
    private ShardingSphereSchema createToBeAlteredSchema() {
        ShardingSphereTable beforeChangedTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereView beforeChangedView = new ShardingSphereView("foo_tbl", "");
        return new ShardingSphereSchema(Collections.singletonMap("foo_tbl", beforeChangedTable), Collections.singletonMap("foo_view", beforeChangedView));
    }
    
    private void assertAlteredDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getPassword(), is("test"));
        assertThat(actual.getUsername(), is("test"));
    }
    
    @Test
    void assertGetDatabaseWithNull() {
        assertThrows(NoDatabaseSelectedException.class, () -> contextManager.getDatabase(null));
    }
    
    @Test
    void assertGetDatabaseWithEmptyString() {
        assertThrows(NoDatabaseSelectedException.class, () -> contextManager.getDatabase(""));
    }
    
    @Test
    void assertGetDatabaseWhenNotExisted() {
        assertThrows(UnknownDatabaseException.class, () -> contextManager.getDatabase("bar_db"));
    }
    
    @Test
    void assertGetDatabase() {
        assertNotNull(contextManager.getDatabase("foo_db"));
    }
    
    @Test
    void assertAlterRuleConfiguration() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> dataSources = Collections.singletonMap("foo_ds", new MockedDataSource());
        when(resourceMetaData.getDataSources()).thenReturn(StorageNodeAggregator.aggregateDataSources(dataSources));
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), resourceMetaData, mock(RuleMetaData.class), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(metaDataContexts.getPersistService()).thenReturn(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS));
        contextManager.getConfigurationContextManager().alterRuleConfiguration("foo_db", Collections.singleton(new ModeRuleConfigurationFixture()));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getRuleMetaData().getConfigurations().size(), is(1));
    }
    
    private ResourceMetaData createOriginalResource() {
        Map<String, DataSource> originalDataSources = new LinkedHashMap<>(2, 1F);
        originalDataSources.put("ds_1", new MockedDataSource());
        originalDataSources.put("ds_2", new MockedDataSource());
        Map<StorageNode, DataSource> storageNodeDataSourceMap = StorageNodeAggregator.aggregateDataSources(originalDataSources);
        Map<String, StorageNode> storageUnitNodeMap = originalDataSources.keySet().stream()
                .collect(Collectors.toMap(each -> each, StorageNode::new, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        for (Entry<String, StorageNode> entry : storageUnitNodeMap.entrySet()) {
            DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
            when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/foo_db"));
            storageUnits.put(entry.getKey(), new StorageUnit(storageUnitNodeMap.get(entry.getKey()), dataSourcePoolProps, storageNodeDataSourceMap.get(entry.getValue())));
        }
        ResourceMetaData result = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getStorageUnits()).thenReturn(storageUnits);
        when(result.getDataSources()).thenReturn(storageNodeDataSourceMap);
        return result;
    }
    
    private RuleMetaData createOriginalRuleMetaData() {
        RuleMetaData result = mock(RuleMetaData.class);
        when(result.getConfigurations()).thenReturn(Collections.singleton(mock(RuleConfiguration.class)));
        return result;
    }
    
    private Map<String, DataSourcePoolProperties> getToBeAlteredDataSourcePoolPropertiesMap() {
        Map<String, DataSourcePoolProperties> result = new HashMap<>(3, 1F);
        result.put("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties("test", "test")));
        result.put("ds_1", new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties("test", "test")));
        result.put("ds_2", new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties("test", "test")));
        return result;
    }
    
    @Test
    void assertAlterGlobalRuleConfiguration() {
        RuleConfiguration ruleConfig = new TransactionRuleConfiguration("LOCAL", null, new Properties());
        contextManager.getConfigurationContextManager().alterGlobalRuleConfiguration(Collections.singleton(ruleConfig));
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    void assertAlterProperties() {
        contextManager.getConfigurationContextManager().alterProperties(PropertiesBuilder.build(new Property("foo", "foo_value")));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getProps().getProperty("foo"), is("foo_value"));
    }
    
    @Test
    void assertReloadSchema() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getName()).thenReturn("foo_db");
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService persistService = mock(MetaDataPersistService.class);
        when(persistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(persistService);
        ShardingSphereDatabase database = mockDatabase();
        contextManager.reloadSchema(database, "foo_schema", "foo_ds");
        verify(databaseMetaDataPersistService).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertReloadTable() {
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService persistService = mock(MetaDataPersistService.class);
        when(persistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(persistService);
        ShardingSphereDatabase database = mockDatabase();
        contextManager.reloadTable(database, "foo_schema", "foo_table");
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnits().containsKey("foo_ds"));
    }
    
    private Map<String, Object> createProperties(final String username, final String password) {
        Map<String, Object> result = new HashMap<>(3, 1F);
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", username);
        result.put("password", password);
        return result;
    }
    
    @Test
    void assertUpdateClusterStatus() {
        contextManager.updateClusterState("READ_ONLY");
        assertThat(contextManager.getClusterStateContext().getCurrentState(), is(ClusterState.READ_ONLY));
    }
    
    @Test
    void assertClose() {
        contextManager.close();
        verify(metaDataContexts).close();
    }
}
