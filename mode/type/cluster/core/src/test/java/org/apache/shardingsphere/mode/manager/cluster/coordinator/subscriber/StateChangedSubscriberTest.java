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

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.datasource.state.DataSourceState;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.event.ClusterLockDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.event.ClusterStateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOfflineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.InstanceOnlineEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.StorageNodeChangedEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.event.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSourceChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StateChangedSubscriberTest {
    
    private StateChangedSubscriber subscriber;
    
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() throws SQLException {
        contextManager = new ClusterContextManagerBuilder().build(createContextManagerBuilderParameter());
        contextManager.renewMetaDataContexts(new MetaDataContexts(contextManager.getMetaDataContexts().getPersistService(), new ShardingSphereMetaData(createDatabases(),
                contextManager.getMetaDataContexts().getMetaData().getGlobalResourceMetaData(), contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(),
                new ConfigurationProperties(new Properties()))));
        subscriber = new StateChangedSubscriber(new RegistryCenter(mock(ClusterPersistRepository.class), new EventBusContext(), mock(ProxyInstanceMetaData.class), null), contextManager);
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("FIXTURE", "", "", new Properties()));
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        return new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(),
                new Properties(), Collections.emptyList(), instanceMetaData, false);
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        when(database.getResourceMetaData().getDataSources()).thenReturn(new LinkedHashMap<>());
        when(database.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", new MySQLDatabaseType()));
        when(database.getSchemas()).thenReturn(Collections.singletonMap("foo_schema", new ShardingSphereSchema()));
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(database.getSchema("foo_schema")).thenReturn(mock(ShardingSphereSchema.class));
        when(database.getRuleMetaData().getRules()).thenReturn(new LinkedList<>());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        when(database.getRuleMetaData().findRules(ResourceHeldRule.class)).thenReturn(Collections.emptyList());
        return Collections.singletonMap("db", database);
    }
    
    @Test
    void assertRenewForDisableStateChanged() {
        StaticDataSourceContainedRule staticDataSourceRule = mock(StaticDataSourceContainedRule.class);
        when(database.getRuleMetaData().findSingleRule(StaticDataSourceContainedRule.class)).thenReturn(Optional.of(staticDataSourceRule));
        StorageNodeChangedEvent event = new StorageNodeChangedEvent(new QualifiedDatabase("db.readwrite_ds.ds_0"), new StorageNodeDataSource(StorageNodeRole.MEMBER, DataSourceState.DISABLED));
        subscriber.renew(event);
        verify(staticDataSourceRule).updateStatus(argThat(
                (ArgumentMatcher<StorageNodeDataSourceChangedEvent>) argumentEvent -> Objects.equals(event.getQualifiedDatabase(), argumentEvent.getQualifiedDatabase())
                        && Objects.equals(event.getDataSource(), argumentEvent.getDataSource())));
    }
    
    @Test
    void assertResetClusterStatus() {
        ClusterLockDeletedEvent mockLockDeletedEvent = new ClusterLockDeletedEvent(ClusterState.OK);
        subscriber.renew(mockLockDeletedEvent);
        assertThat(contextManager.getClusterStateContext().getCurrentState(), is(ClusterState.OK));
    }
    
    @Test
    void assertRenewClusterStatus() {
        ClusterStateEvent mockClusterStateEvent = new ClusterStateEvent("READ_ONLY");
        subscriber.renew(mockClusterStateEvent);
        assertThat(contextManager.getClusterStateContext().getCurrentState(), is(ClusterState.READ_ONLY));
    }
    
    @Test
    void assertRenewInstanceStatus() {
        StateEvent mockStateEvent = new StateEvent(contextManager.getInstanceContext().getInstance().getMetaData().getId(), InstanceState.OK.name());
        subscriber.renew(mockStateEvent);
        assertThat(contextManager.getInstanceContext().getInstance().getState().getCurrentState(), is(InstanceState.OK));
    }
    
    @Test
    void assertRenewInstanceWorkerIdEvent() {
        subscriber.renew(new WorkerIdEvent(contextManager.getInstanceContext().getInstance().getMetaData().getId(), 0));
        assertThat(contextManager.getInstanceContext().getInstance().getWorkerId(), is(0));
    }
    
    @Test
    void assertRenewInstanceLabels() {
        Collection<String> labels = Collections.singleton("test");
        subscriber.renew(new LabelsEvent(contextManager.getInstanceContext().getInstance().getMetaData().getId(), labels));
        assertThat(contextManager.getInstanceContext().getInstance().getLabels(), is(labels));
    }
    
    @Test
    void assertRenewInstanceOfflineEvent() {
        subscriber.renew(new InstanceOfflineEvent(contextManager.getInstanceContext().getInstance().getMetaData()));
        assertThat(((ProxyInstanceMetaData) contextManager.getInstanceContext().getInstance().getMetaData()).getPort(), is(3307));
    }
    
    @Test
    void assertRenewInstanceOnlineEvent() {
        InstanceMetaData instanceMetaData1 = new ProxyInstanceMetaData("foo_instance_3307", 3307);
        InstanceOnlineEvent instanceOnlineEvent1 = new InstanceOnlineEvent(instanceMetaData1);
        subscriber.renew(instanceOnlineEvent1);
        assertThat(contextManager.getInstanceContext().getAllClusterInstances().size(), is(1));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getAllClusterInstances()).get(0).getMetaData(), is(instanceMetaData1));
        InstanceMetaData instanceMetaData2 = new ProxyInstanceMetaData("foo_instance_3308", 3308);
        InstanceOnlineEvent instanceOnlineEvent2 = new InstanceOnlineEvent(instanceMetaData2);
        subscriber.renew(instanceOnlineEvent2);
        assertThat(contextManager.getInstanceContext().getAllClusterInstances().size(), is(2));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getAllClusterInstances()).get(1).getMetaData(), is(instanceMetaData2));
        subscriber.renew(instanceOnlineEvent1);
        assertThat(contextManager.getInstanceContext().getAllClusterInstances().size(), is(2));
        assertThat(((LinkedList<ComputeNodeInstance>) contextManager.getInstanceContext().getAllClusterInstances()).get(1).getMetaData(), is(instanceMetaData1));
    }
}
