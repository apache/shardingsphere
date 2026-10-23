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

package org.apache.shardingsphere.data.pipeline.core.task.runner;

import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({PipelineJobIdUtils.class, TypedSPILoader.class})
@SuppressWarnings("rawtypes")
class TransmissionTasksRunnerTest {
    
    @Test
    void assertStartWithCompletedInventory() {
        PipelineTask inventoryTask = mock(PipelineTask.class);
        when(inventoryTask.getTaskProgress()).thenReturn(new InventoryTaskProgress(new IngestFinishedPosition()));
        TransmissionJobItemContext jobItemContext = createJobItemContext(Collections.singleton(inventoryTask), Collections.emptyList());
        try (MockedConstruction<PipelineJobItemManager> managerMock = mockConstruction(PipelineJobItemManager.class)) {
            new TransmissionTasksRunner(jobItemContext).start();
            verify(managerMock.constructed().get(0)).persistProgress(jobItemContext);
            verify(inventoryTask, never()).start();
        }
    }
    
    private TransmissionJobItemContext createJobItemContext(final Collection<PipelineTask> inventoryTasks, final Collection<PipelineTask> incrementalTasks) {
        TransmissionJobItemContext result = mock(TransmissionJobItemContext.class);
        when(result.getJobId()).thenReturn("foo_job");
        when(result.getInventoryTasks()).thenReturn(inventoryTasks);
        when(result.getIncrementalTasks()).thenReturn(incrementalTasks);
        PipelineJobType<?> jobType = mock(PipelineJobType.class, RETURNS_DEEP_STUBS);
        when(jobType.getType()).thenReturn("FIXTURE");
        when(PipelineJobIdUtils.parseJobType("foo_job")).thenReturn(jobType);
        when((TypedSPI) TypedSPILoader.getService(PipelineJobType.class, "FIXTURE")).thenReturn(jobType);
        return result;
    }
    
    @Test
    void assertStartWithInventoryCompletion() {
        PipelineTask inventoryTask = mock(PipelineTask.class);
        when(inventoryTask.getTaskProgress()).thenReturn(new InventoryTaskProgress(new IngestPlaceholderPosition()));
        TransmissionJobItemContext jobItemContext = createJobItemContext(Collections.singleton(inventoryTask), Collections.emptyList());
        when(jobItemContext.getShardingItem()).thenReturn(1);
        try (
                MockedConstruction<PipelineJobItemManager> ignored = mockConstruction(PipelineJobItemManager.class);
                MockedStatic<PipelineExecuteEngine> engineMock = mockStatic(PipelineExecuteEngine.class);
                MockedStatic<PipelineJobProgressPersistService> persistServiceMock = mockStatic(PipelineJobProgressPersistService.class)) {
            new TransmissionTasksRunner(jobItemContext).start();
            ArgumentCaptor<ExecuteCallback> callbackCaptor = ArgumentCaptor.forClass(ExecuteCallback.class);
            engineMock.verify(() -> PipelineExecuteEngine.trigger(anyCollection(), callbackCaptor.capture()));
            when(inventoryTask.getTaskProgress()).thenReturn(new InventoryTaskProgress(new IngestFinishedPosition()));
            callbackCaptor.getValue().onSuccess();
            verify(inventoryTask).start();
            persistServiceMock.verify(() -> PipelineJobProgressPersistService.persistNow("foo_job", 1));
        }
    }
    
    @Test
    void assertStartWithIncompleteInventory() {
        PipelineTask inventoryTask = mock(PipelineTask.class);
        when(inventoryTask.getTaskProgress()).thenReturn(new InventoryTaskProgress(new IngestPlaceholderPosition()));
        TransmissionJobItemContext jobItemContext = createJobItemContext(Collections.singleton(inventoryTask), Collections.emptyList());
        when(jobItemContext.getShardingItem()).thenReturn(1);
        try (
                MockedConstruction<PipelineJobItemManager> ignored = mockConstruction(PipelineJobItemManager.class);
                MockedStatic<PipelineExecuteEngine> engineMock = mockStatic(PipelineExecuteEngine.class);
                MockedStatic<PipelineJobProgressPersistService> persistServiceMock = mockStatic(PipelineJobProgressPersistService.class)) {
            new TransmissionTasksRunner(jobItemContext).start();
            ArgumentCaptor<ExecuteCallback> callbackCaptor = ArgumentCaptor.forClass(ExecuteCallback.class);
            engineMock.verify(() -> PipelineExecuteEngine.trigger(anyCollection(), callbackCaptor.capture()));
            callbackCaptor.getValue().onSuccess();
            persistServiceMock.verify(() -> PipelineJobProgressPersistService.persistNow("foo_job", 1), never());
        }
    }
    
    @Test
    void assertStartWithDefaultConstructor() {
        TransmissionJobItemContext jobItemContext = createJobItemContext(Collections.emptyList(), Collections.emptyList());
        try (MockedConstruction<PipelineJobItemManager> managerMock = mockConstruction(PipelineJobItemManager.class)) {
            new TransmissionTasksRunner(jobItemContext).start();
            verify(managerMock.constructed().get(0)).persistProgress(jobItemContext);
            verify(jobItemContext, never()).setStatus(JobStatus.FINISHED);
            verify(managerMock.constructed().get(0), never()).updateProgress(jobItemContext);
        }
    }
    
    @Test
    void assertStartWithIncrementalTasks() {
        PipelineTask incrementalTask = mock(PipelineTask.class);
        when(incrementalTask.getTaskProgress()).thenReturn(new InventoryTaskProgress(new IngestPlaceholderPosition()));
        TransmissionJobItemContext jobItemContext = createJobItemContext(Collections.emptyList(), Collections.singleton(incrementalTask));
        when(jobItemContext.getStatus()).thenReturn(JobStatus.RUNNING);
        try (
                MockedConstruction<PipelineJobItemManager> ignored = mockConstruction(PipelineJobItemManager.class);
                MockedStatic<PipelineExecuteEngine> engineMock = mockStatic(PipelineExecuteEngine.class)) {
            new TransmissionTasksRunner(jobItemContext).start();
            verify(incrementalTask).start();
            verify(jobItemContext).setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK);
        }
    }
}
