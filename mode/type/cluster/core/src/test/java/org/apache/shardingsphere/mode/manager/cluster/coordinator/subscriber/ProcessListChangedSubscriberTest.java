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
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.cluster.ClusterContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.KillProcessListIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListTriggerEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.ShowProcessListUnitCompleteEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.util.ReflectionUtil;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.process.ShowProcessListManager;
import org.apache.shardingsphere.mode.process.lock.ShowProcessListSimpleLock;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProcessListChangedSubscriberTest {
    
    private ProcessListChangedSubscriber subscriber;
    
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Before
    public void setUp() throws SQLException {
        contextManager = new ClusterContextManagerBuilder().build(createContextManagerBuilderParameter());
        contextManager.renewMetaDataContexts(new MetaDataContexts(contextManager.getMetaDataContexts().getPersistService(), new ShardingSphereMetaData(createDatabases(),
                contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(new Properties()))));
        subscriber = new ProcessListChangedSubscriber(new RegistryCenter(mock(ClusterPersistRepository.class), new EventBusContext(), mock(ProxyInstanceMetaData.class), null), contextManager);
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("FIXTURE", "", "", new Properties()));
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData("foo_instance_id", 3307);
        return new ContextManagerBuilderParameter(modeConfig, Collections.emptyMap(), Collections.emptyList(), new Properties(), Collections.emptyList(), instanceMetaData, false);
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
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put("db", database);
        return result;
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
            subscriber.completeUnitShowProcessList(new ShowProcessListUnitCompleteEvent(processListId));
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
        subscriber.triggerShowProcessList(new ShowProcessListTriggerEvent(instanceId, processListId));
        ClusterPersistRepository repository = ReflectionUtil.getFieldValue(subscriber, "registryCenter", RegistryCenter.class).getRepository();
        verify(repository).persist("/execution_nodes/foo_process_id/" + instanceId,
                "contexts:" + System.lineSeparator() + "- startTimeMillis: 0" + System.lineSeparator());
        verify(repository).delete("/nodes/compute_nodes/process_trigger/" + instanceId + ":foo_process_id");
    }
    
    @Test
    public void assertKillProcessListId() throws SQLException, NoSuchFieldException, IllegalAccessException {
        String instanceId = contextManager.getInstanceContext().getInstance().getMetaData().getId();
        String processId = "foo_process_id";
        subscriber.killProcessListId(new KillProcessListIdEvent(instanceId, processId));
        ClusterPersistRepository repository = ReflectionUtil.getFieldValue(subscriber, "registryCenter", RegistryCenter.class).getRepository();
        verify(repository).delete("/nodes/compute_nodes/process_kill/" + instanceId + ":foo_process_id");
    }
    
    private void lockAndAwaitDefaultTime(final ShowProcessListSimpleLock lock) {
        lock.lock();
        try {
            lock.awaitDefaultTime();
        } finally {
            lock.unlock();
        }
    }
}
