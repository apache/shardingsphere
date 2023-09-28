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
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeUtils;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapUtils;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
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
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        MutableDataNodeRule mutableDataNodeRule = mock(MutableDataNodeRule.class, RETURNS_DEEP_STUBS);
        when(mutableDataNodeRule.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(mock(DataNode.class)));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(mutableDataNodeRule)));
        when(result.getSchemas()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", new ShardingSphereSchema())));
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getResourceMetaData().getStorageUnitMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
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
    void assertAlterRuleConfiguration() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> dataSources = Collections.singletonMap("foo_ds", new MockedDataSource());
        when(resourceMetaData.getDataSources()).thenReturn(StorageNodeUtils.getStorageNodeDataSources(dataSources));
        StorageUnitMetaData storageUnitMetaData = mock(StorageUnitMetaData.class);
        when(resourceMetaData.getStorageUnitMetaData()).thenReturn(storageUnitMetaData);
        when(storageUnitMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), resourceMetaData, mock(RuleMetaData.class), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(metaDataContexts.getPersistService()).thenReturn(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS));
        contextManager.getConfigurationContextManager().alterRuleConfiguration("foo_db", Collections.singleton(new ModeRuleConfigurationFixture()));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getRuleMetaData().getConfigurations().size(), is(1));
    }
    
    @Test
    void assertAlterDataSourceConfiguration() {
        ShardingSphereDatabase originalDatabaseMetaData = new ShardingSphereDatabase(
                "foo_db", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), createOriginalResource(), createOriginalRuleMetaData(), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(originalDatabaseMetaData);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        contextManager.getConfigurationContextManager().alterDataSourceUnitsConfiguration("foo_db",
                Collections.singletonMap("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties("test", "test"))));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnitMetaData().getStorageUnits().size(), is(3));
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")
                .getResourceMetaData().getDataSources().get(new StorageNodeName("foo_ds")));
    }
    
    private ResourceMetaData createOriginalResource() {
        ResourceMetaData result = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> originalDataSources = new LinkedHashMap<>(2, 1F);
        originalDataSources.put("ds_1", new MockedDataSource());
        originalDataSources.put("ds_2", new MockedDataSource());
        when(result.getStorageUnitMetaData().getDataSources()).thenReturn(originalDataSources);
        Map<StorageNodeName, DataSource> storageNodeDataSourceMap = StorageNodeUtils.getStorageNodeDataSources(originalDataSources);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        Map<String, StorageNode> storageUnitNodeMap = StorageUnitNodeMapUtils.fromDataSources(originalDataSources);
        for (Entry<String, StorageNode> entry : storageUnitNodeMap.entrySet()) {
            storageUnits.put(entry.getKey(), new StorageUnit("foo_db", storageNodeDataSourceMap.get(entry.getValue().getName()), mock(DataSourcePoolProperties.class), entry.getValue()));
        }
        when(result.getStorageUnitMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getStorageUnitMetaData().getStorageNodes()).thenReturn(storageUnitNodeMap);
        when(result.getDataSources()).thenReturn(storageNodeDataSourceMap);
        return result;
    }
    
    private RuleMetaData createOriginalRuleMetaData() {
        RuleMetaData result = mock(RuleMetaData.class);
        when(result.getConfigurations()).thenReturn(Collections.singleton(mock(RuleConfiguration.class)));
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
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getResourceMetaData()
                .getStorageUnitMetaData().getStorageUnits().get("foo_ds").getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService persistService = mock(MetaDataPersistService.class);
        when(persistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(persistService);
        contextManager.reloadSchema("foo_db", "foo_schema", "foo_ds");
        verify(databaseMetaDataPersistService).dropSchema("foo_db", "foo_schema");
    }
    
    @Test
    void assertReloadTable() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnitMetaData().getDataSources())
                .thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService persistService = mock(MetaDataPersistService.class);
        when(persistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(persistService);
        contextManager.reloadTable("foo_db", "foo_schema", "foo_table");
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnitMetaData().getDataSources().containsKey("foo_ds"));
    }
    
    private Map<String, Object> createProperties(final String username, final String password) {
        Map<String, Object> result = new HashMap<>(3, 1F);
        result.putIfAbsent("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.putIfAbsent("username", username);
        result.putIfAbsent("password", password);
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
