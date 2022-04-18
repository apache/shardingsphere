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
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

@RunWith(MockitoJUnitRunner.class)
public final class ContextManagerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    private ContextManager contextManager;
    
    @Before
    public void setUp() throws SQLException {
        contextManager = new ContextManager();
        contextManager.init(metaDataContexts, mock(TransactionContexts.class), mock(InstanceContext.class));
        when(metaDataContexts.getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(metaDataContexts.getMetaData("foo_schema").getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
    }
    
    @Test
    public void assertGetDataSourceMap() {
        ShardingSphereResource resource = new ShardingSphereResource(
                Collections.singletonMap("foo_ds", new MockedDataSource()), mock(DataSourcesMetaData.class), mock(CachedDatabaseMetaData.class), mock(DatabaseType.class));
        when(metaDataContexts.getMetaData(DefaultSchema.LOGIC_NAME)).thenReturn(
                new ShardingSphereMetaData(DefaultSchema.LOGIC_NAME, resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap()));
        assertThat(contextManager.getDataSourceMap(DefaultSchema.LOGIC_NAME).size(), is(1));
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
    public void assertAddSchema() throws SQLException {
        when(metaDataContexts.getMetaDataMap()).thenReturn(new LinkedHashMap<>());
        contextManager.addSchema("foo_schema");
        assertTrue(contextManager.getMetaDataContexts().getMetaDataMap().containsKey("foo_schema"));
        assertTrue(contextManager.getMetaDataContexts().getOptimizerContext().getFederationMetaData().getDatabases().containsKey("foo_schema"));
    }
    
    @Test
    public void assertAlterSchema() {
        contextManager.alterSchema("foo_schema", Collections.singletonMap("foo_schema", new ShardingSphereSchema(Collections.singletonMap("foo_table", 
                new TableMetaData("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())))));
        assertTrue(contextManager.getMetaDataContexts().getMetaDataMap().get("foo_schema").getDefaultSchema().containsTable("foo_table"));
        assertTrue(contextManager.getMetaDataContexts().getOptimizerContext().getFederationMetaData().getDatabases().containsKey("foo_schema"));
        Map<String, FederationSchemaMetaData> schemas = contextManager.getMetaDataContexts().getOptimizerContext().getFederationMetaData().getDatabases().get("foo_schema").getSchemas();
        assertTrue(schemas.get("foo_schema").getTables().containsKey("foo_table"));
    }
    
    @Test
    public void assertDeleteSchema() {
        when(metaDataContexts.getMetaDataMap()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", mock(ShardingSphereMetaData.class))));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getDatabases()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", mock(FederationDatabaseMetaData.class))));
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", mock(OptimizerParserContext.class))));
        when(metaDataContexts.getOptimizerContext().getPlannerContexts()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", mock(OptimizerPlannerContext.class))));
        contextManager.deleteSchema("foo_schema");
        assertFalse(contextManager.getMetaDataContexts().getMetaDataMap().containsKey("foo_schema"));
        assertFalse(contextManager.getMetaDataContexts().getOptimizerContext().getFederationMetaData().getDatabases().containsKey("foo_schema"));
        assertFalse(contextManager.getMetaDataContexts().getOptimizerContext().getParserContexts().containsKey("foo_schema"));
        assertFalse(contextManager.getMetaDataContexts().getOptimizerContext().getPlannerContexts().containsKey("foo_schema"));
    }
    
    @Test
    public void assertAddResource() throws SQLException {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(metaDataContexts.getMetaDataMap()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", new ShardingSphereMetaData(
                "foo_schema", resource, new ShardingSphereRuleMetaData(new LinkedList<>(), new LinkedList<>()), Collections.emptyMap()))));
        contextManager.addResource("foo_schema", createToBeAddedDataSourceProperties());
        assertAddedDataSources(contextManager.getMetaDataContexts().getMetaDataMap().get("foo_schema").getResource().getDataSources());
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
        Map<String, ShardingSphereMetaData> originalMetaDataMap = new HashMap<>(Collections.singletonMap("foo_schema", createOriginalMetaData()));
        when(metaDataContexts.getMetaDataMap()).thenReturn(originalMetaDataMap);
        contextManager.alterResource("foo_schema", Collections.singletonMap("bar_ds", new DataSourceProperties(MockedDataSource.class.getName(), createProperties("test", "test"))));
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaDataMap().get("foo_schema").getResource().getDataSources().get("bar_ds"));
    }
    
    private ShardingSphereMetaData createOriginalMetaData() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        return new ShardingSphereMetaData("foo_schema", resource, ruleMetaData, Collections.emptyMap());
    }
    
    private void assertAlteredDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getPassword(), is("test"));
        assertThat(actual.getUsername(), is("test"));
    }
    
    @Test 
    public void assertDropResource() {
        when(metaDataContexts.getMetaData("foo_schema").getResource().getDataSources()).thenReturn(new HashMap<>(Collections.singletonMap("foo_ds", new MockedDataSource())));
        contextManager.dropResource("foo_schema", Collections.singleton("foo_ds"));
        assertTrue(contextManager.getMetaDataContexts().getMetaData("foo_schema").getResource().getDataSources().isEmpty());
    }
    
    @Test
    public void assertAlterRuleConfiguration() {
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(metaDataContexts.getMetaDataMap()).thenReturn(Collections.singletonMap("foo_schema", 
                new ShardingSphereMetaData("foo_schema", resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap())));
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS)));
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        contextManager.alterRuleConfiguration("foo_schema", Collections.singleton(ruleConfig));
        assertTrue(contextManager.getMetaDataContexts().getMetaDataMap().get("foo_schema").getRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    public void assertAlterDataSourceConfiguration() {
        ShardingSphereMetaData originalMetaData = new ShardingSphereMetaData("foo_schema", createOriginalResource(), createOriginalRuleMetaData(), Collections.emptyMap());
        when(metaDataContexts.getMetaData("foo_schema")).thenReturn(originalMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(Collections.singletonMap("foo_schema", originalMetaData));
        contextManager.alterDataSourceConfiguration("foo_schema", Collections.singletonMap("foo_ds", new DataSourceProperties(MockedDataSource.class.getName(), createProperties("test", "test"))));
        assertThat(contextManager.getMetaDataContexts().getMetaDataMap().get("foo_schema").getResource().getDataSources().size(), is(1));
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaData("foo_schema").getResource().getDataSources().get("foo_ds"));
    }
    
    private ShardingSphereResource createOriginalResource() {
        ShardingSphereResource result = mock(ShardingSphereResource.class);
        Map<String, DataSource> originalDataSources = new LinkedHashMap<>(2, 1);
        originalDataSources.put("ds_1", new MockedDataSource());
        originalDataSources.put("ds_2", new MockedDataSource());
        when(result.getDataSources()).thenReturn(originalDataSources);
        when(result.getDatabaseType()).thenReturn(new MySQLDatabaseType());
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
        assertTrue(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    public void assertAlterProperties() {
        Properties props = new Properties();
        props.put("foo", "foo_value");
        contextManager.alterProperties(props);
        assertThat(contextManager.getMetaDataContexts().getProps().getProps().get("foo"), is("foo_value"));
    }
    
    @Test
    public void assertReloadMetaData() {
        when(metaDataContexts.getMetaData("foo_schema").getResource().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        SchemaMetaDataPersistService schemaMetaDataPersistService = mock(SchemaMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        when(metaDataPersistService.getSchemaMetaDataService()).thenReturn(schemaMetaDataPersistService);
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(metaDataPersistService));
        contextManager.reloadMetaData("foo_schema");
        verify(schemaMetaDataPersistService, times(1)).persist(eq("foo_schema"), eq("foo_schema"), any(ShardingSphereSchema.class));
        contextManager.reloadMetaData("foo_schema", "foo_table");
        assertNotNull(contextManager.getMetaDataContexts().getMetaData("foo_schema"));
        contextManager.reloadMetaData("foo_schema", "foo_table", "foo_ds");
        assertNotNull(contextManager.getMetaDataContexts().getMetaData("foo_schema").getDefaultSchema());
        assertTrue(contextManager.getMetaDataContexts().getMetaData("foo_schema").getResource().getDataSources().containsKey("foo_ds"));
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
