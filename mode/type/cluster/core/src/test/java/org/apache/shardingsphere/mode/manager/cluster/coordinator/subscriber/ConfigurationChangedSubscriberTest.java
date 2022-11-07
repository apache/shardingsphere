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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.DatabaseVersionChangedEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationChangedSubscriberTest {
    
    private ConfigurationChangedSubscriber subscriber;
    
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistService persistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    @Before
    public void setUp() throws SQLException {
        contextManager = new ClusterContextManagerBuilder().build(createContextManagerBuilderParameter());
        contextManager.renewMetaDataContexts(new MetaDataContexts(contextManager.getMetaDataContexts().getPersistService(), new ShardingSphereMetaData(createDatabases(),
                contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(new Properties()))));
        subscriber = new ConfigurationChangedSubscriber(persistService, new RegistryCenter(mock(ClusterPersistRepository.class),
                new EventBusContext(), mock(ProxyInstanceMetaData.class), null), contextManager);
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("FIXTURE", "", "", new Properties()));
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        return new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyList(), new Properties(), Collections.emptyList(), instanceMetaData, false);
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        when(database.getName()).thenReturn("db");
        when(database.getResourceMetaData().getDataSources()).thenReturn(new LinkedHashMap<>());
        when(database.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", new MySQLDatabaseType()));
        when(database.getSchemas()).thenReturn(Collections.singletonMap("foo_schema", new ShardingSphereSchema()));
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(database.getSchema("foo_schema")).thenReturn(mock(ShardingSphereSchema.class));
        when(database.getRuleMetaData().getRules()).thenReturn(new LinkedList<>());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        when(database.getRuleMetaData().findRules(ResourceHeldRule.class)).thenReturn(Collections.emptyList());
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put("db", database);
        return result;
    }
    
    @Test
    public void assertRenewForRuleConfigurationsChanged() {
        when(persistService.getMetaDataVersionPersistService().isActiveVersion("db", "0")).thenReturn(true);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabase("db"), is(database));
        subscriber.renew(new RuleConfigurationsChangedEvent("db", "0", Collections.emptyList()));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabase("db"), not(database));
    }
    
    @Test
    public void assertRenewForDataSourceChanged() {
        when(persistService.getMetaDataVersionPersistService().isActiveVersion("db", "0")).thenReturn(true);
        subscriber.renew(new DataSourceChangedEvent("db", "0", createChangedDataSourcePropertiesMap()));
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").getResourceMetaData().getDataSources().containsKey("ds_2"));
    }
    
    private Map<String, DataSourceProperties> createChangedDataSourcePropertiesMap() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourcePropertiesCreator.create(dataSource));
        result.put("ds_1", DataSourcePropertiesCreator.create(dataSource));
        result.put("ds_2", DataSourcePropertiesCreator.create(dataSource));
        return result;
    }
    
    @Test
    public void assertRenewForGlobalRuleConfigurationsChanged() {
        GlobalRuleConfigurationsChangedEvent event = new GlobalRuleConfigurationsChangedEvent(getChangedGlobalRuleConfigurations());
        subscriber.renew(event);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(), not(globalRuleMetaData));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().size(), is(3));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof AuthorityRule).count(), is(1L));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof TransactionRule).count(), is(1L));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof SQLTranslatorRule).count(), is(1L));
    }
    
    private Collection<RuleConfiguration> getChangedGlobalRuleConfigurations() {
        RuleConfiguration authorityRuleConfig = new AuthorityRuleConfiguration(getShardingSphereUsers(), new AlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        return Collections.singleton(authorityRuleConfig);
    }
    
    private Collection<ShardingSphereUser> getShardingSphereUsers() {
        Collection<ShardingSphereUser> result = new LinkedList<>();
        result.add(new ShardingSphereUser("root", "root", "%"));
        result.add(new ShardingSphereUser("sharding", "sharding", "localhost"));
        return result;
    }
    
    @Test
    public void assertRenewDatabaseVersionChangedEvent() {
        when(persistService.getDataSourceService().load("db", "1")).thenReturn(getVersionChangedDataSourcePropertiesMap());
        when(persistService.getDatabaseRulePersistService().load("db", "1")).thenReturn(Collections.emptyList());
        Map<String, DataSource> dataSourceMap = initContextManager();
        subscriber.renew(new DatabaseVersionChangedEvent("db", "1"));
        assertThat(contextManager.getDataSourceMap("db").get("ds_0"), is(dataSourceMap.get("ds_0")));
        assertNotNull(contextManager.getDataSourceMap("db").get("ds_1"));
        assertThat(DataSourcePropertiesCreator.create(getChangedDataSource()), is(DataSourcePropertiesCreator.create(contextManager.getDataSourceMap("db").get("ds_1"))));
        assertNotNull(contextManager.getDataSourceMap("db").get("primary_ds"));
        assertThat(DataSourcePropertiesCreator.create(new MockedDataSource()), is(DataSourcePropertiesCreator.create(contextManager.getDataSourceMap("db").get("primary_ds"))));
    }
    
    @Test
    public void assertRenewProperties() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        subscriber.renew(new PropertiesChangedEvent(props));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getProps().getProperty(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.TRUE.toString()));
    }
    
    private Map<String, DataSource> initContextManager() {
        Map<String, DataSource> result = getDataSourceMap();
        ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", result);
        ShardingSphereDatabase database = new ShardingSphereDatabase("db", new MySQLDatabaseType(), resourceMetaData, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
        contextManager.getMetaDataContexts().getMetaData().getDatabases().put("db", database);
        return result;
    }
    
    private Map<String, DataSource> getDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(3, 1);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        result.put("db", new MockedDataSource());
        return result;
    }
    
    private Map<String, DataSourceProperties> getVersionChangedDataSourcePropertiesMap() {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourcePropertiesCreator.create(new MockedDataSource()));
        result.put("ds_0", DataSourcePropertiesCreator.create(new MockedDataSource()));
        result.put("ds_1", DataSourcePropertiesCreator.create(getChangedDataSource()));
        return result;
    }
    
    private MockedDataSource getChangedDataSource() {
        MockedDataSource result = new MockedDataSource();
        result.setMaxPoolSize(5);
        result.setUsername("username");
        return result;
    }
}
