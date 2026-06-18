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

package org.apache.shardingsphere.data.pipeline.cdc;

import io.netty.channel.Channel;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink.PipelineCDCSocketSink;
import org.apache.shardingsphere.data.pipeline.cdc.core.prepare.CDCJobPreparer;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseUtils;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.PipelineRequiredColumnsExtractor;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.swapper.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        PipelineJobIdUtils.class, PipelineProcessConfigurationUtils.class, PipelineDataSourceConfigurationFactory.class,
        OrderedSPILoader.class, PipelineAPIFactory.class, PipelineJobProgressPersistService.class,
        PipelineDistributedBarrier.class, ElasticJobServiceLoader.class, CDCResponseUtils.class
})
@MockitoSettings(strictness = Strictness.LENIENT)
class CDCJobTest {
    
    private static final PipelineContextKey CONTEXT_KEY = new PipelineContextKey("logic_db", InstanceType.PROXY);
    
    @Test
    void assertExecuteWhenStopping() {
        CDCJobConfiguration jobConfig = mockJobConfiguration(
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("logic_tbl", Collections.singletonList(new DataNode("ds_0.tbl_0")))))));
        ShardingContext shardingContext = mockShardingContext("param");
        prepareJobTypeAndContext(jobConfig);
        try (
                MockedConstruction<PipelineProcessConfigurationPersistService> ignored = mockPersistService(
                        new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null))) {
            when(PipelineAPIFactory.getPipelineGovernanceFacade(CONTEXT_KEY)).thenReturn(mock(PipelineGovernanceFacade.class));
            when(PipelineDistributedBarrier.getInstance(CONTEXT_KEY)).thenReturn(mock(PipelineDistributedBarrier.class));
            CDCJob job = new CDCJob(mock(PipelineSink.class));
            job.getJobRunnerManager().stop();
            assertDoesNotThrow(() -> job.execute(shardingContext));
        }
    }
    
    @Test
    void assertExecuteSkipsWhenTasksRunnerMissing() {
        CDCJobConfiguration jobConfig = mockJobConfiguration(
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("logic_tbl", Collections.singletonList(new DataNode("ds_0.tbl_0")))))));
        ShardingContext shardingContext = mockShardingContext("job_param");
        prepareJobTypeAndContext(jobConfig);
        PipelineProcessConfiguration processConfig = new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null);
        try (MockedConstruction<PipelineProcessConfigurationPersistService> ignored = mockPersistService(processConfig)) {
            when(PipelineAPIFactory.getPipelineGovernanceFacade(CONTEXT_KEY)).thenReturn(mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS));
            when(PipelineDistributedBarrier.getInstance(CONTEXT_KEY)).thenReturn(mock(PipelineDistributedBarrier.class));
            when(OrderedSPILoader.getServices(eq(PipelineRequiredColumnsExtractor.class), anyCollection())).thenReturn(Collections.emptyMap());
            when(PipelineDataSourceConfigurationFactory.newInstance(anyString(), anyString())).thenReturn(mock(PipelineDataSourceConfiguration.class));
            CDCJob job = new CDCJob(mock(PipelineSink.class));
            PipelineTasksRunner tasksRunner = mock(PipelineTasksRunner.class);
            when(tasksRunner.getJobItemContext()).thenReturn(mock(org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext.class));
            job.getJobRunnerManager().addTasksRunner(0, tasksRunner);
            try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
                job.execute(shardingContext);
                triggerMocked.verifyNoInteractions();
            }
        }
    }
    
    @Test
    void assertExecuteInitTasksFailureStopsJob() {
        CDCJobConfiguration jobConfig = mockJobConfiguration(
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("logic_tbl", Collections.singletonList(new DataNode("ds_0.tbl_0")))))));
        ShardingContext shardingContext = mockShardingContext("param");
        prepareJobTypeAndContext(jobConfig);
        CDCJobAPI jobAPI = mock(CDCJobAPI.class);
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedConstruction<PipelineProcessConfigurationPersistService> ignoredProcess = mockPersistService(
                        new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null));
                MockedConstruction<CDCJobPreparer> ignoredPreparer = mockConstruction(CDCJobPreparer.class, (mock, context) -> doThrow(RuntimeException.class).when(mock).initTasks(anyCollection()));
                MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING")).thenReturn(jobAPI);
            PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
            when(PipelineAPIFactory.getPipelineGovernanceFacade(CONTEXT_KEY)).thenReturn(governanceFacade);
            when(PipelineDistributedBarrier.getInstance(CONTEXT_KEY)).thenReturn(mock(PipelineDistributedBarrier.class));
            when(OrderedSPILoader.getServices(eq(PipelineRequiredColumnsExtractor.class), anyCollection())).thenReturn(Collections.emptyMap());
            when(PipelineDataSourceConfigurationFactory.newInstance(anyString(), anyString())).thenReturn(mock(PipelineDataSourceConfiguration.class));
            CDCJob job = new CDCJob(mock(PipelineSink.class));
            assertThrows(RuntimeException.class, () -> job.execute(shardingContext));
            verify(governanceFacade.getJobItemFacade().getErrorMessage()).update(eq("foo_job_id"), anyInt(), any(RuntimeException.class));
            verify(jobAPI).disable("foo_job_id");
            jobRegistryMocked.verify(() -> PipelineJobRegistry.stop("foo_job_id"));
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertExecuteInventorySuccessAndSkipIncrementalWhenRunning() {
        CDCJobConfiguration jobConfig = mockJobConfiguration(
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("logic_tbl", Collections.singletonList(new DataNode("ds_0.tbl_0")))))));
        ShardingContext shardingContext = mockShardingContext("jobParam");
        prepareJobTypeAndContext(jobConfig);
        PipelineProcessConfiguration processConfig = new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null),
                new PipelineWriteConfiguration(1, 1, new AlgorithmConfiguration("RATE_LIMIT", new Properties())), new AlgorithmConfiguration("MEMORY", new Properties()));
        when(PipelineAPIFactory.getPipelineGovernanceFacade(CONTEXT_KEY)).thenReturn(mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS));
        when(PipelineDistributedBarrier.getInstance(CONTEXT_KEY)).thenReturn(mock(PipelineDistributedBarrier.class));
        when(PipelineDataSourceConfigurationFactory.newInstance(anyString(), anyString())).thenReturn(mock(PipelineDataSourceConfiguration.class));
        YamlRuleConfiguration ruleConfig = mock(YamlRuleConfiguration.class);
        PipelineRequiredColumnsExtractor extractor = mock(PipelineRequiredColumnsExtractor.class);
        Map<ShardingSphereIdentifier, Collection<String>> requiredColumns = Collections.singletonMap(new ShardingSphereIdentifier("logic_tbl"), Collections.singleton("id"));
        when(extractor.getTableAndRequiredColumnsMap(eq(ruleConfig), anyCollection())).thenReturn(requiredColumns);
        when(OrderedSPILoader.getServices(eq(PipelineRequiredColumnsExtractor.class), anyCollection())).thenReturn(Collections.singletonMap(ruleConfig, extractor));
        AtomicReference<CDCJobItemContext> capturedContext = new AtomicReference<>();
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedConstruction<PipelineProcessConfigurationPersistService> ignoredProcess = mockPersistService(processConfig);
                MockedConstruction<CDCJobPreparer> ignoredPreparer = mockConstruction(CDCJobPreparer.class, (mock, context) -> doAnswer(invocation -> {
                    CDCJobItemContext jobItemContext = ((Collection<CDCJobItemContext>) invocation.getArgument(0)).iterator().next();
                    jobItemContext.getInventoryTasks().add(mockTask(new IngestFinishedPosition(), Collections.emptyList()));
                    jobItemContext.getInventoryTasks().add(mockTask(new IngestPlaceholderPosition(), Collections.singletonList(CompletableFuture.completedFuture(null))));
                    jobItemContext.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK);
                    capturedContext.set(jobItemContext);
                    return null;
                }).when(mock).initTasks(anyCollection()));
                MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(eq(JobRateLimitAlgorithm.class), anyString(), any(Properties.class))).thenReturn(mock(JobRateLimitAlgorithm.class));
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                ExecuteCallback callback = invocation.getArgument(1);
                callback.onSuccess();
                return null;
            });
            new CDCJob(mock(PipelineSink.class)).execute(shardingContext);
        }
        assertThat(capturedContext.get().getStatus(), is(JobStatus.EXECUTE_INCREMENTAL_TASK));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteInventoryFuturesEmptyAndIncrementalSuccessWhenStopping() {
        CDCJobConfiguration jobConfig = mockJobConfiguration(Collections.singletonList(
                new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("logic_tbl", Collections.singletonList(new DataNode("ds_0.tbl_0")))))));
        ShardingContext shardingContext = mockShardingContext("param");
        prepareJobTypeAndContext(jobConfig);
        try (
                MockedConstruction<PipelineProcessConfigurationPersistService> ignoredProcess = mockPersistService(
                        new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null));
                MockedConstruction<CDCJobPreparer> ignoredPreparer = mockConstruction(CDCJobPreparer.class, (mock, context) -> doAnswer(invocation -> {
                    CDCJobItemContext jobItemContext = ((Collection<CDCJobItemContext>) invocation.getArgument(0)).iterator().next();
                    jobItemContext.setStopping(true);
                    jobItemContext.getInventoryTasks().add(mockTask(new IngestFinishedPosition(), Collections.emptyList()));
                    jobItemContext.getIncrementalTasks().add(mockTask(new IngestPlaceholderPosition(), Collections.singletonList(CompletableFuture.completedFuture(null))));
                    return null;
                }).when(mock).initTasks(anyCollection()));
                MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            when(PipelineAPIFactory.getPipelineGovernanceFacade(CONTEXT_KEY)).thenReturn(mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS));
            when(PipelineDistributedBarrier.getInstance(CONTEXT_KEY)).thenReturn(mock(PipelineDistributedBarrier.class));
            when(OrderedSPILoader.getServices(eq(PipelineRequiredColumnsExtractor.class), anyCollection())).thenReturn(Collections.emptyMap());
            when(PipelineDataSourceConfigurationFactory.newInstance(anyString(), anyString())).thenReturn(mock(PipelineDataSourceConfiguration.class));
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                ExecuteCallback callback = invocation.getArgument(1);
                callback.onSuccess();
                return null;
            });
            CDCJob job = new CDCJob(mock(PipelineSink.class));
            job.execute(shardingContext);
            triggerMocked.verify(() -> PipelineExecuteEngine.trigger(anyCollection(), any()));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteIncrementalFailureSendError() {
        CDCJobConfiguration jobConfig = mockJobConfiguration(
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("logic_tbl", Collections.singletonList(new DataNode("ds_0.tbl_0")))))));
        ShardingContext shardingContext = mockShardingContext("param");
        prepareJobTypeAndContext(jobConfig);
        CDCJobAPI jobAPI = mock(CDCJobAPI.class);
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedConstruction<PipelineProcessConfigurationPersistService> ignoredProcess = mockPersistService(
                        new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null));
                MockedConstruction<CDCJobPreparer> ignoredPreparer = mockConstruction(CDCJobPreparer.class, (mock, context) -> doAnswer(invocation -> {
                    CDCJobItemContext jobItemContext = ((Collection<CDCJobItemContext>) invocation.getArgument(0)).iterator().next();
                    jobItemContext.getInventoryTasks().add(mockTask(new IngestPlaceholderPosition(), Collections.singletonList(CompletableFuture.completedFuture(null))));
                    jobItemContext.getIncrementalTasks().add(mockTask(new IngestPlaceholderPosition(), Collections.singletonList(CompletableFuture.completedFuture(null))));
                    return null;
                }).when(mock).initTasks(anyCollection()));
                MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class);
                MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING")).thenReturn(jobAPI);
            PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
            when(PipelineAPIFactory.getPipelineGovernanceFacade(CONTEXT_KEY)).thenReturn(governanceFacade);
            when(PipelineDistributedBarrier.getInstance(CONTEXT_KEY)).thenReturn(mock(PipelineDistributedBarrier.class));
            when(OrderedSPILoader.getServices(eq(PipelineRequiredColumnsExtractor.class), anyCollection())).thenReturn(Collections.emptyMap());
            when(PipelineDataSourceConfigurationFactory.newInstance(anyString(), anyString())).thenReturn(mock(PipelineDataSourceConfiguration.class));
            PipelineCDCSocketSink sink = mock(PipelineCDCSocketSink.class);
            Channel channel = mock(Channel.class);
            when(sink.getChannel()).thenReturn(channel);
            CDCResponse response = mock(CDCResponse.class);
            when(CDCResponseUtils.failed(anyString(), anyString(), anyString())).thenReturn(response);
            AtomicInteger triggerCounter = new AtomicInteger();
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                ExecuteCallback callback = invocation.getArgument(1);
                if (0 == triggerCounter.getAndIncrement()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new RuntimeException("failure"));
                }
                return null;
            });
            CDCJob job = new CDCJob(sink);
            job.execute(shardingContext);
            verify(channel, atLeastOnce()).writeAndFlush(response);
            verify(governanceFacade.getJobItemFacade().getErrorMessage()).update(eq("foo_job_id"), anyInt(), any(RuntimeException.class));
            verify(jobAPI).disable("foo_job_id");
            jobRegistryMocked.verify(() -> PipelineJobRegistry.stop("foo_job_id"));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteIncrementalFailureWithoutSocketSink() {
        CDCJobConfiguration jobConfig = mockJobConfiguration(
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("logic_tbl", Collections.singletonList(new DataNode("ds_0.tbl_0")))))));
        ShardingContext shardingContext = mockShardingContext("param");
        prepareJobTypeAndContext(jobConfig);
        CDCJobAPI jobAPI = mock(CDCJobAPI.class);
        try (
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class);
                MockedConstruction<PipelineProcessConfigurationPersistService> ignoredProcess = mockPersistService(
                        new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null));
                MockedConstruction<CDCJobPreparer> ignoredPreparer = mockConstruction(CDCJobPreparer.class, (mock, context) -> doAnswer(invocation -> {
                    CDCJobItemContext jobItemContext = ((Collection<CDCJobItemContext>) invocation.getArgument(0)).iterator().next();
                    jobItemContext.getInventoryTasks().add(mockTask(new IngestPlaceholderPosition(), Collections.singletonList(CompletableFuture.completedFuture(null))));
                    jobItemContext.getIncrementalTasks().add(mockTask(new IngestPlaceholderPosition(), Collections.singletonList(CompletableFuture.completedFuture(null))));
                    return null;
                }).when(mock).initTasks(anyCollection()));
                MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class);
                MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(TransmissionJobAPI.class, "STREAMING")).thenReturn(jobAPI);
            PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
            when(PipelineAPIFactory.getPipelineGovernanceFacade(CONTEXT_KEY)).thenReturn(governanceFacade);
            when(PipelineDistributedBarrier.getInstance(CONTEXT_KEY)).thenReturn(mock(PipelineDistributedBarrier.class));
            when(OrderedSPILoader.getServices(eq(PipelineRequiredColumnsExtractor.class), anyCollection())).thenReturn(Collections.emptyMap());
            AtomicInteger triggerCounter = new AtomicInteger();
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                ExecuteCallback callback = invocation.getArgument(1);
                if (0 == triggerCounter.getAndIncrement()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new RuntimeException("failure"));
                }
                return null;
            });
            new CDCJob(mock(PipelineSink.class)).execute(shardingContext);
            verify(governanceFacade.getJobItemFacade().getErrorMessage()).update(eq("foo_job_id"), anyInt(), any(RuntimeException.class));
            verify(jobAPI).disable("foo_job_id");
            jobRegistryMocked.verify(() -> PipelineJobRegistry.stop("foo_job_id"));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteIncrementalSkipsFinishedTasks() {
        CDCJobConfiguration jobConfig = mockJobConfiguration(
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("logic_tbl", Collections.singletonList(new DataNode("ds_0.tbl_0")))))));
        ShardingContext shardingContext = mockShardingContext("param");
        prepareJobTypeAndContext(jobConfig);
        PipelineProcessConfiguration processConfig = new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null);
        AtomicReference<PipelineTask> finishedTaskRef = new AtomicReference<>();
        try (
                MockedConstruction<PipelineProcessConfigurationPersistService> ignoredProcess = mockPersistService(processConfig);
                MockedConstruction<CDCJobPreparer> ignoredPreparer = mockConstruction(CDCJobPreparer.class, (mock, context) -> doAnswer(invocation -> {
                    CDCJobItemContext jobItemContext = ((Collection<CDCJobItemContext>) invocation.getArgument(0)).iterator().next();
                    PipelineTask finishedTask = mockTask(new IngestFinishedPosition(), Collections.singletonList(CompletableFuture.completedFuture(null)));
                    finishedTaskRef.set(finishedTask);
                    PipelineTask activeTask = mockTask(new IngestPlaceholderPosition(), Collections.singletonList(CompletableFuture.completedFuture(null)));
                    jobItemContext.getInventoryTasks().add(mockTask(new IngestFinishedPosition(), Collections.emptyList()));
                    jobItemContext.getIncrementalTasks().add(finishedTask);
                    jobItemContext.getIncrementalTasks().add(activeTask);
                    return null;
                }).when(mock).initTasks(anyCollection()));
                MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            new CDCJob(mock(PipelineSink.class)).execute(shardingContext);
            verify(finishedTaskRef.get(), never()).start();
            triggerMocked.verify(() -> PipelineExecuteEngine.trigger(anyCollection(), any()));
        }
    }
    
    private CDCJobConfiguration mockJobConfiguration(final List<JobDataNodeLine> jobShardingDataNodes) {
        CDCJobConfiguration result = mock(CDCJobConfiguration.class);
        when(result.getJobId()).thenReturn("foo_job_id");
        when(result.getJobShardingCount()).thenReturn(jobShardingDataNodes.size());
        when(result.getJobShardingDataNodes()).thenReturn(jobShardingDataNodes);
        ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = mock(ShardingSpherePipelineDataSourceConfiguration.class);
        when(dataSourceConfig.getType()).thenReturn("JDBC");
        when(dataSourceConfig.getParameter()).thenReturn("param");
        when(dataSourceConfig.getRootConfig()).thenReturn(createYAMLRootConfiguration());
        when(result.getDataSourceConfig()).thenReturn(dataSourceConfig);
        return result;
    }
    
    private YamlRootConfiguration createYAMLRootConfiguration() {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName("logic_db");
        result.setDataSources(Collections.singletonMap("ds_0", Collections.emptyMap()));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void prepareJobTypeAndContext(final CDCJobConfiguration jobConfig) {
        YamlPipelineJobConfigurationSwapper jobConfigSwapper = mock(YamlPipelineJobConfigurationSwapper.class);
        when(jobConfigSwapper.swapToObject(anyString())).thenReturn(jobConfig);
        PipelineJobType<?> jobType = mock(PipelineJobType.class, RETURNS_DEEP_STUBS);
        when(jobType.getOption().getYamlJobConfigurationSwapper()).thenReturn(jobConfigSwapper);
        when(jobType.getType()).thenReturn("STREAMING");
        when(PipelineJobIdUtils.parseJobType("foo_job_id")).thenReturn(jobType);
        when(PipelineJobIdUtils.parseContextKey("foo_job_id")).thenReturn(CONTEXT_KEY);
    }
    
    private MockedConstruction<PipelineProcessConfigurationPersistService> mockPersistService(final PipelineProcessConfiguration processConfig) {
        MockedConstruction<PipelineProcessConfigurationPersistService> result = mockConstruction(
                PipelineProcessConfigurationPersistService.class, (mock, context) -> when(mock.load(CONTEXT_KEY, "STREAMING")).thenReturn(processConfig));
        when(PipelineProcessConfigurationUtils.fillInDefaultValue(processConfig)).thenReturn(processConfig);
        return result;
    }
    
    private ShardingContext mockShardingContext(final String jobParameter) {
        ShardingContext result = mock(ShardingContext.class);
        when(result.getJobName()).thenReturn("foo_job_id");
        when(result.getJobParameter()).thenReturn(jobParameter);
        return result;
    }
    
    private PipelineTask mockTask(final IngestPosition position, final Collection<CompletableFuture<?>> futures) {
        PipelineTask result = mock(PipelineTask.class, RETURNS_DEEP_STUBS);
        when(result.getTaskProgress().getPosition()).thenReturn(position);
        when(result.start()).thenReturn(futures);
        return result;
    }
}
