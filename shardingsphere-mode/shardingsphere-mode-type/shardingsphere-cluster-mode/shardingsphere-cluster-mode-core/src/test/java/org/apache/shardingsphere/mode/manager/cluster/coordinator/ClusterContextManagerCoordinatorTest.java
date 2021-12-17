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

package org.apache.shardingsphere.mode.manager.cluster.coordinator;

import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.authority.event.AuthorityChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ClusterContextManagerCoordinatorTest {
    
    private ClusterContextManagerCoordinator coordinator;
    
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistService metaDataPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    @SneakyThrows
    @Before
    public void setUp() {
        PersistRepositoryConfiguration persistRepositoryConfiguration = new ClusterPersistRepositoryConfiguration("TEST", "", "", new Properties());
        ModeConfiguration configuration = new ModeConfiguration("Cluster", persistRepositoryConfiguration, false);
        ClusterContextManagerBuilder builder = new ClusterContextManagerBuilder();
        contextManager = builder.build(configuration, new HashMap<>(), new HashMap<>(), new LinkedList<>(), new Properties(), false, null, null);
        contextManager.renewMetaDataContexts(new MetaDataContexts(contextManager.getMetaDataContexts().getMetaDataPersistService().get(), createMetaDataMap(), globalRuleMetaData, 
                mock(ExecutorEngine.class),
                new ConfigurationProperties(new Properties()), mock(OptimizerContext.class, RETURNS_DEEP_STUBS)));
        contextManager.renewTransactionContexts(mock(TransactionContexts.class, RETURNS_DEEP_STUBS));
        coordinator = new ClusterContextManagerCoordinator(metaDataPersistService, contextManager);
    }
    
    @Test
    public void assertSchemaAdd() throws SQLException {
        SchemaAddedEvent event = new SchemaAddedEvent("schema_add");
        when(metaDataPersistService.getDataSourceService().load("schema_add")).thenReturn(getDataSourceConfigurations());
        when(metaDataPersistService.getSchemaRuleService().load("schema_add")).thenReturn(Collections.emptyList());
        coordinator.renew(event);
        assertNotNull(contextManager.getMetaDataContexts().getMetaData("schema_add"));
        assertNotNull(contextManager.getMetaDataContexts().getMetaData("schema_add").getResource().getDataSources());
    }
    
    private Map<String, DataSourceConfiguration> getDataSourceConfigurations() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourceConverter.getDataSourceConfiguration(dataSource));
        result.put("ds_0", DataSourceConverter.getDataSourceConfiguration(dataSource));
        result.put("ds_1", DataSourceConverter.getDataSourceConfiguration(dataSource));
        return result;
    }
    
    @Test
    public void assertSchemaDelete() {
        SchemaDeletedEvent event = new SchemaDeletedEvent("schema");
        coordinator.renew(event);
        assertNull(contextManager.getMetaDataContexts().getMetaData("schema"));
    }
    
    @Test
    public void assertPropertiesChanged() {
        Properties properties = new Properties();
        properties.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        PropertiesChangedEvent event = new PropertiesChangedEvent(properties);
        coordinator.renew(event);
        assertThat(contextManager.getMetaDataContexts().getProps().getProps().getProperty(ConfigurationPropertyKey.SQL_SHOW.getKey()), is("true"));
    }
    
    @Test
    public void assertSchemaChanged() {
        SchemaChangedEvent event = new SchemaChangedEvent("schema", mock(ShardingSphereSchema.class));
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData("schema");
        coordinator.renew(event);
        assertTrue(contextManager.getMetaDataContexts().getAllSchemaNames().contains("schema"));
        assertThat(contextManager.getMetaDataContexts().getMetaData("schema"), not(metaData));
    }
    
    @Test
    public void assertSchemaChangedWithExistSchema() {
        SchemaChangedEvent event = new SchemaChangedEvent("schema", mock(ShardingSphereSchema.class));
        coordinator.renew(event);
        assertThat(contextManager.getMetaDataContexts().getMetaData("schema"), not(metaData));
    }
    
    @Test
    public void assertRuleConfigurationsChanged() {
        assertThat(contextManager.getMetaDataContexts().getMetaData("schema"), is(metaData));
        RuleConfigurationsChangedEvent event = new RuleConfigurationsChangedEvent("schema", new LinkedList<>());
        coordinator.renew(event);
        assertThat(contextManager.getMetaDataContexts().getMetaData("schema"), not(metaData));
    }
    
    @Test
    public void assertDisableStateChanged() {
        DisabledStateChangedEvent event = new DisabledStateChangedEvent(new QualifiedSchema("schema.ds_0"), true);
        coordinator.renew(event);
    }
    
    @Test
    public void assertDataSourceChanged() {
        DataSourceChangedEvent event = new DataSourceChangedEvent("schema", getChangedDataSourceConfigurations());
        coordinator.renew(event);
        assertTrue(contextManager.getMetaDataContexts().getMetaData("schema").getResource().getDataSources().containsKey("ds_2"));
    }
    
    private Map<String, DataSourceConfiguration> getChangedDataSourceConfigurations() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourceConverter.getDataSourceConfiguration(dataSource));
        result.put("ds_1", DataSourceConverter.getDataSourceConfiguration(dataSource));
        result.put("ds_2", DataSourceConverter.getDataSourceConfiguration(dataSource));
        return result;
    }
    
    @Test
    public void assertGlobalRuleConfigurationsChanged() {
        GlobalRuleConfigurationsChangedEvent event = new GlobalRuleConfigurationsChangedEvent(getChangedGlobalRuleConfigurations());
        coordinator.renew(event);
        assertThat(contextManager.getMetaDataContexts().getGlobalRuleMetaData(), not(globalRuleMetaData));
        assertThat(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules().size(), is(3));
        assertThat(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof AuthorityRule).count(), is(1L));
        assertThat(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof TransactionRule).count(), is(1L));
        assertThat(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof SQLParserRule).count(), is(1L));
    }
    
    private Collection<RuleConfiguration> getChangedGlobalRuleConfigurations() {
        RuleConfiguration authorityRuleConfig = new AuthorityRuleConfiguration(getShardingSphereUsers(), new ShardingSphereAlgorithmConfiguration("NATIVE", new Properties()));
        return Collections.singleton(authorityRuleConfig);
    }
    
    private Collection<ShardingSphereUser> getShardingSphereUsers() {
        Collection<ShardingSphereUser> result = new LinkedList<>();
        result.add(new ShardingSphereUser("root", "root", "%"));
        result.add(new ShardingSphereUser("sharding", "sharding", "localhost"));
        return result;
    }
    
    @Test
    public void assertAuthorityChanged() {
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules()).thenReturn(createAuthorityRule());
        AuthorityChangedEvent event = new AuthorityChangedEvent(getShardingSphereUsers());
        coordinator.renew(event);
        Optional<AuthorityRule> authorityRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules()
                .stream().filter(each -> each instanceof AuthorityRule).findAny().map(each -> (AuthorityRule) each);
        assertTrue(authorityRule.isPresent());
        assertNotNull(authorityRule.get().findUser(new ShardingSphereUser("root", "root", "%").getGrantee()));
    }
    
    private Collection<ShardingSphereRule> createAuthorityRule() {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("ALL_PRIVILEGES_PERMITTED", new Properties()));
        AuthorityRule authorityRule = new AuthorityRule(ruleConfig, contextManager.getMetaDataContexts().getMetaDataMap(), Collections.emptyList());
        return Collections.singleton(authorityRule);
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        when(metaData.getName()).thenReturn("schema");
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(metaData.getResource()).thenReturn(resource);
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("schema", metaData);
    }
}
