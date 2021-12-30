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

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.OptimizerParserContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
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
import org.apache.shardingsphere.schedule.core.api.ModeScheduleContext;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
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

    @Mock
    private MetaDataContexts metaDataContexts;

    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ModeScheduleContext modeScheduleContext;

    private ContextManager contextManager;

    @Before
    public void setUp() throws SQLException {
        contextManager = new ContextManager();
        contextManager.init(metaDataContexts, transactionContexts, modeScheduleContext);
        dataSourceMap = new HashMap<>(2, 1);
        DataSource primaryDataSource = mock(DataSource.class);
        DataSource replicaDataSource = mock(DataSource.class);
        dataSourceMap.put("test_primary_ds", primaryDataSource);
        dataSourceMap.put("test_replica_ds", replicaDataSource);
    }

    @SneakyThrows
    @Test
    public void assertClose() {
        contextManager.close();
        verify(metaDataContexts).close();
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
        ShardingSphereResource shardingSphereResource = new ShardingSphereResource(
                dataSourceMap,
                dataSourceMetadata,
                cachedMetadata,
                databaseType
        );
        ShardingSphereMetaData metadata = new ShardingSphereMetaData("logic_schema", shardingSphereResource, sphereRuleMetadata, new ShardingSphereSchema());
        when(metaDataContexts.getMetaData(anyString())).thenReturn(metadata);
        Map<String, DataSource> dataSourceMap = contextManager.getDataSourceMap(DefaultSchema.LOGIC_NAME);
        assertThat(2, equalTo(dataSourceMap.size()));
    }
    
    @SneakyThrows
    @Test
    public void assertAddSchema() {
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        OptimizerContext optimizerContext = mock(OptimizerContext.class, RETURNS_DEEP_STUBS);
        Map<String, FederationSchemaMetaData> federationSchemaMetaDataMap = new LinkedHashMap<>();
        when(optimizerContext.getMetaData().getSchemas()).thenReturn(federationSchemaMetaDataMap);
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
        when(optimizerContext.getMetaData().getSchemas()).thenReturn(federationSchemaMetaDataMap);
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
        when(optimizerContext.getMetaData()).thenReturn(federationMetaData);
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
        assertTrue(metaDataContexts.getOptimizerContext().getMetaData().getSchemas().get("test_schema").getTables().containsKey("test_table_1"));
    }
    
    @SneakyThrows
    @Test
    public void assertAddResource() {
        ShardingSphereResource originalResource = mock(ShardingSphereResource.class);
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        when(originalResource.getDataSources()).thenReturn(dataSources);
        List<RuleConfiguration> ruleConfigurations = new LinkedList<>();
        ShardingSphereRuleMetaData originalShardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(originalShardingSphereRuleMetaData.getConfigurations()).thenReturn(ruleConfigurations);
        ShardingSphereMetaData originalMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(originalMetaData.getName()).thenReturn("test_schema");
        when(originalMetaData.getRuleMetaData()).thenReturn(originalShardingSphereRuleMetaData);
        when(originalMetaData.getResource()).thenReturn(originalResource);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        Map<String, ShardingSphereMetaData> metaDataMap = new LinkedHashMap<>();
        metaDataMap.put("test_schema", originalMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(metaDataMap);
        Properties properties = new Properties();
        ConfigurationProperties configurationProperties = new ConfigurationProperties(properties);
        when(metaDataContexts.getProps()).thenReturn(configurationProperties);
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(globalRuleMetaData.getConfigurations()).thenReturn(new LinkedList<>());
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(mock(OptimizerContext.class));
        when(metaDataContexts.getOptimizerContext().getMetaData()).thenReturn(mock(FederationMetaData.class));
        when(metaDataContexts.getOptimizerContext().getMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new LinkedHashMap<>());
        Properties dsPropsWithJdbcUrl = new Properties();
        dsPropsWithJdbcUrl.put("jdbcUrl", "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        Map<String, DataSourceConfiguration> dataSourceConfigs = new LinkedHashMap<>();
        dataSourceConfigs.put("test_ds_1", createDataSourceConfiguration(dsPropsWithJdbcUrl));
        dataSourceConfigs.put("test_ds_2", createDataSourceConfiguration(dsPropsWithJdbcUrl));
        contextManager.addResource("test_schema", dataSourceConfigs);
        Map<String, DataSource> addedDsMap = contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources();
        assertThat(contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources().size(), is(2));
        assertTrue(addedDsMap.containsKey("test_ds_1"));
        assertTrue(addedDsMap.containsKey("test_ds_2"));
        HikariDataSource addedDs1 = (HikariDataSource) addedDsMap.get("test_ds_1");
        HikariDataSource addedDs2 = (HikariDataSource) addedDsMap.get("test_ds_2");
        assertThat(addedDs1.getJdbcUrl(), is("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(addedDs2.getJdbcUrl(), is("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(addedDs1.getUsername(), is("root"));
        assertThat(addedDs2.getUsername(), is("root"));
        assertThat(addedDs1.getPassword(), is("root"));
        assertThat(addedDs2.getPassword(), is("root"));
    }
    
    @SneakyThrows
    @Test
    public void assertAlterResource() {
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        HikariDataSource originalDS = mock(HikariDataSource.class);
        dataSources.put("test_ds", originalDS);
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
        when(metaDataContexts.getOptimizerContext().getMetaData()).thenReturn(mock(FederationMetaData.class));
        when(metaDataContexts.getOptimizerContext().getMetaData().getSchemas()).thenReturn(new LinkedHashMap<>());
        when(metaDataContexts.getOptimizerContext().getParserContexts()).thenReturn(new LinkedHashMap<>());
        Properties dsProps = new Properties();
        dsProps.put("jdbcUrl", "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dsProps.put("username", "test");
        dsProps.put("password", "test");
        Map<String, DataSourceConfiguration> dataSourceConfigs = new LinkedHashMap<>();
        dataSourceConfigs.put("test_ds", createDataSourceConfiguration(dsProps));
        contextManager.alterResource("test_schema", dataSourceConfigs);
        HikariDataSource actualDs = (HikariDataSource) contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources().get("test_ds");
        assertNotNull(actualDs);
        assertThat(actualDs.getJdbcUrl(), is("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actualDs.getPassword(), is("test"));
        assertThat(actualDs.getUsername(), is("test"));
        assertThat(actualDs.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actualDs.getMaximumPoolSize(), is(50));
        assertThat(actualDs.getMinimumIdle(), is(1));
        assertThat(actualDs.getMaxLifetime(), is(60000L));
    }
    
    @Test 
    public void assertDropResource() {
        ShardingSphereMetaData originalMetaData = mock(ShardingSphereMetaData.class);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        ShardingSphereResource originalResource = mock(ShardingSphereResource.class);
        when(originalMetaData.getResource()).thenReturn(originalResource);
        Map<String, DataSource> originalDsMap = new LinkedHashMap<>();
        originalDsMap.put("ds_1", mock(DataSource.class));
        when(originalResource.getDataSources()).thenReturn(originalDsMap);
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
        when(optimizerContext.getMetaData()).thenReturn(federationMetaData);
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
    public void assertAlterDataSourceConfiguration() {
        ShardingSphereMetaData originalMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(originalMetaData.getName()).thenReturn("test_schema");
        Map<String, ShardingSphereMetaData> originalMetaDataMap = new LinkedHashMap<>();
        originalMetaDataMap.put("test_schema", originalMetaData);
        when(metaDataContexts.getMetaDataMap()).thenReturn(originalMetaDataMap);
        Map<String, DataSource> originalDataSources = new LinkedHashMap<>();
        HikariDataSource ds1 = mock(HikariDataSource.class, RETURNS_DEEP_STUBS);
        HikariDataSource ds2 = mock(HikariDataSource.class, RETURNS_DEEP_STUBS);
        when(ds2.getHikariPoolMXBean()).thenReturn(mock(HikariPoolMXBean.class));
        when(ds1.getHikariPoolMXBean()).thenReturn(mock(HikariPoolMXBean.class));
        originalDataSources.put("test_ds_1", ds1);
        originalDataSources.put("test_ds_2", ds2);
        ShardingSphereResource originalShardingSphereResource = mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS);
        when(originalShardingSphereResource.getDataSources()).thenReturn(originalDataSources);
        when(originalMetaData.getResource()).thenReturn(originalShardingSphereResource);
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS);
        List<RuleConfiguration> ruleConfigurations = new LinkedList<>();
        ruleConfigurations.add(mock(RuleConfiguration.class));
        when(shardingSphereRuleMetaData.getConfigurations()).thenReturn(ruleConfigurations);
        when(originalMetaData.getRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        ShardingSphereRuleMetaData globalShardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        List<RuleConfiguration> globalRuleConfigurations = new LinkedList<>();
        globalRuleConfigurations.add(mock(RuleConfiguration.class));
        when(metaDataContexts.getGlobalRuleMetaData()).thenReturn(globalShardingSphereRuleMetaData);
        ConfigurationProperties configurationProperties = new ConfigurationProperties(new Properties());
        when(metaDataContexts.getProps()).thenReturn(configurationProperties);
        OptimizerContext optimizerContext = mock(OptimizerContext.class, RETURNS_DEEP_STUBS);
        FederationMetaData federationMetaData = mock(FederationMetaData.class);
        Map<String, FederationSchemaMetaData> federationSchemaMetaDataMap = new LinkedHashMap<>();
        when(federationMetaData.getSchemas()).thenReturn(federationSchemaMetaDataMap);
        when(optimizerContext.getMetaData()).thenReturn(federationMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(optimizerContext);
        when(metaDataContexts.getMetaData("test_schema")).thenReturn(originalMetaData);
        Map<String, DataSourceConfiguration> newDataSourceConfigurations = new LinkedHashMap<>();
        Properties dsProps = new Properties();
        dsProps.put("username", "test");
        dsProps.put("password", "test");
        newDataSourceConfigurations.put("test_ds_1", createDataSourceConfiguration(dsProps));
        contextManager.alterDataSourceConfiguration("test_schema", newDataSourceConfigurations);
        assertTrue(contextManager.getMetaDataContexts().getMetaDataMap().containsKey("test_schema"));
        assertThat(contextManager.getMetaDataContexts().getMetaDataMap().get("test_schema").getResource().getDataSources().size(), is(1));
        HikariDataSource actualDs = (HikariDataSource) contextManager.getMetaDataContexts().getMetaData("test_schema").getResource().getDataSources().get("test_ds_1");
        assertThat(actualDs.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actualDs.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actualDs.getPassword(), is("test"));
        assertThat(actualDs.getUsername(), is("test"));
        assertThat(actualDs.getMaximumPoolSize(), is(50));
        assertThat(actualDs.getMinimumIdle(), is(1));
        assertThat(actualDs.getMaxLifetime(), is(60000L));
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
        Properties newProperties = new Properties();
        newProperties.put("test1", "test1_value");
        contextManager.alterProps(newProperties);
        assertEquals("test1_value", contextManager.getMetaDataContexts().getProps().getProps().get("test1"));
    }
    
    @SneakyThrows
    @Test
    public void assertReloadMetaData() {
        HikariDataSource testDs = mock(HikariDataSource.class, RETURNS_DEEP_STUBS);
        when(testDs.getConnection()).thenReturn(mock(Connection.class));
        when(testDs.getConnection().getMetaData()).thenReturn(mock(DatabaseMetaData.class));
        when(testDs.getConnection().getMetaData().getURL()).thenReturn("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        Map<String, DataSource> dataSources = new LinkedHashMap<>();
        dataSources.put("test_ds", testDs);
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
        when(optimizerContext.getMetaData()).thenReturn(federationMetaData);
        when(metaDataContexts.getOptimizerContext()).thenReturn(optimizerContext);
        SchemaMetaDataPersistService schemaMetaDataPersistService = mock(SchemaMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        when(metaDataPersistService.getSchemaMetaDataService()).thenReturn(schemaMetaDataPersistService);
        when(metaDataContexts.getMetaDataPersistService()).thenReturn(Optional.of(metaDataPersistService));  
        contextManager.reloadMetaData("test_schema");
        verify(schemaMetaDataPersistService, times(1)).persist(eq("test_schema"), any(ShardingSphereSchema.class));
        contextManager.reloadMetaData("test_schema", "test_table");
        verify(schemaMetaDataPersistService, times(2)).persist(eq("test_schema"), any(ShardingSphereSchema.class));
        assertTrue(contextManager.getMetaDataContexts().getMetaData("test_schema").getSchema().containsTable("test_table"));
        contextManager.reloadMetaData("test_schema", "test_table", "test_ds");
        verify(schemaMetaDataPersistService, times(3)).persist(eq("test_schema"), any(ShardingSphereSchema.class));
        assertTrue(contextManager.getMetaDataContexts().getMetaData("test_schema").getSchema().containsTable("test_table"));
        assertTrue(contextManager.getMetaDataContexts().getMetaData("test_schema").getResource().getDataSources().containsKey("test_ds"));
    }
    
    private DataSourceConfiguration createDataSourceConfiguration(final Properties properties) {
        Map<String, Object> props = (properties == null || properties.size() == 0) ? new HashMap(16, 1) : new HashMap(properties);
        props.putIfAbsent("driverClassName", "org.h2.Driver");
        props.putIfAbsent("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        props.putIfAbsent("username", "root");
        props.putIfAbsent("password", "root");
        props.putIfAbsent("maximumPoolSize", "50");
        props.putIfAbsent("minimumIdle", "1");
        props.putIfAbsent("maxLifetime", "60000");
        props.putIfAbsent("test", "test");
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProps().putAll(props);
        return result;
    }
}
