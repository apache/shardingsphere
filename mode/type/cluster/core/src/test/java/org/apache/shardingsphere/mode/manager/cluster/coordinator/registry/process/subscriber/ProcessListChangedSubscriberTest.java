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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.subscriber;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockReleaseStrategy;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillLocalProcessEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ReportLocalProcessesCompletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ReportLocalProcessesEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessListChangedSubscriberTest {
    
    private ProcessListChangedSubscriber subscriber;
    
    private ContextManager contextManager;
    
    private RegistryCenter registryCenter;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() throws SQLException {
        contextManager = new ClusterContextManagerBuilder().build(createContextManagerBuilderParameter());
        contextManager.renewMetaDataContexts(new MetaDataContexts(contextManager.getMetaDataContexts().getPersistService(), new ShardingSphereMetaData(createDatabases(),
                contextManager.getMetaDataContexts().getMetaData().getGlobalResourceMetaData(), contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(),
                new ConfigurationProperties(new Properties()))));
        registryCenter = new RegistryCenter(mock(ClusterPersistRepository.class), new EventBusContext(), mock(ProxyInstanceMetaData.class), null);
        subscriber = new ProcessListChangedSubscriber(registryCenter, contextManager);
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
    void assertReportLocalProcesses() {
        String instanceId = contextManager.getInstanceContext().getInstance().getMetaData().getId();
        Process process = mock(Process.class);
        String processId = "foo_id";
        when(process.getId()).thenReturn(processId);
        ProcessRegistry.getInstance().add(process);
        subscriber.reportLocalProcesses(new ReportLocalProcessesEvent(instanceId, processId));
        ClusterPersistRepository repository = registryCenter.getRepository();
        verify(repository).persist("/execution_nodes/foo_id/" + instanceId,
                "processes:" + System.lineSeparator() + "- completedUnitCount: 0\n  id: foo_id\n  idle: false\n  startMillis: 0\n  totalUnitCount: 0" + System.lineSeparator());
        verify(repository).delete("/nodes/compute_nodes/show_process_list_trigger/" + instanceId + ":foo_id");
    }
    
    @Test
    void assertCompleteToReportLocalProcesses() {
        String taskId = "foo_id";
        long startMillis = System.currentTimeMillis();
        Executors.newFixedThreadPool(1).submit(() -> {
            Awaitility.await().pollDelay(50L, TimeUnit.MILLISECONDS).until(() -> true);
            subscriber.completeToReportLocalProcesses(new ReportLocalProcessesCompletedEvent(taskId));
        });
        waitUntilReleaseReady(taskId);
        long currentMillis = System.currentTimeMillis();
        assertThat(currentMillis, greaterThanOrEqualTo(startMillis + 50L));
        assertThat(currentMillis, lessThanOrEqualTo(startMillis + 5000L));
    }
    
    @Test
    void assertKillLocalProcess() throws SQLException {
        String instanceId = contextManager.getInstanceContext().getInstance().getMetaData().getId();
        String processId = "foo_id";
        subscriber.killLocalProcess(new KillLocalProcessEvent(instanceId, processId));
        ClusterPersistRepository repository = registryCenter.getRepository();
        verify(repository).delete("/nodes/compute_nodes/kill_process_trigger/" + instanceId + ":foo_id");
    }
    
    @Test
    void assertCompleteToKillLocalProcess() {
        String processId = "foo_id";
        long startMillis = System.currentTimeMillis();
        Executors.newFixedThreadPool(1).submit(() -> {
            Awaitility.await().pollDelay(50L, TimeUnit.MILLISECONDS).until(() -> true);
            subscriber.completeToKillLocalProcess(new KillLocalProcessCompletedEvent(processId));
        });
        waitUntilReleaseReady(processId);
        long currentMillis = System.currentTimeMillis();
        assertThat(currentMillis, greaterThanOrEqualTo(startMillis + 50L));
        assertThat(currentMillis, lessThanOrEqualTo(startMillis + 5000L));
    }
    
    private void waitUntilReleaseReady(final String lockId) {
        ProcessOperationLockRegistry.getInstance().waitUntilReleaseReady(lockId, new ProcessOperationLockReleaseStrategy() {
            
            private final AtomicBoolean firstTime = new AtomicBoolean(true);
            
            @Override
            public boolean isReadyToRelease() {
                return !firstTime.getAndSet(false);
            }
        });
    }
}
