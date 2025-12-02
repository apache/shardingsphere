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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.task;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemErrorMessageGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemFacade;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemProcessGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.job.PipelineJobCheckGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.job.PipelineJobFacade;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckProcessContext;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({PipelineAPIFactory.class, PipelineJobIdUtils.class})
class ConsistencyCheckTasksRunnerTest {
    
    private static final String CHECK_JOB_ID = "check_job_id";
    
    private static final String PARENT_JOB_ID = "parent_job_id";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Fixture");
    
    @Test
    void assertStartWhenStopping() {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        jobItemContext.setStopping(true);
        assertDoesNotThrow(new ConsistencyCheckTasksRunner(jobItemContext)::start);
    }
    
    @Test
    void assertStartWhenNotStopping() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenAnswer(invocation -> CompletableFuture.completedFuture(null));
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        PipelineContextKey checkContextKey = new PipelineContextKey("check", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(CHECK_JOB_ID)).thenReturn(checkContextKey);
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(checkContextKey)).thenReturn(governanceFacade);
        PipelineJobItemFacade jobItemFacade = mock(PipelineJobItemFacade.class);
        when(governanceFacade.getJobItemFacade()).thenReturn(jobItemFacade);
        PipelineJobItemProcessGovernanceRepository processRepository = mock(PipelineJobItemProcessGovernanceRepository.class);
        when(jobItemFacade.getProcess()).thenReturn(processRepository);
        try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> null);
            new ConsistencyCheckTasksRunner(jobItemContext).start();
            triggerMocked.verify(() -> PipelineExecuteEngine.trigger(anyCollection(), any()));
        }
        verify(executeEngine).submit(any(PipelineLifecycleRunnable.class));
        verify(processRepository).persist(eq(CHECK_JOB_ID), eq(0), anyString());
    }
    
    @Test
    void assertStop() throws ReflectiveOperationException {
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(createJobItemContext());
        PipelineLifecycleRunnable checkExecutor = mock(PipelineLifecycleRunnable.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("checkExecutor"), runner, checkExecutor);
        runner.stop();
        assertTrue(runner.getJobItemContext().isStopping());
        verify(checkExecutor).stop();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRunBlockingPersistResultWhenNotStopping() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = mock(PipelineJobItemManager.class);
        PipelineProcessConfigurationPersistService processConfigPersistService = mock(PipelineProcessConfigurationPersistService.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobItemManager"), runner, jobItemManager);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("processConfigPersistService"), runner, processConfigPersistService);
        PipelineProcessConfiguration processConfig = new PipelineProcessConfiguration(new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null);
        PipelineJobType<PipelineJobConfiguration> parentJobType = mock(PipelineJobType.class);
        when(parentJobType.getType()).thenReturn("CONSISTENCY_CHECK");
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        when(PipelineJobIdUtils.parseJobType(PARENT_JOB_ID)).thenReturn(parentJobType);
        PipelineJobConfiguration parentJobConfig = mock(PipelineJobConfiguration.class);
        when(parentJobConfig.getJobId()).thenReturn(PARENT_JOB_ID);
        PipelineContextKey parentContextKey = new PipelineContextKey("parent", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(PARENT_JOB_ID)).thenReturn(parentContextKey);
        when(processConfigPersistService.load(parentContextKey, "CONSISTENCY_CHECK")).thenReturn(processConfig);
        Map<String, TableDataConsistencyCheckResult> checkResult = Collections.singletonMap("t_order", new TableDataConsistencyCheckResult(true));
        PipelineDataConsistencyChecker checker = mock(PipelineDataConsistencyChecker.class);
        when(checker.check(jobItemContext.getJobConfig().getAlgorithmTypeName(), jobItemContext.getJobConfig().getAlgorithmProps())).thenReturn(checkResult);
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class);
        PipelineJobFacade jobFacade = mock(PipelineJobFacade.class);
        PipelineJobCheckGovernanceRepository checkRepository = mock(PipelineJobCheckGovernanceRepository.class);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(parentContextKey)).thenReturn(governanceFacade);
        when(governanceFacade.getJobFacade()).thenReturn(jobFacade);
        when(jobFacade.getCheck()).thenReturn(checkRepository);
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenAnswer(invocation -> {
            PipelineLifecycleRunnable runnable = invocation.getArgument(0);
            runnable.start();
            return CompletableFuture.completedFuture(null);
        });
        try (
                MockedConstruction<PipelineJobConfigurationManager> ignore = mockConstruction(PipelineJobConfigurationManager.class,
                        (mock, context) -> when(mock.getJobConfiguration(PARENT_JOB_ID)).thenReturn(parentJobConfig));
                MockedStatic<PipelineProcessConfigurationUtils> processConfigMocked = mockStatic(PipelineProcessConfigurationUtils.class);
                MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            processConfigMocked.when(() -> PipelineProcessConfigurationUtils.fillInDefaultValue(processConfig)).thenReturn(processConfig);
            when(parentJobType.buildDataConsistencyChecker(eq(parentJobConfig), any(TransmissionProcessContext.class), eq(jobItemContext.getProgressContext()))).thenReturn(checker);
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> null);
            runner.start();
            verify(jobItemManager).persistProgress(jobItemContext);
            verify(checkRepository).persistCheckJobResult(PARENT_JOB_ID, CHECK_JOB_ID, checkResult);
            assertThat(jobItemContext.getProgressContext().getCheckEndTimeMillis(), notNullValue());
            runner.stop();
            verify(checker).cancel();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRunBlockingSkipPersistWhenStopping() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = mock(PipelineJobItemManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobItemManager"), runner, jobItemManager);
        PipelineProcessConfigurationPersistService processConfigPersistService = mock(PipelineProcessConfigurationPersistService.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("processConfigPersistService"), runner, processConfigPersistService);
        PipelineProcessConfiguration processConfig = new PipelineProcessConfiguration(
                new PipelineReadConfiguration(1, 1, 1, null), new PipelineWriteConfiguration(1, 1, null), null);
        PipelineDataConsistencyChecker checker = mock(PipelineDataConsistencyChecker.class);
        PipelineJobType<PipelineJobConfiguration> parentJobType = mock(PipelineJobType.class);
        when(parentJobType.getType()).thenReturn("CONSISTENCY_CHECK");
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        when(PipelineJobIdUtils.parseJobType(PARENT_JOB_ID)).thenReturn(parentJobType);
        PipelineJobConfiguration parentJobConfig = mock(PipelineJobConfiguration.class);
        when(parentJobConfig.getJobId()).thenReturn(PARENT_JOB_ID);
        PipelineContextKey parentContextKey = new PipelineContextKey("parent", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(PARENT_JOB_ID)).thenReturn(parentContextKey);
        when(processConfigPersistService.load(parentContextKey, "CONSISTENCY_CHECK")).thenReturn(processConfig);
        Map<String, TableDataConsistencyCheckResult> checkResult = Collections.singletonMap("t_user", new TableDataConsistencyCheckResult(true));
        when(checker.check(jobItemContext.getJobConfig().getAlgorithmTypeName(), jobItemContext.getJobConfig().getAlgorithmProps())).thenAnswer(invocation -> {
            jobItemContext.setStopping(true);
            return checkResult;
        });
        PipelineJobCheckGovernanceRepository checkRepository = mock(PipelineJobCheckGovernanceRepository.class);
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenAnswer(invocation -> {
            PipelineLifecycleRunnable runnable = invocation.getArgument(0);
            runnable.start();
            return CompletableFuture.completedFuture(null);
        });
        try (
                MockedConstruction<PipelineJobConfigurationManager> ignored = mockConstruction(PipelineJobConfigurationManager.class,
                        (mock, context) -> when(mock.getJobConfiguration(PARENT_JOB_ID)).thenReturn(parentJobConfig));
                MockedStatic<PipelineProcessConfigurationUtils> processConfigMocked = mockStatic(PipelineProcessConfigurationUtils.class);
                MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            processConfigMocked.when(() -> PipelineProcessConfigurationUtils.fillInDefaultValue(processConfig)).thenReturn(processConfig);
            when(parentJobType.buildDataConsistencyChecker(eq(parentJobConfig), any(TransmissionProcessContext.class), eq(jobItemContext.getProgressContext()))).thenReturn(checker);
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> null);
            runner.start();
            verify(jobItemManager).persistProgress(jobItemContext);
            verifyNoInteractions(checkRepository);
            assertThat(jobItemContext.getProgressContext().getCheckEndTimeMillis(), notNullValue());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertOnSuccessWhenStopping() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobManager jobManager = mock(PipelineJobManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobManager"), runner, jobManager);
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = mock(PipelineJobItemManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobItemManager"), runner, jobItemManager);
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenReturn(CompletableFuture.completedFuture(null));
        AtomicReference<ExecuteCallback> callbackRef = new AtomicReference<>();
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                callbackRef.set(invocation.getArgument(1));
                return null;
            });
            runner.start();
        }
        ExecuteCallback callback = callbackRef.get();
        jobItemContext.setStopping(true);
        callback.onSuccess();
        assertThat(jobItemContext.getStatus(), is(JobStatus.RUNNING));
        verifyNoInteractions(jobManager);
        verifyNoInteractions(jobItemManager);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertOnSuccessUpdateToFinished() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobManager jobManager = mock(PipelineJobManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobManager"), runner, jobManager);
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = mock(PipelineJobItemManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobItemManager"), runner, jobItemManager);
        PipelineContextKey parentContextKey = new PipelineContextKey("parent_db", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(PARENT_JOB_ID)).thenReturn(parentContextKey);
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(parentContextKey)).thenReturn(governanceFacade);
        PipelineJobFacade jobFacade = mock(PipelineJobFacade.class);
        when(governanceFacade.getJobFacade()).thenReturn(jobFacade);
        PipelineJobCheckGovernanceRepository checkRepository = mock(PipelineJobCheckGovernanceRepository.class);
        when(jobFacade.getCheck()).thenReturn(checkRepository);
        Map<String, TableDataConsistencyCheckResult> checkResult = Collections.singletonMap("t_order", new TableDataConsistencyCheckResult(true));
        when(checkRepository.getCheckJobResult(PARENT_JOB_ID, CHECK_JOB_ID)).thenReturn(checkResult);
        AtomicReference<ExecuteCallback> callbackRef = new AtomicReference<>();
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                callbackRef.set(invocation.getArgument(1));
                return null;
            });
            runner.start();
        }
        ExecuteCallback callback = callbackRef.get();
        callback.onSuccess();
        assertThat(jobItemContext.getStatus(), is(JobStatus.FINISHED));
        verify(jobItemManager).persistProgress(jobItemContext);
        verify(jobManager).stop(CHECK_JOB_ID);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertOnSuccessUpdateToFailure() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobManager jobManager = mock(PipelineJobManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobManager"), runner, jobManager);
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = mock(PipelineJobItemManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobItemManager"), runner, jobItemManager);
        PipelineContextKey parentContextKey = new PipelineContextKey("parent_db", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(PARENT_JOB_ID)).thenReturn(parentContextKey);
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(parentContextKey)).thenReturn(governanceFacade);
        PipelineJobFacade jobFacade = mock(PipelineJobFacade.class);
        when(governanceFacade.getJobFacade()).thenReturn(jobFacade);
        PipelineJobCheckGovernanceRepository checkRepository = mock(PipelineJobCheckGovernanceRepository.class);
        when(jobFacade.getCheck()).thenReturn(checkRepository);
        when(checkRepository.getCheckJobResult(PARENT_JOB_ID, CHECK_JOB_ID)).thenReturn(Collections.emptyMap());
        AtomicReference<ExecuteCallback> callbackRef = new AtomicReference<>();
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                callbackRef.set(invocation.getArgument(1));
                return null;
            });
            runner.start();
        }
        ExecuteCallback callback = callbackRef.get();
        callback.onSuccess();
        assertThat(jobItemContext.getStatus(), is(JobStatus.CONSISTENCY_CHECK_FAILURE));
        verify(jobItemManager).persistProgress(jobItemContext);
        verify(jobManager).stop(CHECK_JOB_ID);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertOnSuccessUpdateToFailureWhenResultContainsFailedItem() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobManager jobManager = mock(PipelineJobManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobManager"), runner, jobManager);
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = mock(PipelineJobItemManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobItemManager"), runner, jobItemManager);
        PipelineContextKey checkContextKey = new PipelineContextKey("check_db_fail", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(CHECK_JOB_ID)).thenReturn(checkContextKey);
        PipelineContextKey parentContextKey = new PipelineContextKey("parent_db", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(PARENT_JOB_ID)).thenReturn(parentContextKey);
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        PipelineGovernanceFacade checkGovernanceFacade = mock(PipelineGovernanceFacade.class);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(checkContextKey)).thenReturn(checkGovernanceFacade);
        PipelineJobItemFacade jobItemFacade = mock(PipelineJobItemFacade.class);
        PipelineJobItemProcessGovernanceRepository processRepository = mock(PipelineJobItemProcessGovernanceRepository.class);
        when(jobItemFacade.getProcess()).thenReturn(processRepository);
        when(checkGovernanceFacade.getJobItemFacade()).thenReturn(jobItemFacade);
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(parentContextKey)).thenReturn(governanceFacade);
        PipelineJobFacade jobFacade = mock(PipelineJobFacade.class);
        when(governanceFacade.getJobFacade()).thenReturn(jobFacade);
        PipelineJobCheckGovernanceRepository checkRepository = mock(PipelineJobCheckGovernanceRepository.class);
        when(jobFacade.getCheck()).thenReturn(checkRepository);
        Map<String, TableDataConsistencyCheckResult> checkResult = Collections.singletonMap("t_order", new TableDataConsistencyCheckResult(false));
        when(checkRepository.getCheckJobResult(PARENT_JOB_ID, CHECK_JOB_ID)).thenReturn(checkResult);
        AtomicReference<ExecuteCallback> callbackRef = new AtomicReference<>();
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                callbackRef.set(invocation.getArgument(1));
                return null;
            });
            runner.start();
        }
        ExecuteCallback callback = callbackRef.get();
        callback.onSuccess();
        assertThat(jobItemContext.getStatus(), is(JobStatus.CONSISTENCY_CHECK_FAILURE));
        verify(jobItemManager).persistProgress(jobItemContext);
        verify(jobManager).stop(CHECK_JOB_ID);
    }
    
    @Test
    void assertOnFailureWhenCheckerCanceling() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobManager jobManager = mock(PipelineJobManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobManager"), runner, jobManager);
        PipelineDataConsistencyChecker checker = mock(PipelineDataConsistencyChecker.class);
        when(checker.isCanceling()).thenReturn(true);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("consistencyChecker"), runner, new AtomicReference<>(checker));
        AtomicReference<ExecuteCallback> callbackRef = new AtomicReference<>();
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenReturn(CompletableFuture.completedFuture(null));
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                callbackRef.set(invocation.getArgument(1));
                return null;
            });
            runner.start();
        }
        ExecuteCallback callback = callbackRef.get();
        callback.onFailure(new RuntimeException("cancel"));
        verify(jobManager).stop(CHECK_JOB_ID);
    }
    
    @Test
    void assertOnFailurePersistError() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobManager jobManager = mock(PipelineJobManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobManager"), runner, jobManager);
        PipelineDataConsistencyChecker checker = mock(PipelineDataConsistencyChecker.class);
        when(checker.isCanceling()).thenReturn(false);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("consistencyChecker"), runner, new AtomicReference<>(checker));
        PipelineContextKey checkContextKey = new PipelineContextKey("check_db", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(CHECK_JOB_ID)).thenReturn(checkContextKey);
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(checkContextKey)).thenReturn(governanceFacade);
        PipelineJobItemFacade jobItemFacade = mock(PipelineJobItemFacade.class);
        when(governanceFacade.getJobItemFacade()).thenReturn(jobItemFacade);
        PipelineJobItemErrorMessageGovernanceRepository errorRepository = mock(PipelineJobItemErrorMessageGovernanceRepository.class);
        PipelineJobItemProcessGovernanceRepository processRepository = mock(PipelineJobItemProcessGovernanceRepository.class);
        when(jobItemFacade.getProcess()).thenReturn(processRepository);
        when(jobItemFacade.getErrorMessage()).thenReturn(errorRepository);
        AtomicReference<ExecuteCallback> callbackRef = new AtomicReference<>();
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                callbackRef.set(invocation.getArgument(1));
                return null;
            });
            runner.start();
        }
        ExecuteCallback callback = callbackRef.get();
        RuntimeException failure = new RuntimeException("failure");
        callback.onFailure(failure);
        verify(errorRepository).update(CHECK_JOB_ID, 0, failure);
        verify(jobManager).stop(CHECK_JOB_ID);
    }
    
    @Test
    void assertOnFailurePersistErrorWhenCheckerAbsent() throws ReflectiveOperationException {
        ConsistencyCheckJobItemContext jobItemContext = createJobItemContext();
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(jobItemContext);
        PipelineJobManager jobManager = mock(PipelineJobManager.class);
        Plugins.getMemberAccessor().set(ConsistencyCheckTasksRunner.class.getDeclaredField("jobManager"), runner, jobManager);
        PipelineContextKey checkContextKey = new PipelineContextKey("check_db_null_checker", InstanceType.JDBC);
        when(PipelineJobIdUtils.parseContextKey(CHECK_JOB_ID)).thenReturn(checkContextKey);
        when(PipelineJobIdUtils.parseJobType(CHECK_JOB_ID)).thenReturn(new ConsistencyCheckJobType());
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(checkContextKey)).thenReturn(governanceFacade);
        PipelineJobItemFacade jobItemFacade = mock(PipelineJobItemFacade.class);
        PipelineJobItemProcessGovernanceRepository processRepository = mock(PipelineJobItemProcessGovernanceRepository.class);
        when(jobItemFacade.getProcess()).thenReturn(processRepository);
        when(governanceFacade.getJobItemFacade()).thenReturn(jobItemFacade);
        PipelineJobItemErrorMessageGovernanceRepository errorRepository = mock(PipelineJobItemErrorMessageGovernanceRepository.class);
        when(jobItemFacade.getErrorMessage()).thenReturn(errorRepository);
        AtomicReference<ExecuteCallback> callbackRef = new AtomicReference<>();
        ConsistencyCheckProcessContext processContext = mock(ConsistencyCheckProcessContext.class);
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        when(processContext.getConsistencyCheckExecuteEngine()).thenReturn(executeEngine);
        Plugins.getMemberAccessor().set(ConsistencyCheckJobItemContext.class.getDeclaredField("processContext"), jobItemContext, processContext);
        when(executeEngine.submit(any(PipelineLifecycleRunnable.class))).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedStatic<PipelineExecuteEngine> triggerMocked = mockStatic(PipelineExecuteEngine.class)) {
            triggerMocked.when(() -> PipelineExecuteEngine.trigger(anyCollection(), any())).thenAnswer(invocation -> {
                callbackRef.set(invocation.getArgument(1));
                return null;
            });
            runner.start();
        }
        ExecuteCallback callback = callbackRef.get();
        RuntimeException failure = new RuntimeException("failure");
        callback.onFailure(failure);
        verify(errorRepository).update(CHECK_JOB_ID, 0, failure);
        verify(jobManager).stop(CHECK_JOB_ID);
    }
    
    @Test
    void assertStopWithoutConsistencyChecker() throws ReflectiveOperationException {
        ConsistencyCheckTasksRunner runner = new ConsistencyCheckTasksRunner(createJobItemContext());
        PipelineLifecycleRunnable checkExecutor = (PipelineLifecycleRunnable) Plugins.getMemberAccessor().get(ConsistencyCheckTasksRunner.class.getDeclaredField("checkExecutor"), runner);
        markLifecycleRunnableRunning(checkExecutor);
        runner.stop();
        assertTrue(runner.getJobItemContext().isStopping());
    }
    
    private ConsistencyCheckJobItemContext createJobItemContext() {
        ConsistencyCheckJobConfiguration jobConfig = new ConsistencyCheckJobConfiguration(CHECK_JOB_ID, PARENT_JOB_ID, "FIXTURE", new Properties(), databaseType);
        return new ConsistencyCheckJobItemContext(jobConfig, 0, JobStatus.RUNNING, null);
    }
    
    @SuppressWarnings("unchecked")
    private void markLifecycleRunnableRunning(final PipelineLifecycleRunnable runnable) throws ReflectiveOperationException {
        AtomicReference<Boolean> running = (AtomicReference<Boolean>) Plugins.getMemberAccessor().get(AbstractPipelineLifecycleRunnable.class.getDeclaredField("running"), runnable);
        running.set(true);
        Plugins.getMemberAccessor().set(AbstractPipelineLifecycleRunnable.class.getDeclaredField("startTimeMillis"), runnable, System.currentTimeMillis());
    }
}
