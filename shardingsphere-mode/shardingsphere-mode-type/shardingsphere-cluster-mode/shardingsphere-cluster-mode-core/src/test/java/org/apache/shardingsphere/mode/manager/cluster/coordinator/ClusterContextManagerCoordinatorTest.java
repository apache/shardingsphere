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
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DynamicDataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.TableMetaDataChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.ViewMetaDataChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.DatabaseVersionChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListTriggerEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.util.ReflectionUtil;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.mode.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ClusterContextManagerCoordinatorTest {
    
    private ClusterContextManagerCoordinator coordinator;
    
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
        contextManager.renewMetaDataContexts(new MetaDataContexts(contextManager.getMetaDataContexts().getPersistService(),
                new ShardingSphereMetaData(createDatabases(), contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(new Properties()))));
        coordinator = new ClusterContextManagerCoordinator(persistService, new RegistryCenter(mock(ClusterPersistRepository.class),
                new EventBusContext(), mock(ProxyInstanceMetaData.class), null), contextManager);
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("FIXTURE", "", "", new Properties()), false);
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        return new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyList(), new Properties(), Collections.emptyList(), instanceMetaData, false);
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        when(database.getName()).thenReturn("db");
        when(database.getResource().getDataSources()).thenReturn(new LinkedHashMap<>());
        when(database.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
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
    public void assertRenewForDatabaseAdded() throws SQLException {
        when(persistService.getDataSourceService().load("db_added")).thenReturn(createDataSourcePropertiesMap());
        when(persistService.getDatabaseRulePersistService().load("db_added")).thenReturn(Collections.emptyList());
        coordinator.renew(new DatabaseAddedEvent("db_added"));
        assertNotNull(contextManager.getMetaDataContexts().getMetaData().getDatabase("db_added").getResource().getDataSources());
    }
    
    private Map<String, DataSourceProperties> createDataSourcePropertiesMap() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourcePropertiesCreator.create(dataSource));
        result.put("replica_ds_0", DataSourcePropertiesCreator.create(dataSource));
        result.put("replica_ds_1", DataSourcePropertiesCreator.create(dataSource));
        return result;
    }
    
    @Test
    public void assertRenewForDatabaseDeleted() {
        coordinator.renew(new DatabaseDeletedEvent("db"));
        assertNull(contextManager.getMetaDataContexts().getMetaData().getDatabase("db"));
    }
    
    @Test
    public void assertRenewForSchemaAdded() {
        coordinator.renew(new SchemaAddedEvent("db", "foo_schema"));
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db")).putSchema(argThat(argument -> argument.equals("foo_schema")), any(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertRenewForSchemaDeleted() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").containsSchema("foo_schema")).thenReturn(true);
        coordinator.renew(new SchemaDeletedEvent("db", "foo_schema"));
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db")).removeSchema("foo_schema");
    }
    
    @Test
    public void assertRenewForTableMetaDataChangedChanged() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").containsSchema("db")).thenReturn(true);
        ShardingSphereTable changedTableMetaData = new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        TableMetaDataChangedEvent event = new TableMetaDataChangedEvent("db", "db", changedTableMetaData, null);
        coordinator.renew(event);
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").getSchema("db")).putTable("t_order", event.getChangedTableMetaData());
    }
    
    @Test
    public void assertRenewForViewMetaDataChanged() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").containsSchema("db")).thenReturn(true);
        ShardingSphereView changedViewMetaData = new ShardingSphereView("t_order_view", "");
        ViewMetaDataChangedEvent event = new ViewMetaDataChangedEvent("db", "db", changedViewMetaData, null);
        coordinator.renew(event);
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").getSchema("db")).putView("t_order_view", event.getChangedViewMetaData());
    }
    
    @Test
    public void assertRenewForRuleConfigurationsChanged() {
        when(persistService.getMetaDataVersionPersistService().isActiveVersion("db", "0")).thenReturn(true);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabase("db"), is(database));
        coordinator.renew(new RuleConfigurationsChangedEvent("db", "0", Collections.emptyList()));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabase("db"), not(database));
    }
    
    @Test
    public void assertRenewForDisableStateChanged() {
        StaticDataSourceContainedRule staticDataSourceRule = mock(StaticDataSourceContainedRule.class);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(staticDataSourceRule));
        StorageNodeChangedEvent event = new StorageNodeChangedEvent(new QualifiedDatabase("db.readwrite_ds.ds_0"), new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.DISABLED));
        coordinator.renew(event);
        verify(staticDataSourceRule).updateStatus(argThat(
                (ArgumentMatcher<StorageNodeDataSourceChangedEvent>) argumentEvent -> Objects.equals(event.getQualifiedDatabase(), argumentEvent.getQualifiedDatabase())
                        && Objects.equals(event.getDataSource(), argumentEvent.getDataSource())));
    }
    
    @Test
    public void assertRenewForDataSourceChanged() {
        when(persistService.getMetaDataVersionPersistService().isActiveVersion("db", "0")).thenReturn(true);
        coordinator.renew(new DataSourceChangedEvent("db", "0", createChangedDataSourcePropertiesMap()));
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabase("db").getResource().getDataSources().containsKey("ds_2"));
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
        coordinator.renew(event);
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
    public void assertRenewPrimaryDataSourceName() {
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        DynamicDataSourceContainedRule dynamicDataSourceRule = mock(DynamicDataSourceContainedRule.class);
        rules.add(dynamicDataSourceRule);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(rules);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        contextManager.getMetaDataContexts().getMetaData().getDatabases().put("db", database);
        PrimaryStateChangedEvent mockPrimaryStateChangedEvent = new PrimaryStateChangedEvent(new QualifiedDatabase("db.readwrite_ds.test_ds"));
        coordinator.renew(mockPrimaryStateChangedEvent);
        verify(dynamicDataSourceRule).restartHeartBeatJob(any());
    }
    
    @Test
    public void assertRenewInstanceStatus() {
        Collection<String> testStates = new LinkedList<>();
        testStates.add(StateType.OK.name());
        StateEvent mockStateEvent = new StateEvent(contextManager.getInstanceContext().getInstance().getMetaData().getId(), testStates);
        coordinator.renew(mockStateEvent);
        assertThat(contextManager.getInstanceContext().getInstance().getState().getCurrentState(), is(StateType.OK));
        testStates.add(StateType.CIRCUIT_BREAK.name());
        coordinator.renew(mockStateEvent);
        assertThat(contextManager.getInstanceContext().getInstance().getState().getCurrentState(), is(StateType.CIRCUIT_BREAK));
    }
    
    @Test
    public void assertRenewInstanceLabels() {
        Collection<String> labels = Collections.singleton("test");
        coordinator.renew(new LabelsEvent(contextManager.getInstanceContext().getInstance().getMetaData().getId(), labels));
        assertThat(contextManager.getInstanceContext().getInstance().getLabels(), is(labels));
    }
    
    @Test
    public void assertRenewInstanceOfflineEvent() {
        coordinator.renew(new InstanceOfflineEvent(contextManager.getInstanceContext().getInstance().getMetaData()));
        assertThat(((ProxyInstanceMetaData) contextManager.getInstanceContext().getInstance().getMetaData()).getPort(), is(3307));
    }
    
    @Test
    public void assertRenewDatabaseVersionChangedEvent() {
        when(persistService.getDataSourceService().load("db", "1")).thenReturn(getVersionChangedDataSourcePropertiesMap());
        when(persistService.getDatabaseRulePersistService().load("db", "1")).thenReturn(Collections.emptyList());
        Map<String, DataSource> dataSourceMap = initContextManager();
        coordinator.renew(new DatabaseVersionChangedEvent("db", "1"));
        assertThat(contextManager.getDataSourceMap("db").get("ds_0"), is(dataSourceMap.get("ds_0")));
        assertNotNull(contextManager.getDataSourceMap("db").get("ds_1"));
        assertThat(DataSourcePropertiesCreator.create(getChangedDataSource()), is(DataSourcePropertiesCreator.create(contextManager.getDataSourceMap("db").get("ds_1"))));
        assertNotNull(contextManager.getDataSourceMap("db").get("primary_ds"));
        assertThat(DataSourcePropertiesCreator.create(new MockedDataSource()), is(DataSourcePropertiesCreator.create(contextManager.getDataSourceMap("db").get("primary_ds"))));
    }
    
    @Test
    public void assertRenewInstanceOnlineEvent() {
        InstanceMetaData instanceMetaData1 = new ProxyInstanceMetaData("foo_instance_3307", 3307);
        InstanceOnlineEvent instanceOnlineEvent1 = new InstanceOnlineEvent(instanceMetaData1);
        coordinator.renew(instanceOnlineEvent1);
        assertThat(contextManager.getInstanceContext().getAllClusterInstances().size(), is(1));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getAllClusterInstances()).get(0).getMetaData(), is(instanceMetaData1));
        InstanceMetaData instanceMetaData2 = new ProxyInstanceMetaData("foo_instance_3308", 3308);
        InstanceOnlineEvent instanceOnlineEvent2 = new InstanceOnlineEvent(instanceMetaData2);
        coordinator.renew(instanceOnlineEvent2);
        assertThat(contextManager.getInstanceContext().getAllClusterInstances().size(), is(2));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getAllClusterInstances()).get(1).getMetaData(), is(instanceMetaData2));
        coordinator.renew(instanceOnlineEvent1);
        assertThat(contextManager.getInstanceContext().getAllClusterInstances().size(), is(2));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getAllClusterInstances()).get(1).getMetaData(), is(instanceMetaData1));
    }
    
    @Test
    public void assertRenewProperties() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        coordinator.renew(new PropertiesChangedEvent(props));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getProps().getProperty(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.TRUE.toString()));
    }
    
    @Test
    public void assertCompleteUnitShowProcessList() {
        String processListId = "foo_process_id";
        ShowProcessListSimpleLock lock = new ShowProcessListSimpleLock();
        ShowProcessListManager.getInstance().getLocks().put(processListId, lock);
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            try {
                Thread.sleep(50L);
            } catch (final InterruptedException ignored) {
            }
            coordinator.completeUnitShowProcessList(new ShowProcessListUnitCompleteEvent(processListId));
        });
        lockAndAwaitDefaultTime(lock);
        long currentTime = System.currentTimeMillis();
        assertTrue(currentTime >= startTime + 50L);
        assertTrue(currentTime <= startTime + 5000L);
        ShowProcessListManager.getInstance().getLocks().remove(processListId);
    }
    
    @Test
    public void assertTriggerShowProcessList() throws NoSuchFieldException, IllegalAccessException {
        String instanceId = contextManager.getInstanceContext().getInstance().getMetaData().getId();
        ShowProcessListManager.getInstance().putProcessContext("foo_execution_id", mock(ExecuteProcessContext.class));
        String processListId = "foo_process_id";
        coordinator.triggerShowProcessList(new ShowProcessListTriggerEvent(instanceId, processListId));
        ClusterPersistRepository repository = ReflectionUtil.getFieldValue(coordinator, "registryCenter", RegistryCenter.class).getRepository();
        verify(repository).persist("/execution_nodes/foo_process_id/" + instanceId,
                "contexts:" + System.lineSeparator() + "- startTimeMillis: 0" + System.lineSeparator());
        verify(repository).delete("/nodes/compute_nodes/process_trigger/" + instanceId + ":foo_process_id");
    }
    
    private void lockAndAwaitDefaultTime(final ShowProcessListSimpleLock lock) {
        lock.lock();
        try {
            lock.awaitDefaultTime();
        } finally {
            lock.unlock();
        }
    }
    
    private Map<String, DataSource> initContextManager() {
        Map<String, DataSource> result = getDataSourceMap();
        ShardingSphereResource resource = new ShardingSphereResource("sharding_db", result);
        ShardingSphereDatabase database = new ShardingSphereDatabase("db", new MySQLDatabaseType(), resource, mock(ShardingSphereRuleMetaData.class), Collections.emptyMap());
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
