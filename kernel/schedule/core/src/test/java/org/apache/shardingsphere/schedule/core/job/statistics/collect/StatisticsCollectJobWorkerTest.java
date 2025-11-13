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

package org.apache.shardingsphere.schedule.core.job.statistics.collect;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings.JobConfigurationAPIImpl;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsCollectJobWorkerTest {
    
    private static AtomicBoolean workerInitialized = new AtomicBoolean(false);
    
    private StatisticsCollectJobWorker jobWorker;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    @SneakyThrows(ReflectiveOperationException.class)
    void setUp() {
        workerInitialized = (AtomicBoolean) Plugins.getMemberAccessor().get(StatisticsCollectJobWorker.class.getDeclaredField("WORKER_INITIALIZED"), StatisticsCollectJobWorker.class);
        jobWorker = new StatisticsCollectJobWorker();
    }
    
    @AfterEach
    void tearDown() {
        workerInitialized.set(false);
        setStaticField("scheduleJobBootstrap", null);
        setStaticField("contextManager", null);
        setStaticField("registryCenter", null);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setStaticField(final String fieldName, final Object value) {
        Plugins.getMemberAccessor().set(StatisticsCollectJobWorker.class.getDeclaredField(fieldName), StatisticsCollectJobWorker.class, value);
    }
    
    @Test
    void assertInitializeTwice() {
        jobWorker.initialize(contextManager);
        jobWorker.initialize(contextManager);
        verify(contextManager.getComputeNodeInstanceContext()).getModeConfiguration();
    }
    
    @Test
    void assertInitializeWithZooKeeperRepository() {
        Properties repositoryProps = PropertiesBuilder.build(new Property("retryIntervalMilliseconds", 200),
                new Property("maxRetries", 4), new Property("timeToLiveSeconds", 5), new Property("operationTimeoutMilliseconds", 800), new Property("digest", "digest"));
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration()).thenReturn(
                new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("ZooKeeper", "namespace", "127.0.0.1:2181", repositoryProps)));
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(
                new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getKey(), "0 0/5 * * * ?"))));
        AtomicReference<JobConfiguration> jobConfigRef = new AtomicReference<>();
        try (
                MockedConstruction<ZookeeperRegistryCenter> registryCenterConstruction = mockConstruction(ZookeeperRegistryCenter.class);
                MockedConstruction<ScheduleJobBootstrap> scheduleJobBootstrapConstruction = mockConstruction(ScheduleJobBootstrap.class,
                        (mock, context) -> jobConfigRef.set((JobConfiguration) context.arguments().get(2)));
                MockedConstruction<JobOperateAPIImpl> jobOperateAPIConstruction = mockConstruction(JobOperateAPIImpl.class)) {
            jobWorker.initialize(contextManager);
            verify(registryCenterConstruction.constructed().get(0)).init();
            verify(scheduleJobBootstrapConstruction.constructed().get(0)).schedule();
            assertThat(jobConfigRef.get().getCron(), is("0 0/5 * * * ?"));
            verify(jobOperateAPIConstruction.constructed().get(0)).trigger("statistics-collect");
        }
    }
    
    @Test
    void assertInitializeWithZooKeeperRepositoryUsingDefaultConfiguration() {
        Properties repositoryProps = PropertiesBuilder.build(new Property("timeToLiveSeconds", 0), new Property("operationTimeoutMilliseconds", 0));
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration()).thenReturn(
                new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("ZooKeeper", "namespace", "127.0.0.1:2181", repositoryProps)));
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(new Properties()));
        try (
                MockedConstruction<ZookeeperRegistryCenter> registryCenterConstruction = mockConstruction(ZookeeperRegistryCenter.class);
                MockedConstruction<ScheduleJobBootstrap> scheduleJobBootstrapConstruction = mockConstruction(ScheduleJobBootstrap.class);
                MockedConstruction<JobOperateAPIImpl> jobOperateAPIConstruction = mockConstruction(JobOperateAPIImpl.class)) {
            jobWorker.initialize(contextManager);
            verify(registryCenterConstruction.constructed().get(0)).init();
            verify(scheduleJobBootstrapConstruction.constructed().get(0)).schedule();
            verify(jobOperateAPIConstruction.constructed().get(0)).trigger("statistics-collect");
        }
    }
    
    @Test
    void assertInitializeWithZooKeeperRepositoryUsingDefaultValues() {
        when(contextManager.getComputeNodeInstanceContext().getModeConfiguration()).thenReturn(
                new ModeConfiguration("Cluster", new ClusterPersistRepositoryConfiguration("ZooKeeper", "namespace", "127.0.0.1:2181", new Properties())));
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(new Properties()));
        try (
                MockedConstruction<ZookeeperRegistryCenter> registryCenterConstruction = mockConstruction(ZookeeperRegistryCenter.class);
                MockedConstruction<ScheduleJobBootstrap> scheduleJobBootstrapConstruction = mockConstruction(ScheduleJobBootstrap.class);
                MockedConstruction<JobOperateAPIImpl> jobOperateAPIConstruction = mockConstruction(JobOperateAPIImpl.class)) {
            jobWorker.initialize(contextManager);
            verify(registryCenterConstruction.constructed().get(0)).init();
            verify(scheduleJobBootstrapConstruction.constructed().get(0)).schedule();
            verify(jobOperateAPIConstruction.constructed().get(0)).trigger("statistics-collect");
        }
    }
    
    @Test
    void assertUpdateJobConfigurationWithNullContextManager() {
        assertDoesNotThrow(() -> jobWorker.updateJobConfiguration());
    }
    
    @Test
    void assertUpdateJobConfiguration() {
        setStaticField("contextManager", contextManager);
        CoordinatorRegistryCenter registryCenter = mock(CoordinatorRegistryCenter.class);
        setStaticField("registryCenter", registryCenter);
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(
                new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getKey(), "invalid"))));
        AtomicReference<Object> constructorRegistryCenter = new AtomicReference<>();
        try (
                MockedConstruction<JobConfigurationAPIImpl> jobConfigurationAPIConstruction = mockConstruction(JobConfigurationAPIImpl.class,
                        (mock, context) -> constructorRegistryCenter.set(context.arguments().get(0)))) {
            jobWorker.updateJobConfiguration();
            assertThat(constructorRegistryCenter.get(), is(registryCenter));
            ArgumentCaptor<JobConfigurationPOJO> argumentCaptor = ArgumentCaptor.forClass(JobConfigurationPOJO.class);
            verify(jobConfigurationAPIConstruction.constructed().get(0)).updateJobConfiguration(argumentCaptor.capture());
            JobConfiguration jobConfiguration = argumentCaptor.getValue().toJobConfiguration();
            assertThat(jobConfiguration.getCron(), is(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getDefaultValue()));
        }
    }
    
    @Test
    void assertUpdateJobConfigurationWithException() {
        setStaticField("contextManager", contextManager);
        setStaticField("registryCenter", mock(CoordinatorRegistryCenter.class));
        when(contextManager.getMetaDataContexts().getMetaData().getTemporaryProps()).thenReturn(
                new TemporaryConfigurationProperties(PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getKey(), "0 0/2 * * * ?"))));
        try (
                MockedConstruction<JobConfigurationAPIImpl> jobConfigurationAPIConstruction = mockConstruction(JobConfigurationAPIImpl.class,
                        (mock, context) -> doThrow(RuntimeException.class).when(mock).updateJobConfiguration(any(JobConfigurationPOJO.class)))) {
            assertDoesNotThrow(() -> jobWorker.updateJobConfiguration());
            verify(jobConfigurationAPIConstruction.constructed().get(0)).updateJobConfiguration(any(JobConfigurationPOJO.class));
        }
    }
    
    @Test
    void assertDestroy() {
        jobWorker.destroy();
        jobWorker.destroy();
        assertNull(getScheduleJobBootstrap());
    }
    
    @Test
    void assertDestroyWhenInitialized() {
        ScheduleJobBootstrap scheduleJobBootstrap = mock(ScheduleJobBootstrap.class);
        setStaticField("scheduleJobBootstrap", scheduleJobBootstrap);
        workerInitialized.set(true);
        jobWorker.destroy();
        verify(scheduleJobBootstrap).shutdown();
        assertNull(getScheduleJobBootstrap());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ScheduleJobBootstrap getScheduleJobBootstrap() {
        return (ScheduleJobBootstrap) Plugins.getMemberAccessor().get(StatisticsCollectJobWorker.class.getDeclaredField("scheduleJobBootstrap"), StatisticsCollectJobWorker.class);
    }
}
