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
import org.apache.shardingsphere.infra.config.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

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
        DataSourcesMetaData dataSourceMetadata = mock(DataSourcesMetaData.class);
        CachedDatabaseMetaData cachedMetadata = mock(CachedDatabaseMetaData.class);
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereRuleMetaData sphereRuleMetadata = mock(ShardingSphereRuleMetaData.class);
        ShardingSphereResource resource = new ShardingSphereResource(dataSourceMap, dataSourceMetadata, cachedMetadata, databaseType);
        ShardingSphereMetaData metadata = new ShardingSphereMetaData("logic_schema", resource, sphereRuleMetadata, new ShardingSphereSchema());
        when(metaDataContexts.getMetaData(anyString())).thenReturn(metadata);
        Map<String, DataSource> dataSourceMap = contextManager.getDataSourceMap(DefaultSchema.LOGIC_NAME);
        assertThat(dataSourceMap.size(), is(2));
    }
    
    @Test
    public void assertAddSchema() throws SQLException {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        OptimizerContext optimizerContext = mock(OptimizerContext.class, RETURNS_DEEP_STUBS);
        Map<String, FederationSchemaMetaData> federationSchemaMetaDataMap = new LinkedHashMap<>();
        when(optimizerContext.getFederationMetaData().getSchemas()).thenReturn(federationSchemaMetaDataMap);
        ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class);
        when(metaDataContexts.getProps()).thenReturn(configurationProperties);
        when(metaDataContexts.getOptimizerContext()).thenReturn(optimizerContext);
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        contextManager.addSchema("test_add_schema");
        assertTrue(federationSchemaMetaDataMap.containsKey("test_add_schema"));
        assertTrue(metaDataMap.containsKey("test_add_schema"));
    }
    
    @Test
    public void assertDeleteSchema() {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        metaDataMap.put("test_delete_schema", shardingSphereMetaData);
        FederationSchemaMetaData federationSchemaMetaData = mock(FederationSchemaMetaData.class);
        Map<String, FederationSchemaMetaData> federationSchemaMetaDataMap = new LinkedHashMap<>();
        federationSchemaMetaDataMap.put("test_delete_schema", federationSchemaMetaData);
        Map<String, OptimizerParserContext> parserContexts = new LinkedHashMap<>();
        OptimizerParserContext optimizerParserContext = mock(OptimizerParserContext.class);
        parserContexts.put("test_delete_schema", optimizerParserContext);
        Map<String, OptimizerPlannerContext> plannerContexts = new LinkedHashMap<>();
        OptimizerPlannerContext optimizerPlannerContext = mock(OptimizerPlannerContext.class);
        plannerContexts.put("test_delete_schema", optimizerPlannerContext);
        OptimizerContext optimizerContext = mock(OptimizerContext.class, RETURNS_DEEP_STUBS);
        when(optimizerContext.getFederationMetaData().getSchemas()).thenReturn(federationSchemaMetaDataMap);
        when(optimizerContext.getPlannerContexts()).thenReturn(plannerContexts);
        when(optimizerContext.getParserContexts()).thenReturn(parserContexts);
        when(metaDataContexts.getOptimizerContext()).thenReturn(optimizerContext);
        contextManager.deleteSchema("test_delete_schema");
        assertFalse(metaDataMap.containsKey("test_delete_schema"));
        assertFalse(federationSchemaMetaDataMap.containsKey("test_delete_schema"));
        assertFalse(parserContexts.containsKey("test_delete_schema"));
        assertFalse(plannerContexts.containsKey("test_delete_schema"));
    }
    
    @Test
    public void assertAlterSchema() {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        metaDataMap.put("test_schema", shardingSphereMetaData);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(shardingSphereMetaData);
        OptimizerContext optimizerContext = mock(OptimizerContext.class, RETURNS_DEEP_STUBS);
        FederationMetaData federationMetaData = mock(FederationMetaData.class);
        when(optimizerContext.getFederationMetaData()).thenReturn(federationMetaData);
        Map<String, FederationSchemaMetaData> federationSchemaMetaDataMap = new LinkedHashMap<>();
        when(federationMetaData.getSchemas()).thenReturn(federationSchemaMetaDataMap);
        when(metaDataContexts.getOptimizerContext()).thenReturn(optimizerContext);
        Map<String, TableMetaData> tables = new ConcurrentHashMap<>();
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(shardingSphereSchema.getTables()).thenReturn(tables);
        TableMetaData testTableMetaData = mock(TableMetaData.class, RETURNS_DEEP_STUBS);
        when(testTableMetaData.getName()).thenReturn("test_table_1");
        tables.put("test_table_1", testTableMetaData);
        contextManager.alterSchema("test_schema", shardingSphereSchema);
        assertTrue(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas().get("test_schema").getTables().containsKey("test_table_1"));
    }
    
    @Test
    public void assertAddResource() throws SQLException {
        mockMetaDataContextsForAddResource();
        contextManager.addResource("test_schema", createToBeAddedDataSourceProperties());
        assertAddedDataSources(contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources());
    }
    
    private void mockMetaDataContextsForAddResource() {
        ShardingSphereMetaData originalMetaData = mockOriginalMetaData();
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        metaDataMap.put("test_schema", originalMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(globalRuleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(mock(OptimizerContext.class));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData()).thenReturn(mock(FederationMetaData.class));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new LinkedHashMap<>());
    }
    
    private ShardingSphereMetaData mockOriginalMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("test_schema");
        when(result.getRuleMetaData().getConfigurations()).thenReturn(new LinkedList<>());
        when(result.getResource().getDataSources()).thenReturn(new LinkedHashMap<>());
        return result;
    }
    
    private Map<String, DataSourceProperties> createToBeAddedDataSourceProperties() {
        Properties dataSourceProps = new Properties();
        dataSourceProps.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        Map<String, DataSourceProperties> result = new LinkedHashMap<>();
        result.put("test_ds_1", new DataSourceProperties(MockedDataSource.class.getName(), createProperties(dataSourceProps)));
        result.put("test_ds_2", new DataSourceProperties(MockedDataSource.class.getName(), createProperties(dataSourceProps)));
        return result;
    }
    
    private void assertAddedDataSources(final Map<String, DataSource> actual) {
        assertThat(contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources().size(), is(2));
        assertTrue(actual.containsKey("test_ds_1"));
        assertDataSource((MockedDataSource) actual.get("test_ds_1"));
        assertTrue(actual.containsKey("test_ds_2"));
        assertDataSource((MockedDataSource) actual.get("test_ds_2"));
    }
    
    @Test
    public void assertAlterResource() throws SQLException {
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        dataSources.put("original_ds", new MockedDataSource());
        ShardingSphereResource originalResource = mock(ShardingSphereResource.class);
        when(originalResource.getDataSources()).thenReturn(dataSources);
        ShardingSphereRuleMetaData originalShardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        List<RuleConfiguration> ruleConfigurations = new LinkedList<>();
        when(originalShardingSphereRuleMetaData.getConfigurations()).thenReturn(ruleConfigurations);
        ShardingSphereMetaData originalMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(originalMetaData.getName()).thenReturn("test_schema");
        when(originalMetaData.getRuleMetaData()).thenReturn(originalShardingSphereRuleMetaData);
        when(originalMetaData.getResource()).thenReturn(originalResource);
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        metaDataMap.put("test_schema", originalMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        Properties properties = new Properties();
        ConfigurationProperties configurationProperties = new ConfigurationProperties(properties);
        when(metaDataContexts.getProps()).thenReturn(configurationProperties);
        ShardingSphereRuleMetaData globalShardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        List<RuleConfiguration> globalRuleConfigurations = new LinkedList<>();
        when(globalShardingSphereRuleMetaData.getConfigurations()).thenReturn(globalRuleConfigurations);
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(globalShardingSphereRuleMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(mock(OptimizerContext.class));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData()).thenReturn(mock(FederationMetaData.class));
        when(metaDataContexts.getOptimizerContext().getFederationMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new LinkedHashMap<>());
        contextManager.alterResource("test_schema", createToBeAlteredDataSourceProperties());
        assertAlteredDataSource((MockedDataSource) contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources().get("test_ds"));
    }
    
    private Map<String, DataSourceProperties> createToBeAlteredDataSourceProperties() {
        Properties dataSourceProps = new Properties();
        dataSourceProps.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        dataSourceProps.put("username", "test");
        dataSourceProps.put("password", "test");
        Map<String, DataSourceProperties> result = new LinkedHashMap<>();
        result.put("test_ds", new DataSourceProperties(MockedDataSource.class.getName(), createProperties(dataSourceProps)));
        return result;
    }
    
    private void assertAlteredDataSource(final MockedDataSource actual) {
        assertNotNull(actual);
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getPassword(), is("test"));
        assertThat(actual.getUsername(), is("test"));
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getCanonicalName()));
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
        List<String> dsNamesToBeDropped = new LinkedList<>();
        dsNamesToBeDropped.add("ds_1");
        contextManager.dropResource("test_schema", dsNamesToBeDropped);
        assertThat(contextManager.getMetaDataContexts().getMetaData("test_schema").getResource().getDataSources().size(), is(0));
    }
    
    @Test
    public void assertAlterRuleConfiguration() {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(shardingSphereMetaData.getName()).thenReturn("test");
        metaDataMap.put("test", shardingSphereMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(metaDataPersistService));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        OptimizerContext optimizerContext = mock(OptimizerContext.class, RETURNS_DEEP_STUBS);
        FederationMetaData federationMetaData = mock(FederationMetaData.class);
        Map<String, FederationSchemaMetaData> federationSchemaMetaDataMap = new LinkedHashMap<>();
        when(federationMetaData.getSchemas()).thenReturn(federationSchemaMetaDataMap);
        when(optimizerContext.getFederationMetaData()).thenReturn(federationMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(optimizerContext);
        ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class, RETURNS_DEEP_STUBS);
        Properties properties = new Properties();
        when(configurationProperties.getProps()).thenReturn(properties);
        when(metaDataContexts.getProps()).thenReturn(configurationProperties);
        Collection<RuleConfiguration> ruleConfigs = new ArrayList<>();
        RuleConfiguration ruleConfiguration = mock(RuleConfiguration.class);
        ruleConfigs.add(ruleConfiguration);
        contextManager.alterRuleConfiguration("test", ruleConfigs);
        assertTrue(contextManager.getMetaDataContexts().getMetaDataMap().get("test").getRuleMetaData().getConfigurations().contains(ruleConfiguration));
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
        assertThat(actualDs.getDriverClassName(), is(MockedDataSource.class.getCanonicalName()));
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
        RuleConfiguration ruleConfiguration = mock(RuleConfiguration.class);
        newRuleConfigs.add(ruleConfiguration);
        contextManager.alterGlobalRuleConfiguration(newRuleConfigs);
        assertTrue(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getConfigurations().contains(ruleConfiguration));
    }
    
    @Test
    public void assertAlterProps() {
        Properties newProps = new Properties();
        newProps.put("test1", "test1_value");
        contextManager.alterProps(newProps);
        assertThat(contextManager.getMetaDataContexts().getProps().getProps().get("test1"), is("test1_value"));
    }
    
    @Test
    public void assertReloadMetaData() throws SQLException {
        MockedDataSource testDataSource = mock(MockedDataSource.class, RETURNS_DEEP_STUBS);
        when(testDataSource.getConnection()).thenReturn(mock(Connection.class));
        when(testDataSource.getConnection().getMetaData()).thenReturn(mock(DatabaseMetaData.class));
        when(testDataSource.getConnection().getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        dataSources.put("test_ds", testDataSource);
        ShardingSphereResource originalResource = mock(ShardingSphereResource.class);
        when(originalResource.getDataSources()).thenReturn(dataSources);
        ShardingSphereRuleMetaData originalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(originalRuleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        ShardingSphereMetaData originalMetaData = mock(ShardingSphereMetaData.class);
        when(originalMetaData.getResource()).thenReturn(originalResource);
        when(originalMetaData.getRuleMetaData()).thenReturn(originalRuleMetaData);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        ConfigurationProperties configurationProperties = new ConfigurationProperties(new Properties());
        when(metaDataContexts.getProps()).thenReturn(configurationProperties);
        FederationMetaData federationMetaData = mock(FederationMetaData.class);
        when(federationMetaData.getSchemas()).thenReturn(new LinkedHashMap<>());
        OptimizerContext optimizerContext = mock(OptimizerContext.class);
        when(optimizerContext.getFederationMetaData()).thenReturn(federationMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(optimizerContext);
        SchemaMetaDataPersistService schemaMetaDataPersistService = mock(SchemaMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        when(metaDataPersistService.getSchemaMetaDataService()).thenReturn(schemaMetaDataPersistService);
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(metaDataPersistService));  
        contextManager.reloadMetaData("test_schema");
        verify(schemaMetaDataPersistService, times(1)).persist(eq("test_schema"), any(ShardingSphereSchema.class));
        contextManager.reloadMetaData("test_schema", "test_table");
        assertNotNull(contextManager.getMetaDataContexts().getMetaData("test_schema"));
        contextManager.reloadMetaData("test_schema", "test_table", "test_ds");
        assertNotNull(contextManager.getMetaDataContexts().getMetaData("test_schema").getSchema());
        assertTrue(contextManager.getMetaDataContexts().getMetaData("test_schema").getResource().getDataSources().containsKey("test_ds"));
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
