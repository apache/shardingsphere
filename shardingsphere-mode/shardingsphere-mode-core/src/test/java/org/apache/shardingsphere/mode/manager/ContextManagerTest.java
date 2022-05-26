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
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData().findSingleRule(TransactionRule.class)).thenReturn(Optional.of(mock(TransactionRule.class)));
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(metaDataContexts.getDatabase("foo_db").getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(metaDataContexts.getDatabase("foo_db").getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(metaDataContexts.getDatabase("foo_db").getSchemas()).thenReturn(Collections.singletonMap("foo_db", new ShardingSphereSchema()));
        when(metaDataContexts.getDatabase("foo_db").getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases()).thenReturn(new LinkedHashMap<>());
        contextManager = new ContextManager(metaDataContexts, mock(TransactionContexts.class), mock(InstanceContext.class));
    }
    
    @Test
    public void assertGetDataSourceMap() {
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("foo_ds", new MockedDataSource()));
        when(metaDataContexts.getDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(
                new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap()));
        assertThat(contextManager.getDataSourceMap(DefaultDatabase.LOGIC_NAME).size(), is(1));
    }
    
    @Test
    public void assertRenewMetaDataContexts() {
        MetaDataContexts contexts = mock(MetaDataContexts.class);
        contextManager.renewMetaDataContexts(contexts);
        assertThat(contextManager.getMetaDataContexts(), is(contexts));
    }
    
    @Test
    public void assertRenewTransactionContexts() {
        TransactionContexts contexts = mock(TransactionContexts.class);
        contextManager.renewTransactionContexts(contexts);
        assertThat(contextManager.getTransactionContexts(), is(contexts));
    }
    
    @Test
    public void assertAddDatabase() throws SQLException {
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(new LinkedHashMap<>());
        contextManager.addDatabase("foo_db");
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().containsKey("foo_db"));
        assertTrue(contextManager.getMetaDataContexts().getOptimizerContext().getFederationMetaData().getDatabases().containsKey("foo_db"));
    }
    
    @Test
    public void assertAlterSchemas() {
        contextManager.alterSchemas("foo_db", Collections.singletonMap("foo_db", new ShardingSphereSchema(Collections.singletonMap("foo_table",
                new TableMetaData("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())))));
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getSchemas().get("foo_db").containsTable("foo_table"));
        assertTrue(contextManager.getMetaDataContexts().getOptimizerContext().getFederationMetaData().getDatabases().containsKey("foo_db"));
        Map<String, FederationSchemaMetaData> schemas = contextManager.getMetaDataContexts().getOptimizerContext().getFederationMetaData().getDatabases().get("foo_db").getSchemas();
        assertTrue(schemas.get("foo_db").getTables().containsKey("foo_table"));
    }
    
    @Test
    public void assertDeleteDatabase() {
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(new HashMap<>(Collections.singletonMap("foo_db", mock(ShardingSphereDatabase.class))));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases()).thenReturn(new HashMap<>(Collections.singletonMap("foo_db", mock(FederationDatabaseMetaData.class))));
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new HashMap<>(Collections.singletonMap("foo_db", mock(OptimizerParserContext.class))));
        when(metaDataContexts.getOptimizerContext().getPlannerContexts()).thenReturn(new HashMap<>(Collections.singletonMap("foo_db", mock(OptimizerPlannerContext.class))));
        contextManager.deleteDatabase("foo_db");
        assertFalse(contextManager.getMetaDataContexts().getMetaData().getDatabases().containsKey("foo_db"));
        assertFalse(contextManager.getMetaDataContexts().getOptimizerContext().getFederationMetaData().getDatabases().containsKey("foo_db"));
        assertFalse(contextManager.getMetaDataContexts().getOptimizerContext().getParserContexts().containsKey("foo_db"));
        assertFalse(contextManager.getMetaDataContexts().getOptimizerContext().getPlannerContexts().containsKey("foo_db"));
    }
    
    @Test
    public void assertAddResource() throws SQLException {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(new HashMap<>(Collections.singletonMap("foo_db", new ShardingSphereDatabase("foo_db", new MySQLDatabaseType(), resource,
                new ShardingSphereRuleMetaData(new LinkedList<>(), new LinkedList<>()), Collections.emptyMap()))));
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
        Map<String, ShardingSphereDatabase> originalDatabaseMap = new HashMap<>(Collections.singletonMap("foo_db", createOriginalDatabaseMetaData()));
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(originalDatabaseMap);
        contextManager.alterResource("foo_db", Collections.singletonMap("bar_ds", new DataSourceProperties(MockedDataSource.class.getName(), createProperties("test", "test"))));
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getResource().getDataSources().get("bar_ds"));
    }
    
    private ShardingSphereDatabase createOriginalDatabaseMetaData() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("foo_db", new MockedDataSource()));
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
        when(metaDataContexts.getDatabase("foo_db").getResource().getDataSources()).thenReturn(new HashMap<>(Collections.singletonMap("foo_ds", new MockedDataSource())));
        contextManager.dropResource("foo_db", Collections.singleton("foo_ds"));
        assertTrue(contextManager.getMetaDataContexts().getDatabase("foo_db").getResource().getDataSources().isEmpty());
    }
    
    @Test
    public void assertAlterRuleConfiguration() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("foo_db",
                new ShardingSphereDatabase("foo_db", new MySQLDatabaseType(), resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap())));
        when(metaDataContexts.getPersistService()).thenReturn(Optional.of(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS)));
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        contextManager.alterRuleConfiguration("foo_db", Collections.singleton(ruleConfig));
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    public void assertAlterDataSourceConfiguration() {
        ShardingSphereDatabase originalDatabaseMetaData = new ShardingSphereDatabase(
                "foo_db", new MySQLDatabaseType(), createOriginalResource(), createOriginalRuleMetaData(), Collections.emptyMap());
        when(metaDataContexts.getDatabase("foo_db")).thenReturn(originalDatabaseMetaData);
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("foo_db", originalDatabaseMetaData));
        contextManager.alterDataSourceConfiguration("foo_db", Collections.singletonMap("foo_ds", new DataSourceProperties(MockedDataSource.class.getName(), createProperties("test", "test"))));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("foo_db").getResource().getDataSources().size(), is(1));
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getDatabase("foo_db").getResource().getDataSources().get("foo_ds"));
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
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
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
    public void assertReloadMetaData() {
        when(metaDataContexts.getDatabase("foo_db").getResource().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        SchemaMetaDataPersistService schemaMetaDataPersistService = mock(SchemaMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        when(metaDataPersistService.getSchemaMetaDataService()).thenReturn(schemaMetaDataPersistService);
        when(metaDataContexts.getPersistService()).thenReturn(Optional.of(metaDataPersistService));
        contextManager.reloadMetaData("foo_db");
        verify(schemaMetaDataPersistService, times(1)).persistMetaData(eq("foo_db"), eq("foo_db"), any(ShardingSphereSchema.class));
        contextManager.reloadMetaData("foo_db", "foo_schema", "foo_table");
        assertNotNull(contextManager.getMetaDataContexts().getDatabase("foo_db").getSchemas().get("foo_db"));
        assertTrue(contextManager.getMetaDataContexts().getDatabase("foo_db").getResource().getDataSources().containsKey("foo_ds"));
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
