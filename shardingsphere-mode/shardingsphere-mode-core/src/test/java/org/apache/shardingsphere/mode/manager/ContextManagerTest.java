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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
    public void setUp() throws SQLException {
        metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(metaDataContexts.getMetaData().getDatabases().get("foo_db").getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(metaDataContexts.getMetaData().getDatabases().get("foo_db").getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(metaDataContexts.getMetaData().getDatabases().get("foo_db").getSchemas()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", new ShardingSphereSchema())));
        when(metaDataContexts.getMetaData().getDatabases().get("foo_db").getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getOptimizerContext().getPlannerContexts()).thenReturn(new LinkedHashMap<>());
        contextManager = new ContextManager(metaDataContexts, mock(InstanceContext.class));
    }
    
    @Test
    public void assertGetDataSourceMap() {
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("foo_ds", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database));
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
        contextManager.addDatabase("foo_db");
        verify(metaDataContexts.getMetaData()).addDatabase(eq("foo_db"), any(DatabaseType.class));
        verify(metaDataContexts.getOptimizerContext()).addDatabase(eq("foo_db"), any(DatabaseType.class));
    }
    
    @Test
    public void assertAddExistedDatabase() throws SQLException {
        when(metaDataContexts.getMetaData().getDatabases().containsKey("foo_db")).thenReturn(true);
        contextManager.addDatabase("foo_db");
        verify(metaDataContexts.getMetaData(), times(0)).addDatabase(eq("foo_db"), any(DatabaseType.class));
        verify(metaDataContexts.getOptimizerContext(), times(0)).addDatabase(eq("foo_db"), any(DatabaseType.class));
    }
    
    @Test
    public void assertDropDatabase() {
        when(metaDataContexts.getMetaData().getDatabases().containsKey("foo_db")).thenReturn(true);
        contextManager.dropDatabase("foo_db");
        verify(metaDataContexts.getMetaData()).dropDatabase("foo_db");
        verify(metaDataContexts.getOptimizerContext()).dropDatabase("foo_db");
    }
    
    @Test
    public void assertDropNotExistedDatabase() {
        contextManager.dropDatabase("foo_db");
        verify(metaDataContexts.getMetaData(), times(0)).dropDatabase("foo_db");
        verify(metaDataContexts.getOptimizerContext(), times(0)).dropDatabase("foo_db");
    }
    
    @Test
    public void assertAddSchema() {
        contextManager.addSchema("foo_db", "bar_schema");
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getSchemas().containsKey("bar_schema"));
        verify(metaDataContexts.getOptimizerContext()).addSchema("foo_db", "bar_schema");
    }
    
    @Test
    public void assertAddExistedSchema() {
        contextManager.addSchema("foo_db", "foo_schema");
        verify(metaDataContexts.getOptimizerContext(), times(0)).addSchema("foo_db", "foo_schema");
    }
    
    @Test
    public void assertAddResource() throws SQLException {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(new HashMap<>(Collections.singletonMap("foo_db", new ShardingSphereDatabase("foo_db", new MySQLDatabaseType(), resource,
                new ShardingSphereRuleMetaData(new LinkedList<>()), Collections.emptyMap()))));
        contextManager.addResource("foo_db", createToBeAddedDataSourceProperties());
        assertAddedDataSources(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getResource().getDataSources());
    }
    
    private Map<String, DataSourceProperties> createToBeAddedDataSourceProperties() {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(2, 1);
        result.put("foo_ds_1", new DataSourceProperties(MockedDataSource.class.getName(), createProperties("root", "root")));
        result.put("foo_ds_2", new DataSourceProperties(MockedDataSource.class.getName(), createProperties("root", "root")));
        return result;
    }
    
    private void assertAddedDataSources(final Map<String, DataSource> actual) {
        assertThat(actual.size(), is(2));
        assertAddedDataSource((MockedDataSource) actual.get("foo_ds_1"));
        assertAddedDataSource((MockedDataSource) actual.get("foo_ds_2"));
    }
    
    private void assertAddedDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    @Test
    public void assertAlterResource() throws SQLException {
        Map<String, ShardingSphereDatabase> originalDatabases = new HashMap<>(Collections.singletonMap("foo_db", createOriginalDatabaseMetaData()));
        ShardingSphereResource originalResource = originalDatabases.get("foo_db").getResource();
        DataSource originalDataSource = originalResource.getDataSources().get("bar_ds");
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(originalDatabases);
        contextManager.alterResource("foo_db", Collections.singletonMap("bar_ds", new DataSourceProperties(MockedDataSource.class.getName(),
                createProperties("test", "test"))));
        verify(originalResource, times(1)).close(originalDataSource);
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getResource().getDataSources().get("bar_ds"));
    }
    
    private ShardingSphereDatabase createOriginalDatabaseMetaData() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("bar_ds", new MockedDataSource()));
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        return new ShardingSphereDatabase("foo_db", new MySQLDatabaseType(), resource, ruleMetaData, Collections.emptyMap());
    }
    
    private void assertAlteredDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getPassword(), is("test"));
        assertThat(actual.getUsername(), is("test"));
    }
    
    @Test
    public void assertDropResource() {
        when(metaDataContexts.getMetaData().getDatabases().get("foo_db").getResource().getDataSources()).thenReturn(new HashMap<>(Collections.singletonMap("foo_ds", new MockedDataSource())));
        contextManager.dropResource("foo_db", Collections.singleton("foo_ds"));
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getResource().getDataSources().isEmpty());
    }
    
    @Test
    public void assertAlterRuleConfiguration() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>();
        databases.put("foo_db", new ShardingSphereDatabase("foo_db", new MySQLDatabaseType(), resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap()));
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(databases);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        when(metaDataContexts.getPersistService()).thenReturn(Optional.of(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS)));
        // TODO TransactionRule is global rule, do not use it in database rule test
        RuleConfiguration ruleConfig = new TransactionRuleConfiguration("LOCAL", null, new Properties());
        contextManager.alterRuleConfiguration("foo_db", Collections.singleton(ruleConfig));
        // TODO create DistributedRuleFixture to assert alter rule
        // assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    public void assertAlterDataSourceConfiguration() {
        ShardingSphereDatabase originalDatabaseMetaData = new ShardingSphereDatabase(
                "foo_db", new MySQLDatabaseType(), createOriginalResource(), createOriginalRuleMetaData(), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("foo_db", originalDatabaseMetaData));
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        contextManager.alterDataSourceConfiguration("foo_db", Collections.singletonMap("foo_ds", new DataSourceProperties(MockedDataSource.class.getName(), createProperties("test", "test"))));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getResource().getDataSources().size(), is(1));
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getResource().getDataSources().get("foo_ds"));
    }
    
    private ShardingSphereResource createOriginalResource() {
        ShardingSphereResource result = mock(ShardingSphereResource.class);
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
        when(metaDataContexts.getMetaData().getDatabases().get("foo_db").getResource().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(Optional.of(metaDataPersistService));
        contextManager.reloadDatabase("foo_db");
        verify(databaseMetaDataPersistService, times(1)).deleteSchema(eq("foo_db"), eq("foo_schema"));
        verify(databaseMetaDataPersistService, times(1)).persistMetaData(eq("foo_db"), eq("foo_db"), any(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertReloadTable() {
        when(metaDataContexts.getMetaData().getDatabases().get("foo_db").getResource().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        DatabaseMetaDataPersistService databaseMetaDataPersistService = mock(DatabaseMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        when(metaDataPersistService.getDatabaseMetaDataService()).thenReturn(databaseMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(Optional.of(metaDataPersistService));
        contextManager.reloadTable("foo_db", "foo_schema", "foo_table");
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getResource().getDataSources().containsKey("foo_ds"));
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
