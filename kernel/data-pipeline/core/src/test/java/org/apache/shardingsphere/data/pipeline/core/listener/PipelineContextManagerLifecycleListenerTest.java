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

package org.apache.shardingsphere.data.pipeline.core.listener;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNodeWatcher;
import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobStatisticsAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.schedule.spi.CoordinatorRegistryCenterProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineContextManagerLifecycleListenerTest {
    
    @Test
    void assertInitializeWithDefaultDatabase() {
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getPreSelectedDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        new PipelineContextManagerLifecycleListener().onInitialized(contextManager);
    }
    
    @Test
    void assertInitializeAndResumeEnabledJobs() {
        JobConfigurationAPI jobConfigAPI = mock(JobConfigurationAPI.class);
        JobStatisticsAPI jobStatisticsAPI = mock(JobStatisticsAPI.class);
        when(jobStatisticsAPI.getAllJobsBriefInfo()).thenReturn(Arrays.asList(createJobBriefInfo("_internal"), createJobBriefInfo("invalid"),
                createJobBriefInfo("consistency"), createJobBriefInfo("missing"), createJobBriefInfo("disabled"), createJobBriefInfo("enabled")));
        PipelineJobType<?> consistencyCheckJobType = mock(PipelineJobType.class);
        when(consistencyCheckJobType.getType()).thenReturn("CONSISTENCY_CHECK");
        PipelineJobType<?> jobType = mock(PipelineJobType.class);
        when(jobType.getType()).thenReturn("FIXTURE");
        when(jobConfigAPI.getJobConfiguration("missing")).thenThrow(new PipelineJobNotFoundException("missing"));
        JobConfigurationPOJO disabledJobConfig = mock(JobConfigurationPOJO.class);
        when(disabledJobConfig.isDisabled()).thenReturn(true);
        when(jobConfigAPI.getJobConfiguration("disabled")).thenReturn(disabledJobConfig);
        when(jobConfigAPI.getJobConfiguration("enabled")).thenReturn(mock(JobConfigurationPOJO.class));
        ContextManager contextManager = mockContextManager();
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.JDBC);
        try (
                MockedStatic<PipelineContextManager> pipelineContextManager = mockStatic(PipelineContextManager.class);
                MockedStatic<PipelineMetaDataNodeWatcher> watcher = mockStatic(PipelineMetaDataNodeWatcher.class);
                MockedStatic<ElasticJobServiceLoader> serviceLoader = mockStatic(ElasticJobServiceLoader.class);
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<PipelineAPIFactory> pipelineAPIFactory = mockStatic(PipelineAPIFactory.class);
                MockedStatic<PipelineJobIdUtils> jobIdUtils = mockStatic(PipelineJobIdUtils.class);
                MockedConstruction<PipelineJobManager> jobManagerConstruction = mockConstruction(PipelineJobManager.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(CoordinatorRegistryCenterProvider.class, "FIXTURE")).thenReturn(Optional.of(mock(CoordinatorRegistryCenterProvider.class)));
            pipelineAPIFactory.when(() -> PipelineAPIFactory.getJobConfigurationAPI(contextKey)).thenReturn(jobConfigAPI);
            pipelineAPIFactory.when(() -> PipelineAPIFactory.getJobStatisticsAPI(contextKey)).thenReturn(jobStatisticsAPI);
            jobIdUtils.when(() -> PipelineJobIdUtils.parseJobType("invalid")).thenThrow(new IllegalArgumentException("expected"));
            jobIdUtils.when(() -> PipelineJobIdUtils.parseJobType("consistency")).thenReturn(consistencyCheckJobType);
            jobIdUtils.when(() -> PipelineJobIdUtils.parseJobType("missing")).thenReturn(jobType);
            jobIdUtils.when(() -> PipelineJobIdUtils.parseJobType("disabled")).thenReturn(jobType);
            jobIdUtils.when(() -> PipelineJobIdUtils.parseJobType("enabled")).thenReturn(jobType);
            new PipelineContextManagerLifecycleListener().onInitialized(contextManager);
            pipelineContextManager.verify(() -> PipelineContextManager.putContext(contextKey, contextManager));
            watcher.verify(() -> PipelineMetaDataNodeWatcher.init(contextKey));
            serviceLoader.verify(() -> ElasticJobServiceLoader.registerTypedService(ElasticJobListener.class));
            assertThat(jobManagerConstruction.constructed().size(), is(1));
            verify(jobManagerConstruction.constructed().get(0)).resume("enabled");
        }
    }
    
    @Test
    void assertInitializeWithoutCoordinatorRegistryCenterProvider() {
        ContextManager contextManager = mockContextManager();
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.JDBC);
        try (
                MockedStatic<PipelineContextManager> pipelineContextManager = mockStatic(PipelineContextManager.class);
                MockedStatic<PipelineMetaDataNodeWatcher> watcher = mockStatic(PipelineMetaDataNodeWatcher.class);
                MockedStatic<ElasticJobServiceLoader> serviceLoader = mockStatic(ElasticJobServiceLoader.class);
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<PipelineAPIFactory> pipelineAPIFactory = mockStatic(PipelineAPIFactory.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(CoordinatorRegistryCenterProvider.class, "FIXTURE")).thenReturn(Optional.empty());
            new PipelineContextManagerLifecycleListener().onInitialized(contextManager);
            pipelineContextManager.verify(() -> PipelineContextManager.putContext(contextKey, contextManager));
            watcher.verifyNoInteractions();
            serviceLoader.verifyNoInteractions();
            pipelineAPIFactory.verifyNoInteractions();
        }
    }
    
    @Test
    void assertInitializeIgnoresDispatchFailure() {
        ContextManager contextManager = mockContextManager();
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.JDBC);
        try (
                MockedStatic<PipelineContextManager> ignoredContextManager = mockStatic(PipelineContextManager.class);
                MockedStatic<PipelineMetaDataNodeWatcher> ignoredWatcher = mockStatic(PipelineMetaDataNodeWatcher.class);
                MockedStatic<ElasticJobServiceLoader> ignoredServiceLoader = mockStatic(ElasticJobServiceLoader.class);
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedStatic<PipelineAPIFactory> pipelineAPIFactory = mockStatic(PipelineAPIFactory.class)) {
            typedSPILoader.when(() -> TypedSPILoader.findService(CoordinatorRegistryCenterProvider.class, "FIXTURE")).thenReturn(Optional.of(mock(CoordinatorRegistryCenterProvider.class)));
            pipelineAPIFactory.when(() -> PipelineAPIFactory.getJobConfigurationAPI(contextKey)).thenThrow(new RuntimeException("expected"));
            assertDoesNotThrow(() -> new PipelineContextManagerLifecycleListener().onInitialized(contextManager));
        }
    }
    
    @Test
    void assertDestroyReleasesPipelineResourcesBeforeRemovingContext() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getPreSelectedDatabaseName()).thenReturn("foo_db");
        when(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.JDBC);
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.JDBC);
        List<String> invocations = new LinkedList<>();
        try (
                MockedStatic<PipelineAPIFactory> pipelineAPIFactory = mockStatic(PipelineAPIFactory.class);
                MockedStatic<PipelineContextManager> pipelineContextManager = mockStatic(PipelineContextManager.class)) {
            pipelineAPIFactory.when(() -> PipelineAPIFactory.close(contextKey)).thenAnswer(invocation -> invocations.add("close"));
            pipelineContextManager.when(() -> PipelineContextManager.removeContext(contextKey)).thenAnswer(invocation -> invocations.add("remove"));
            new PipelineContextManagerLifecycleListener().onDestroyed(contextManager);
        }
        assertThat(invocations, contains("close", "remove"));
    }
    
    @Test
    void assertDestroyRemovesContextWhenResourceReleaseFailed() {
        ContextManager contextManager = mockContextManager();
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.JDBC);
        RuntimeException expected = new RuntimeException("expected");
        try (
                MockedStatic<PipelineAPIFactory> pipelineAPIFactory = mockStatic(PipelineAPIFactory.class);
                MockedStatic<PipelineContextManager> pipelineContextManager = mockStatic(PipelineContextManager.class)) {
            pipelineAPIFactory.when(() -> PipelineAPIFactory.close(contextKey)).thenThrow(expected);
            assertThat(assertThrows(RuntimeException.class, () -> new PipelineContextManagerLifecycleListener().onDestroyed(contextManager)), sameInstance(expected));
            pipelineContextManager.verify(() -> PipelineContextManager.removeContext(contextKey));
        }
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getPreSelectedDatabaseName()).thenReturn("foo_db");
        when(result.getComputeNodeInstanceContext().getInstance().getMetaData().getType()).thenReturn(InstanceType.JDBC);
        when(result.getComputeNodeInstanceContext().getModeConfiguration().getRepository().getType()).thenReturn("FIXTURE");
        return result;
    }
    
    private JobBriefInfo createJobBriefInfo(final String jobName) {
        JobBriefInfo result = new JobBriefInfo();
        result.setJobName(jobName);
        return result;
    }
}
