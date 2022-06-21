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
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.version.DatabaseVersionChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListTriggerEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.util.ReflectionUtil;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
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
import java.util.HashMap;
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public final class ClusterContextManagerCoordinatorTest {
    
    private ClusterContextManagerCoordinator coordinator;
    
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistService metaDataPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    @Before
    public void setUp() throws SQLException {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("FIXTURE", "", "", new Properties()), false);
        contextManager = new ClusterContextManagerBuilder().build(ContextManagerBuilderParameter.builder().modeConfig(modeConfig).databaseConfigs(Collections.emptyMap())
                .globalRuleConfigs(Collections.emptyList()).props(new Properties()).instanceDefinition(new InstanceDefinition(InstanceType.PROXY, 3307, "foo_instance_id")).build());
        assertTrue(contextManager.getMetaDataContexts().getPersistService().isPresent());
        contextManager.renewMetaDataContexts(new MetaDataContexts(contextManager.getMetaDataContexts().getPersistService().get(),
                new ShardingSphereMetaData(createDatabases(), contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(),
                        new ConfigurationProperties(new Properties())),
                createOptimizerContext()));
        coordinator = new ClusterContextManagerCoordinator(metaDataPersistService, contextManager, new RegistryCenter(mock(ClusterPersistRepository.class)));
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        when(database.getName()).thenReturn("db");
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        when(resource.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(database.getResource()).thenReturn(resource);
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(database.getSchemas().get(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
        when(database.getRuleMetaData().getRules()).thenReturn(new LinkedList<>());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return new HashMap<>(Collections.singletonMap("db", database));
    }
    
    private OptimizerContext createOptimizerContext() {
        OptimizerContext result = mock(OptimizerContext.class, RETURNS_DEEP_STUBS);
        when(result.getFederationMetaData().getDatabases()).thenReturn(new HashMap<>(Collections.singletonMap("db", new FederationDatabaseMetaData("db", Collections.emptyMap()))));
        return result;
    }
    
    @Test
    public void assertDatabaseAdd() throws SQLException {
        when(metaDataPersistService.getDataSourceService().load("db_add")).thenReturn(getDataSourcePropertiesMap());
        when(metaDataPersistService.getDatabaseRulePersistService().load("db_add")).thenReturn(Collections.emptyList());
        coordinator.renew(new DatabaseAddedEvent("db_add"));
        assertNotNull(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("db_add").getResource().getDataSources());
    }
    
    private Map<String, DataSourceProperties> getDataSourcePropertiesMap() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourcePropertiesCreator.create(dataSource));
        result.put("ds_0", DataSourcePropertiesCreator.create(dataSource));
        result.put("ds_1", DataSourcePropertiesCreator.create(dataSource));
        return result;
    }
    
    @Test
    public void assertSchemaDelete() {
        coordinator.renew(new DatabaseDeletedEvent("db"));
        assertNull(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("db"));
    }
    
    @Test
    public void assertPropertiesChanged() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        coordinator.renew(new PropertiesChangedEvent(props));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getProps().getProperty(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.TRUE.toString()));
    }
    
    @Test
    public void assertSchemaAdd() {
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("db").getSchemas().get("foo_schema")).thenReturn(null);
        coordinator.renew(new SchemaAddedEvent("db", "foo_schema"));
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("db").getSchemas()).put(argThat(argument -> argument.equals("foo_schema")), any(ShardingSphereSchema.class));
    }
    
    @Test
    public void assertSchemaChanged() {
        ShardingSphereTable changedTableMetaData = new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        SchemaChangedEvent event = new SchemaChangedEvent("db", "db", changedTableMetaData, null);
        coordinator.renew(event);
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().containsKey("db"));
        verify(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("db").getSchemas().get("db")).put("t_order", event.getChangedTableMetaData());
    }
    
    @Test
    public void assertRuleConfigurationsChanged() {
        when(metaDataPersistService.getDatabaseVersionPersistService().isActiveVersion("db", "0")).thenReturn(true);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("db"), is(database));
        coordinator.renew(new RuleConfigurationsChangedEvent("db", "0", Collections.emptyList()));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("db"), not(database));
    }
    
    @Test
    public void assertDisableStateChanged() {
        StatusContainedRule statusContainedRule = mock(StatusContainedRule.class);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(statusContainedRule));
        StorageNodeChangedEvent event = new StorageNodeChangedEvent(new QualifiedDatabase("db.readwrite_ds.ds_0"), new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.DISABLED));
        coordinator.renew(event);
        verify(statusContainedRule, times(1)).updateStatus(argThat(
                (ArgumentMatcher<StorageNodeDataSourceChangedEvent>) argumentEvent -> Objects.equals(event.getQualifiedDatabase(), argumentEvent.getQualifiedDatabase())
                        && Objects.equals(event.getDataSource(), argumentEvent.getDataSource())));
    }
    
    @Test
    public void assertDataSourceChanged() {
        when(metaDataPersistService.getDatabaseVersionPersistService().isActiveVersion("db", "0")).thenReturn(true);
        coordinator.renew(new DataSourceChangedEvent("db", "0", getChangedDataSourcePropertiesMap()));
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabases().get("db").getResource().getDataSources().containsKey("ds_2"));
    }
    
    private Map<String, DataSourceProperties> getChangedDataSourcePropertiesMap() {
        MockedDataSource dataSource = new MockedDataSource();
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(3, 1);
        result.put("primary_ds", DataSourcePropertiesCreator.create(dataSource));
        result.put("ds_1", DataSourcePropertiesCreator.create(dataSource));
        result.put("ds_2", DataSourcePropertiesCreator.create(dataSource));
        return result;
    }
    
    @Test
    public void assertGlobalRuleConfigurationsChanged() {
        GlobalRuleConfigurationsChangedEvent event = new GlobalRuleConfigurationsChangedEvent(getChangedGlobalRuleConfigurations());
        coordinator.renew(event);
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(), not(globalRuleMetaData));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().size(), is(4));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof AuthorityRule).count(), is(1L));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof TransactionRule).count(), is(1L));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof SQLParserRule).count(), is(1L));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof SQLTranslatorRule).count(), is(1L));
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
    public void assertRenewPrimaryDataSourceName() {
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        StatusContainedRule mockStatusContainedRule = mock(StatusContainedRule.class);
        rules.add(mockStatusContainedRule);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(rules);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        contextManager.getMetaDataContexts().getMetaData().getDatabases().put("db", database);
        PrimaryStateChangedEvent mockPrimaryStateChangedEvent = new PrimaryStateChangedEvent(new QualifiedDatabase("db.readwrite_ds.test_ds"));
        coordinator.renew(mockPrimaryStateChangedEvent);
        verify(mockStatusContainedRule).updateStatus(any());
    }
    
    @Test
    public void assertRenewInstanceStatus() {
        Collection<String> testStates = new LinkedList<>();
        testStates.add(StateType.OK.name());
        StateEvent mockStateEvent = new StateEvent(contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId(), testStates);
        coordinator.renew(mockStateEvent);
        assertThat(contextManager.getInstanceContext().getInstance().getState().getCurrentState(), is(StateType.OK));
        testStates.add(StateType.CIRCUIT_BREAK.name());
        coordinator.renew(mockStateEvent);
        assertThat(contextManager.getInstanceContext().getInstance().getState().getCurrentState(), is(StateType.CIRCUIT_BREAK));
    }
    
    @Test
    public void assertRenewWorkerIdChange() {
        WorkerIdEvent mockWorkerIdEvent = new WorkerIdEvent(contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId(), 12223L);
        coordinator.renew(mockWorkerIdEvent);
        assertThat(contextManager.getInstanceContext().getWorkerId(), is(12223L));
    }
    
    @Test
    public void assertRenewInstanceLabels() {
        Collection<String> labels = Collections.singleton("test");
        coordinator.renew(new LabelsEvent(contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId(), labels));
        assertThat(contextManager.getInstanceContext().getInstance().getLabels(), is(labels));
    }
    
    @Test
    public void assertRenewInstanceOfflineEvent() {
        coordinator.renew(new InstanceOfflineEvent(contextManager.getInstanceContext().getInstance().getInstanceDefinition()));
        assertThat(contextManager.getInstanceContext().getInstance().getInstanceDefinition().getUniqueSign(), is("3307"));
    }
    
    @Test
    public void assertRenewDatabaseVersionChangedEvent() {
        when(metaDataPersistService.getDataSourceService().load("db", "1")).thenReturn(getVersionChangedDataSourcePropertiesMap());
        when(metaDataPersistService.getDatabaseRulePersistService().load("db", "1")).thenReturn(Collections.emptyList());
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
        InstanceDefinition instanceDefinition1 = new InstanceDefinition(InstanceType.PROXY, 3307, "foo_instance_3307");
        InstanceOnlineEvent instanceOnlineEvent1 = new InstanceOnlineEvent(instanceDefinition1);
        coordinator.renew(instanceOnlineEvent1);
        assertThat(contextManager.getInstanceContext().getComputeNodeInstances().size(), is(1));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getComputeNodeInstances()).get(0).getInstanceDefinition(), is(instanceDefinition1));
        InstanceDefinition instanceDefinition2 = new InstanceDefinition(InstanceType.PROXY, 3308, "foo_instance_3308");
        InstanceOnlineEvent instanceOnlineEvent2 = new InstanceOnlineEvent(instanceDefinition2);
        coordinator.renew(instanceOnlineEvent2);
        assertThat(contextManager.getInstanceContext().getComputeNodeInstances().size(), is(2));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getComputeNodeInstances()).get(1).getInstanceDefinition(), is(instanceDefinition2));
        coordinator.renew(instanceOnlineEvent1);
        assertThat(contextManager.getInstanceContext().getComputeNodeInstances().size(), is(2));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getComputeNodeInstances()).get(1).getInstanceDefinition(), is(instanceDefinition1));
    }
    
    @Test
    public void assertCompleteUnitShowProcessList() {
        String showProcessListId = "foo_process_id";
        ShowProcessListSimpleLock lock = new ShowProcessListSimpleLock();
        ShowProcessListManager.getInstance().getLocks().put(showProcessListId, lock);
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            try {
                Thread.sleep(50L);
            } catch (final InterruptedException ignored) {
            }
            coordinator.completeUnitShowProcessList(new ShowProcessListUnitCompleteEvent(showProcessListId));
        });
        lockAndAwaitDefaultTime(lock);
        long currentTime = System.currentTimeMillis();
        assertTrue(currentTime >= startTime + 50L);
        assertTrue(currentTime <= startTime + 5000L);
        ShowProcessListManager.getInstance().getLocks().remove(showProcessListId);
    }
    
    @Test
    public void assertTriggerShowProcessList() throws NoSuchFieldException, IllegalAccessException {
        String instanceId = contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId();
        ShowProcessListManager.getInstance().putProcessContext("foo_execution_id", new YamlExecuteProcessContext(mock(ExecuteProcessContext.class)));
        String showProcessListId = "foo_process_id";
        coordinator.triggerShowProcessList(new ShowProcessListTriggerEvent(instanceId, showProcessListId));
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
        ShardingSphereResource resource = new ShardingSphereResource(result);
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
