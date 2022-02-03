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
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationMetaData;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ContextManagerTest {
    
    private static Map<String, DataSource> dataSourceMap;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private InstanceContext instanceContext;
    
    private ContextManager contextManager;
    
    @Before
    public void setUp() throws SQLException {
        contextManager = new ContextManager();
        contextManager.init(metaDataContexts, transactionContexts, instanceContext);
        dataSourceMap = new HashMap<>(2, 1);
        DataSource primaryDataSource = mock(DataSource.class);
        DataSource replicaDataSource = mock(DataSource.class);
        dataSourceMap.put("test_primary_ds", primaryDataSource);
        dataSourceMap.put("test_replica_ds", replicaDataSource);
    }
    
    @Test
    public void assertRenewMetaDataContexts() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        contextManager.renewMetaDataContexts(metaDataContexts);
        assertThat(contextManager.getMetaDataContexts(), is(metaDataContexts));
    }
    
    @Test
    public void assertRenewTransactionContexts() {
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        contextManager.renewTransactionContexts(transactionContexts);
        assertThat(contextManager.getTransactionContexts(), is(transactionContexts));
    }
    
    @Test
    public void assertGetDataSourceMap() {
        ShardingSphereResource resource = new ShardingSphereResource(dataSourceMap, mock(DataSourcesMetaData.class), mock(CachedDatabaseMetaData.class), mock(DatabaseType.class));
        when(metaDataContexts.getMetaData(anyString())).thenReturn(new ShardingSphereMetaData("logic_schema", resource, mock(ShardingSphereRuleMetaData.class), new ShardingSphereSchema()));
        assertThat(contextManager.getDataSourceMap(DefaultSchema.LOGIC_NAME).size(), is(2));
    }
    
    @Test
    public void assertAddSchema() throws SQLException {
        when(metaDataContexts.getMetaDataMap()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getProps()).thenReturn(mock(ConfigurationProperties.class));
        contextManager.addSchema("test_add_schema");
        assertTrue(metaDataContexts.getMetaDataMap().containsKey("test_add_schema"));
        assertTrue(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas().containsKey("test_add_schema"));
    }
    
    @Test
    public void assertDeleteSchema() {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        metaDataMap.put("test_delete_schema", mock(ShardingSphereMetaData.class));
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        FederationSchemaMetaData federationSchemaMetaData = mock(FederationSchemaMetaData.class);
        Map<String, FederationSchemaMetaData> federationSchemaMetaDataMap = new LinkedHashMap<>();
        federationSchemaMetaDataMap.put("test_delete_schema", federationSchemaMetaData);
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(federationSchemaMetaDataMap);
        Map<String, OptimizerParserContext> parserContexts = new LinkedHashMap<>();
        parserContexts.put("test_delete_schema", mock(OptimizerParserContext.class));
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(parserContexts);
        Map<String, OptimizerPlannerContext> plannerContexts = new LinkedHashMap<>();
        plannerContexts.put("test_delete_schema", mock(OptimizerPlannerContext.class));
        when(metaDataContexts.getOptimizerContext().getPlannerContexts()).thenReturn(plannerContexts);
        contextManager.deleteSchema("test_delete_schema");
        assertFalse(metaDataContexts.getMetaDataMap().containsKey("test_delete_schema"));
        assertFalse(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas().containsKey("test_delete_schema"));
        assertFalse(parserContexts.containsKey("test_delete_schema"));
        assertFalse(plannerContexts.containsKey("test_delete_schema"));
    }
    
    @Test
    public void assertAlterSchema() {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        metaDataMap.put("test_schema", shardingSphereMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(shardingSphereMetaData);
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        Map<String, TableMetaData> tables = new HashMap<>();
        tables.put("test_table_1", new TableMetaData("test_table_1", Collections.emptyList(), Collections.emptyList()));
        contextManager.alterSchema("test_schema", new ShardingSphereSchema(tables));
        assertTrue(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas().get("test_schema").getTables().containsKey("test_table_1"));
    }
    
    @Test
    public void assertAddResource() throws SQLException {
        ShardingSphereMetaData originalMetaData = mockOriginalMetaData();
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        metaDataMap.put("test_schema", originalMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(globalRuleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(mock(OptimizerContext.class));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData()).thenReturn(mock(FederationMetaData.class));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        contextManager.addResource("test_schema", createToBeAddedDataSourceProperties());
        assertAddedDataSources(contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources());
    }
    
    private ShardingSphereMetaData mockOriginalMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("test_schema");
        when(result.getResource().getDataSources()).thenReturn(new LinkedHashMap<>());
        when(result.getRuleMetaData().getConfigurations()).thenReturn(new LinkedList<>());
        return result;
    }
    
    private void assertAddedDataSources(final Map<String, DataSource> actual) {
        assertThat(contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources().size(), is(2));
        assertTrue(actual.containsKey("foo_ds_1"));
        assertDataSource((MockedDataSource) actual.get("foo_ds_1"));
        assertTrue(actual.containsKey("foo_ds_2"));
        assertDataSource((MockedDataSource) actual.get("foo_ds_2"));
    }
    
    private Map<String, DataSourceProperties> createToBeAddedDataSourceProperties() {
        Properties dataSourceProps = new Properties();
        dataSourceProps.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        Map<String, DataSourceProperties> result = new LinkedHashMap<>();
        result.put("foo_ds_1", new DataSourceProperties(MockedDataSource.class.getName(), createProperties(dataSourceProps)));
        result.put("foo_ds_2", new DataSourceProperties(MockedDataSource.class.getName(), createProperties(dataSourceProps)));
        return result;
    }
    
    @Test
    public void assertAlterResource() throws SQLException {
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        dataSources.put("original_ds", new MockedDataSource());
        ShardingSphereResource originalResource = mock(ShardingSphereResource.class);
        when(originalResource.getDataSources()).thenReturn(dataSources);
        ShardingSphereRuleMetaData originalShardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(originalShardingSphereRuleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        ShardingSphereMetaData originalMetaData = new ShardingSphereMetaData("test_schema", originalResource, originalShardingSphereRuleMetaData, mock(ShardingSphereSchema.class));
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        metaDataMap.put("test_schema", originalMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        ShardingSphereRuleMetaData globalShardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(globalShardingSphereRuleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(globalShardingSphereRuleMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(mock(OptimizerContext.class));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData()).thenReturn(mock(FederationMetaData.class));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        contextManager.alterResource("test_schema", createToBeAlteredDataSourceProperties());
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources().get("foo_ds"));
    }
    
    private Map<String, DataSourceProperties> createToBeAlteredDataSourceProperties() {
        Properties dataSourceProps = new Properties();
        dataSourceProps.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        dataSourceProps.put("username", "test");
        dataSourceProps.put("password", "test");
        Map<String, DataSourceProperties> result = new LinkedHashMap<>();
        result.put("foo_ds", new DataSourceProperties(MockedDataSource.class.getName(), createProperties(dataSourceProps)));
        return result;
    }
    
    private void assertAlteredDataSource(final MockedDataSource actual) {
        assertNotNull(actual);
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getPassword(), is("test"));
        assertThat(actual.getUsername(), is("test"));
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getName()));
    }
    
    @Test 
    public void assertDropResource() {
        ShardingSphereMetaData originalMetaData = mock(ShardingSphereMetaData.class);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        ShardingSphereResource originalResource = mock(ShardingSphereResource.class);
        when(originalMetaData.getResource()).thenReturn(originalResource);
        Map<String, DataSource> originalDataSources = new LinkedHashMap<>();
        originalDataSources.put("ds_1", new MockedDataSource());
        when(originalResource.getDataSources()).thenReturn(originalDataSources);
        contextManager.dropResource("test_schema", Collections.singleton("ds_1"));
        assertTrue(contextManager.getMetaDataContexts().getMetaData("test_schema").getResource().getDataSources().isEmpty());
    }
    
    @Test
    public void assertAlterRuleConfiguration() {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        ShardingSphereMetaData shardingSphereMetaData = new ShardingSphereMetaData(
                "test", mock(ShardingSphereResource.class), mock(ShardingSphereRuleMetaData.class), mock(ShardingSphereSchema.class));
        metaDataMap.put("test", shardingSphereMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS)));
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        ruleConfigs.add(ruleConfig);
        contextManager.alterRuleConfiguration("test", ruleConfigs);
        assertTrue(contextManager.getMetaDataContexts().getMetaDataMap().get("test").getRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    @Ignore
    // TODO fix test cases
    public void assertAlterDataSourceConfiguration() {
        ShardingSphereMetaData originalMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(originalMetaData.getName()).thenReturn("test_schema");
        ShardingSphereResource originalResource = mockOriginalResource();
        when(originalMetaData.getResource()).thenReturn(originalResource);
        ShardingSphereRuleMetaData ruleMetaData = mockRuleMetaData();
        when(originalMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class));
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        Map<String, ShardingSphereMetaData> originalMetaDataMap = new LinkedHashMap<>();
        originalMetaDataMap.put("test_schema", originalMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(originalMetaDataMap);
        Map<String, DataSourceProperties> newDataSourceProps = new LinkedHashMap<>();
        Properties dataSourceProps = new Properties();
        dataSourceProps.put("username", "test");
        dataSourceProps.put("password", "test");
        newDataSourceProps.put("ds_1", new DataSourceProperties(MockedDataSource.class.getName(), createProperties(dataSourceProps)));
        contextManager.alterDataSourceConfiguration("test_schema", newDataSourceProps);
        assertTrue(contextManager.getMetaDataContexts().getMetaDataMap().containsKey("test_schema"));
        assertThat(contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources().size(), is(1));
        MockedDataSource actualDs = (MockedDataSource) contextManager.getMetaDataContexts().getMetaData("test_schema").getResource().getDataSources().get("ds_1");
        assertThat(actualDs.getDriverClassName(), is(MockedDataSource.class.getName()));
        assertThat(actualDs.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actualDs.getPassword(), is("test"));
        assertThat(actualDs.getUsername(), is("test"));
    }
    
    private ShardingSphereResource mockOriginalResource() {
        ShardingSphereResource result = mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> originalDataSources = new LinkedHashMap<>();
        originalDataSources.put("ds_1", new MockedDataSource());
        originalDataSources.put("ds_2", new MockedDataSource());
        when(result.getDataSources()).thenReturn(originalDataSources);
        return result;
    }
    
    private ShardingSphereRuleMetaData mockRuleMetaData() {
        ShardingSphereRuleMetaData result = mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS);
        List<RuleConfiguration> ruleConfigs = new LinkedList<>();
        ruleConfigs.add(mock(RuleConfiguration.class));
        when(result.getConfigurations()).thenReturn(ruleConfigs);
        return result;
    }
    
    @Test
    public void assertAlterGlobalRuleConfiguration() {
        List<RuleConfiguration> newRuleConfigs = new LinkedList<>();
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        newRuleConfigs.add(ruleConfig);
        contextManager.alterGlobalRuleConfiguration(newRuleConfigs);
        assertTrue(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getConfigurations().contains(ruleConfig));
    }
    
    @Test
    public void assertAlterProps() {
        Properties newProps = new Properties();
        newProps.put("test1", "test1_value");
        contextManager.alterProps(newProps);
        assertThat(contextManager.getMetaDataContexts().getProps().getProps().get("test1"), is("test1_value"));
    }
    
    @Test
    public void assertReloadMetaData() {
        ShardingSphereResource originalResource = mock(ShardingSphereResource.class);
        when(originalResource.getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        when(metaDataContexts.getMetaData("test_schema").getResource()).thenReturn(originalResource);
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        SchemaMetaDataPersistService schemaMetaDataPersistService = mock(SchemaMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        when(metaDataPersistService.getSchemaMetaDataService()).thenReturn(schemaMetaDataPersistService);
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(metaDataPersistService));
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        contextManager.reloadMetaData("test_schema");
        verify(schemaMetaDataPersistService, times(1)).persist(eq("test_schema"), any(ShardingSphereSchema.class));
        contextManager.reloadMetaData("test_schema", "test_table");
        assertNotNull(contextManager.getMetaDataContexts().getMetaData("test_schema"));
        contextManager.reloadMetaData("test_schema", "test_table", "foo_ds");
        assertNotNull(contextManager.getMetaDataContexts().getMetaData("test_schema").getSchema());
        assertTrue(contextManager.getMetaDataContexts().getMetaData("test_schema").getResource().getDataSources().containsKey("foo_ds"));
    }
    
    private Map<String, Object> createProperties(final Properties dataSourceProps) {
        Map<String, Object> result = new HashMap(dataSourceProps);
        result.putIfAbsent("driverClassName", MockedDataSource.class.getName());
        result.putIfAbsent("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.putIfAbsent("username", "root");
        result.putIfAbsent("password", "root");
        return result;
    }
    
    private void assertDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    @Test
    public void assertClose() throws Exception {
        contextManager.close();
        verify(metaDataContexts).close();
    }
}
