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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockReleaseStrategy;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ReportLocalProcessesCompletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.ReportLocalProcessesEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.plugins.MemberAccessor;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    
    @Mock
    private PersistRepository repository;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException {
        EventBusContext eventBusContext = new EventBusContext();
        contextManager = new ClusterContextManagerBuilder().build(createContextManagerBuilderParameter(), eventBusContext);
        contextManager.renewMetaDataContexts(MetaDataContextsFactory.create(contextManager.getPersistServiceFacade().getMetaDataPersistService(), new ShardingSphereMetaData(createDatabases(),
                contextManager.getMetaDataContexts().getMetaData().getGlobalResourceMetaData(), contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(),
                new ConfigurationProperties(new Properties()))));
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(contextManager.getPersistServiceFacade().getClass().getDeclaredField("repository"), contextManager.getPersistServiceFacade(), repository);
        subscriber = new ProcessListChangedSubscriber(contextManager);
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("FIXTURE", "", "", new Properties()));
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        return new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(),
                new Properties(), Collections.emptyList(), instanceMetaData, false);
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        when(database.getSchemas()).thenReturn(Collections.singletonMap("foo_schema", new ShardingSphereSchema()));
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(database.getSchema("foo_schema")).thenReturn(mock(ShardingSphereSchema.class));
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("db", database);
    }
    
    @Test
    void assertReportLocalProcesses() {
        Process process = mock(Process.class);
        String processId = "foo_id";
        when(process.getId()).thenReturn(processId);
        when(process.isInterrupted()).thenReturn(false);
        when(process.isIdle()).thenReturn(false);
        when(process.getCompletedUnitCount()).thenReturn(new AtomicInteger(0));
        when(process.getTotalUnitCount()).thenReturn(new AtomicInteger(0));
        ProcessRegistry.getInstance().add(process);
        String instanceId = contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId();
        subscriber.reportLocalProcesses(new ReportLocalProcessesEvent(instanceId, processId));
        verify(repository).persist("/execution_nodes/foo_id/" + instanceId,
                "processes:" + System.lineSeparator() + "- completedUnitCount: 0" + System.lineSeparator()
                        + "  id: foo_id" + System.lineSeparator()
                        + "  idle: false" + System.lineSeparator()
                        + "  interrupted: false" + System.lineSeparator()
                        + "  startMillis: 0" + System.lineSeparator()
                        + "  totalUnitCount: 0" + System.lineSeparator());
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
        String instanceId = contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId();
        String processId = "foo_id";
        subscriber.killLocalProcess(new KillLocalProcessEvent(instanceId, processId));
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
