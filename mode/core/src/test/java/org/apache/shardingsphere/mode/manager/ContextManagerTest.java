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
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ContextManagerTest {
    
    private MetaDataContexts metaDataContexts;
    
    private ContextManager contextManager;
    
    @Before
    public void setUp() {
        metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(metaDataContexts.getMetaData().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ShardingSphereDatabase database = mockDatabase();
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getDatabase("foo_db").containsSchema("foo_schema")).thenReturn(true);
        when(metaDataContexts.getMetaData().getDatabases().values()).thenReturn(Collections.singleton(database));
        contextManager = new ContextManager(metaDataContexts, mock(InstanceContext.class));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(result.getResourceMetaData().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        MutableDataNodeRule mutableDataNodeRule = mock(MutableDataNodeRule.class, RETURNS_DEEP_STUBS);
        when(mutableDataNodeRule.findSingleTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(mock(DataNode.class)));
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(mutableDataNodeRule)));
        when(result.getSchemas()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", new ShardingSphereSchema())));
        return result;
    }
    
    @Test
    public void assertGetDataSourceMap() {
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", Collections.singletonMap("foo_ds", new MockedDataSource()));
        ShardingSphereDatabase database =
                new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), resourceMetaData, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(database);
        assertThat(contextManager.getDataSourceMap(DefaultDatabase.LOGIC_NAME).size(), is(1));
    }
    
    @Test
    public void assertRenewMetaDataContexts() {
        MetaDataContexts contexts = mock(MetaDataContexts.class);
        contextManager.renewMetaDataContexts(contexts);
        assertThat(contextManager.getMetaDataContexts(), is(contexts));
    }
    
    @Test
    public void assertAddDatabase() throws SQLException {
        contextManager.addDatabaseAndPersist("new_db");
        verify(metaDataContexts.getMetaData()).addDatabase(eq("new_db"), any(DatabaseType.class));
    }
    
    @Test
    public void assertAddDatabaseAndPersist() throws SQLException {
        contextManager.addDatabaseAndPersist("new_db");
        verify(metaDataContexts.getMetaData()).addDatabase(eq("new_db"), any(DatabaseType.class));
    }
    
    @Test
    public void assertAddExistedDatabase() throws SQLException {
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        contextManager.addDatabaseAndPersist("foo_db");
        verify(metaDataContexts.getMetaData(), times(0)).addDatabase(eq("foo_db"), any(DatabaseType.class));
    }
    
    @Test
    public void assertAddExistedDatabaseAndPersist() throws SQLException {
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        contextManager.addDatabaseAndPersist("foo_db");
        verify(metaDataContexts.getMetaData(), times(0)).addDatabase(eq("foo_db"), any(DatabaseType.class));
    }
    
    @Test
    public void assertDropDatabase() {
        when(metaDataContexts.getMetaData().getActualDatabaseName("foo_db")).thenReturn("foo_db");
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        contextManager.dropDatabaseAndPersist("foo_db");
        verify(metaDataContexts.getMetaData()).dropDatabase("foo_db");
    }
    
    @Test
    public void assertDropDatabaseAndPersist() {
        when(metaDataContexts.getMetaData().getActualDatabaseName("foo_db")).thenReturn("foo_db");
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        contextManager.dropDatabaseAndPersist("foo_db");
        verify(metaDataContexts.getMetaData()).dropDatabase("foo_db");
    }
    
    @Test
    public void assertDropNotExistedDatabase() {
        contextManager.dropDatabaseAndPersist("not_existed_db");
        verify(metaDataContexts.getMetaData(), times(0)).dropDatabase("not_existed_db");
    }
    
    @Test
    public void assertDropNotExistedDatabaseAndPersist() {
        contextManager.dropDatabaseAndPersist("not_existed_db");
        verify(metaDataContexts.getMetaData(), times(0)).dropDatabase("not_existed_db");
    }
    
    @Test
    public void assertAddSchema() {
        contextManager.addSchema("foo_db", "bar_schema");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db")).putSchema(anyString(), any(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertAddExistedSchema() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").containsSchema("foo_schema")).thenReturn(true);
        contextManager.addSchema("foo_db", "foo_schema");
        verify(metaDataContexts.getMetaData().getDatabase("foo_db"), times(0)).putSchema(anyString(), any(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertAlterSchemaForTableAltered() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_schema", toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        ShardingSphereColumn toBeChangedColumn = new ShardingSphereColumn("foo_col", Types.VARCHAR, false, false, false, true);
        ShardingSphereTable toBeChangedTable = new ShardingSphereTable("foo_tbl", Collections.singleton(toBeChangedColumn), Collections.emptyList(), Collections.emptyList());
        contextManager.alterSchema("foo_db", "foo_schema", toBeChangedTable, null);
        ShardingSphereTable table = contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTables().get("foo_tbl");
        assertThat(table.getColumns().size(), is(1));
        assertTrue(table.getColumns().containsKey("foo_col"));
    }
    
    @Test
    public void assertAlterSchemaForViewAltered() {
        ShardingSphereSchema toBeAlteredSchema = createToBeAlteredSchema();
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_schema", toBeAlteredSchema));
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(toBeAlteredSchema);
        ShardingSphereView toBeChangedView = new ShardingSphereView("foo_view", "select `foo_view`.`foo_view`.`id` AS `id` from `foo_view`.`foo_view`");
        contextManager.alterSchema("foo_db", "foo_schema", null, toBeChangedView);
        ShardingSphereView view = contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").getView("foo_view");
        assertThat(view.getName(), is("foo_view"));
        assertThat(view.getViewDefinition(), is("select `foo_view`.`foo_view`.`id` AS `id` from `foo_view`.`foo_view`"));
    }
    
    @Test
    public void assertAlterSchemaForTableDropped() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_schema", createToBeAlteredSchema()));
        contextManager.alterSchema("foo_db", "foo_schema", "foo_tbl", null);
        assertFalse(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTables().containsKey("foo_tbl"));
    }
    
    @Test
    public void assertAlterSchemaForViewDropped() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_schema", createToBeAlteredSchema()));
        contextManager.alterSchema("foo_db", "foo_schema", "foo_view", null);
        assertFalse(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema").getViews().containsKey("foo_view"));
    }
    
    private ShardingSphereSchema createToBeAlteredSchema() {
        ShardingSphereTable beforeChangedTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereView beforeChangedView = new ShardingSphereView("foo_tbl", "");
        return new ShardingSphereSchema(Collections.singletonMap("foo_tbl", beforeChangedTable), Collections.singletonMap("foo_view", beforeChangedView));
    }
    
    @Ignore
    @Test
    public void assertUpdateResources() throws SQLException {
        ShardingSphereDatabase originalDatabase = createOriginalDatabaseMetaData();
        ShardingSphereResourceMetaData originalResourceMetaData = originalDatabase.getResourceMetaData();
        DataSource originalDataSource = originalResourceMetaData.getDataSources().get("bar_ds");
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(originalDatabase);
        contextManager.updateResources("foo_db", Collections.singletonMap("bar_ds", new DataSourceProperties(MockedDataSource.class.getName(),
                createProperties("test", "test"))));
        verify(originalResourceMetaData, times(1)).close(originalDataSource);
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getDataSources().get("bar_ds"));
    }
    
    private ShardingSphereDatabase createOriginalDatabaseMetaData() {
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("bar_ds", new MockedDataSource()));
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        return new ShardingSphereDatabase("foo_db", new MySQLDatabaseType(), resourceMetaData, ruleMetaData, Collections.emptyMap());
    }
    
    private void assertAlteredDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getPassword(), is("test"));
        assertThat(actual.getUsername(), is("test"));
    }
    
    @Test
    public void assertDropResources() throws SQLException {
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "foo_db", new MySQLDatabaseType(), createOriginalResource(), createOriginalRuleMetaData(), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        Map<String, ShardingSphereDatabase> databases = new LinkedHashMap<>(1, 1);
        databases.put("foo_db", database);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(databases);
        when(metaDataContexts.getMetaData().getActualDatabaseName("foo_db")).thenReturn("foo_db");
        when(metaDataContexts.getPersistService()).thenReturn(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS));
        Map<String, DataSourceProperties> dataSourcePropertiesMap = new LinkedHashMap<>(1, 1);
        dataSourcePropertiesMap.put("ds_1", mock(DataSourceProperties.class));
        dataSourcePropertiesMap.put("ds_2", mock(DataSourceProperties.class));
        when(metaDataContexts.getPersistService().getDataSourceService().load("foo_db")).thenReturn(dataSourcePropertiesMap);
        contextManager.dropResources("foo_db", Arrays.asList("ds_1", "ds_2"));
        assertTrue(metaDataContexts.getMetaData().getDatabases().get("foo_db").getResourceMetaData().getDataSources().isEmpty());
    }
    
    @Test
    public void assertAlterRuleConfiguration() {
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>();
        databases.put("foo_db", new ShardingSphereDatabase("foo_db", new MySQLDatabaseType(), resourceMetaData, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap()));
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(databases);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        when(metaDataContexts.getPersistService()).thenReturn(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS));
        when(metaDataContexts.getMetaData().getActualDatabaseName("foo_db")).thenReturn("foo_db");
        // TODO TransactionRule is global rule, do not use it in database rule test
        RuleConfiguration ruleConfig = new TransactionRuleConfiguration("LOCAL", null, new Properties());
        contextManager.alterRuleConfiguration("foo_db", Collections.singleton(ruleConfig));
        // TODO create DistributedRuleFixture to assert alter rule
        // assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    public void assertAlterDataSourceConfiguration() {
        ShardingSphereDatabase originalDatabaseMetaData = new ShardingSphereDatabase(
                "foo_db", new MySQLDatabaseType(), createOriginalResource(), createOriginalRuleMetaData(), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(originalDatabaseMetaData);
        when(metaDataContexts.getMetaData().getActualDatabaseName("foo_db")).thenReturn("foo_db");
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        contextManager.alterDataSourceConfiguration("foo_db", Collections.singletonMap("foo_ds", new DataSourceProperties(MockedDataSource.class.getName(), createProperties("test", "test"))));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getDataSources().size(), is(3));
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getDataSources().get("foo_ds"));
    }
    
    private ShardingSphereResourceMetaData createOriginalResource() {
        ShardingSphereResourceMetaData result = mock(ShardingSphereResourceMetaData.class);
        Map<String, DataSource> originalDataSources = new LinkedHashMap<>(2, 1);
        originalDataSources.put("ds_1", new MockedDataSource());
        originalDataSources.put("ds_2", new MockedDataSource());
        when(result.getDataSources()).thenReturn(originalDataSources);
        return result;
    }
    
    private ShardingSphereRuleMetaData createOriginalRuleMetaData() {
        ShardingSphereRuleMetaData result = mock(ShardingSphereRuleMetaData.class);
        when(result.getConfigurations()).thenReturn(Collections.singleton(mock(RuleConfiguration.class)));
        return result;
    }
    
    @Test
    public void assertAlterGlobalRuleConfiguration() {
        RuleConfiguration ruleConfig = new TransactionRuleConfiguration("LOCAL", null, new Properties());
        contextManager.alterGlobalRuleConfiguration(Collections.singleton(ruleConfig));
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    public void assertAlterProperties() {
        Properties props = new Properties();
        props.put("foo", "foo_value");
        contextManager.alterProperties(props);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getProps().getProperty("foo"), is("foo_value"));
    }
    
    @Test
    public void assertReloadDatabase() {
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(1, 1);
        dataSourceMap.put("foo_ds", new MockedDataSource());
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getResourceMetaData().getDataSources()).thenReturn(dataSourceMap);
        when(metaDataContexts.getMetaData().getActualDatabaseName("foo_db")).thenReturn("foo_db");
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService persistService = mock(MetaDataPersistService.class);
        when(persistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(persistService);
        contextManager.reloadDatabase("foo_db");
        verify(databaseMetaDataPersistService, times(1)).dropSchema(eq("foo_db"), eq("foo_schema"));
        verify(databaseMetaDataPersistService, times(1)).compareAndPersist(eq("foo_db"), eq("foo_db"), any(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertReloadSchema() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getResourceMetaData().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        when(metaDataContexts.getMetaData().getActualDatabaseName("foo_db")).thenReturn("foo_db");
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService persistService = mock(MetaDataPersistService.class);
        when(persistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(persistService);
        contextManager.reloadSchema("foo_db", "foo_schema", "foo_ds");
        verify(databaseMetaDataPersistService, times(1)).dropSchema(eq("foo_db"), eq("foo_schema"));
    }
    
    @Test
    public void assertReloadTable() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getResourceMetaData().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService persistService = mock(MetaDataPersistService.class);
        when(persistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(persistService);
        contextManager.reloadTable("foo_db", "foo_schema", "foo_table");
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getDataSources().containsKey("foo_ds"));
    }
    
    private Map<String, Object> createProperties(final String username, final String password) {
        Map<String, Object> result = new HashMap<>(3, 1);
        result.putIfAbsent("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.putIfAbsent("username", username);
        result.putIfAbsent("password", password);
        return result;
    }
    
    @Test
    public void assertClose() throws Exception {
        contextManager.close();
        verify(metaDataContexts).close();
    }
}
