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

package org.apache.shardingsphere.data.pipeline.core.job.progress.persist;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({PipelineJobRegistry.class, ThreadLocalRandom.class})
class PipelineJobProgressPersistServiceTest {
    
    @SuppressWarnings("unchecked")
    @AfterEach
    void tearDown() {
        getJobProgressPersistMap().clear();
        clearInvocations(PipelineJobRegistry.class);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<String, Map<Integer, PipelineJobProgressPersistContext>> getJobProgressPersistMap() {
        Field field = PipelineJobProgressPersistService.class.getDeclaredField("JOB_PROGRESS_PERSIST_MAP");
        return (Map<String, Map<Integer, PipelineJobProgressPersistContext>>) Plugins.getMemberAccessor().get(field, PipelineJobProgressPersistService.class);
    }
    
    @Test
    void assertAdd() {
        PipelineJobProgressPersistService.add("foo_id", 1);
        Map<String, Map<Integer, PipelineJobProgressPersistContext>> jobProgressPersistMap = getJobProgressPersistMap();
        assertThat(jobProgressPersistMap.get("foo_id").get(1).getUnhandledEventCount().get(), is(0L));
    }
    
    @Test
    void assertRemove() {
        PipelineJobProgressPersistService.add("foo_id", 1);
        PipelineJobProgressPersistService.remove("foo_id");
        Map<String, Map<Integer, PipelineJobProgressPersistContext>> jobProgressPersistMap = getJobProgressPersistMap();
        assertFalse(jobProgressPersistMap.containsKey("foo_id"));
    }
    
    @Test
    void assertNotifyPersist() {
        PipelineJobProgressPersistService.add("foo_id", 1);
        PipelineJobProgressPersistService.notifyPersist("foo_id", 1);
        Map<String, Map<Integer, PipelineJobProgressPersistContext>> jobProgressPersistMap = getJobProgressPersistMap();
        assertThat(jobProgressPersistMap.get("foo_id").get(1).getUnhandledEventCount().get(), is(1L));
    }
    
    @Test
    void assertPersistNowSkipsWhenUnhandledCountIsZero() {
        PipelineJobProgressPersistService.add("foo_id", 1);
        assertDoesNotThrow(() -> PipelineJobProgressPersistService.persistNow("foo_id", 1));
    }
    
    @SuppressWarnings("rawtypes")
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    void assertPersistJobContextRunnableIteratesEntries() {
        String jobId = "foo_scheduler_job";
        int shardingItem = 2;
        PipelineJobItemContext jobItemContext = mock(PipelineJobItemContext.class);
        when(PipelineJobRegistry.getItemContext(jobId, shardingItem)).thenReturn(Optional.of(jobItemContext));
        PipelineJobType<?> jobType = mock(PipelineJobType.class);
        PipelineJobOption jobOption = mock(PipelineJobOption.class);
        when(jobOption.getYamlJobItemProgressSwapper()).thenReturn(null);
        when(jobType.getOption()).thenReturn(jobOption);
        when(jobType.getType()).thenReturn("TEST");
        ThreadLocalRandom randomMock = mock(ThreadLocalRandom.class);
        when(ThreadLocalRandom.current()).thenReturn(randomMock);
        when(randomMock.nextInt(100)).thenReturn(0);
        try (
                MockedStatic<PipelineJobIdUtils> jobIdUtilsMock = mockStatic(PipelineJobIdUtils.class);
                MockedStatic<TypedSPILoader> typedSpiLoaderStatic = mockStatic(TypedSPILoader.class);
                MockedConstruction<PipelineJobItemManager> mockedConstruction = mockConstruction(PipelineJobItemManager.class,
                        (mock, context) -> doNothing().when(mock).updateProgress(jobItemContext))) {
            jobIdUtilsMock.when(() -> PipelineJobIdUtils.parseJobType(jobId)).thenReturn(jobType);
            typedSpiLoaderStatic.when(() -> TypedSPILoader.getService(PipelineJobType.class, "TEST")).thenReturn(jobType);
            PipelineJobProgressPersistService.add(jobId, shardingItem);
            PipelineJobProgressPersistService.notifyPersist(jobId, shardingItem);
            Class<?> runnableClass = Class.forName(PipelineJobProgressPersistService.class.getName() + "$PersistJobContextRunnable");
            Constructor<?> constructor = runnableClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Runnable runnable = (Runnable) constructor.newInstance();
            assertDoesNotThrow(runnable::run);
            assertThat(getJobProgressPersistMap().get(jobId).get(shardingItem).getUnhandledEventCount().get(), is(0L));
            verify(mockedConstruction.constructed().get(0)).updateProgress(jobItemContext);
        } finally {
            PipelineJobProgressPersistService.remove(jobId);
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertPersistNowUpdatesProgressWhenJobItemContextPresentWithoutLogging() {
        String jobId = "foo_id_success_no_log";
        int shardingItem = 1;
        PipelineJobProgressPersistService.add(jobId, shardingItem);
        PipelineJobProgressPersistService.notifyPersist(jobId, shardingItem);
        PipelineJobItemContext jobItemContext = mock(PipelineJobItemContext.class);
        when(PipelineJobRegistry.getItemContext(jobId, shardingItem)).thenReturn(Optional.of(jobItemContext));
        PipelineJobType<?> jobType = mock(PipelineJobType.class);
        PipelineJobOption jobOption = mock(PipelineJobOption.class);
        when(jobOption.getYamlJobItemProgressSwapper()).thenReturn(null);
        when(jobType.getOption()).thenReturn(jobOption);
        when(jobType.getType()).thenReturn("TEST");
        ThreadLocalRandom randomMock = mock(ThreadLocalRandom.class);
        when(ThreadLocalRandom.current()).thenReturn(randomMock);
        when(randomMock.nextInt(100)).thenReturn(0);
        try (
                MockedStatic<PipelineJobIdUtils> jobIdUtilsMock = mockStatic(PipelineJobIdUtils.class);
                MockedStatic<TypedSPILoader> typedSpiLoaderStatic = mockStatic(TypedSPILoader.class);
                MockedConstruction<PipelineJobItemManager> mockedConstruction = mockConstruction(PipelineJobItemManager.class,
                        (mock, context) -> doNothing().when(mock).updateProgress(jobItemContext))) {
            jobIdUtilsMock.when(() -> PipelineJobIdUtils.parseJobType(jobId)).thenReturn(jobType);
            typedSpiLoaderStatic.when(() -> TypedSPILoader.getService(PipelineJobType.class, "TEST")).thenReturn(jobType);
            PipelineJobProgressPersistService.persistNow(jobId, shardingItem);
            assertThat(getJobProgressPersistMap().get(jobId).get(shardingItem).getUnhandledEventCount().get(), is(0L));
            verify(mockedConstruction.constructed().get(0)).updateProgress(jobItemContext);
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertPersistNowUpdatesProgressWhenLoggingEnabled() {
        String jobId = "foo_id_success_log";
        int shardingItem = 1;
        PipelineJobProgressPersistService.add(jobId, shardingItem);
        PipelineJobProgressPersistService.notifyPersist(jobId, shardingItem);
        PipelineJobItemContext jobItemContext = mock(PipelineJobItemContext.class);
        when(PipelineJobRegistry.getItemContext(jobId, shardingItem)).thenReturn(Optional.of(jobItemContext));
        PipelineJobType<?> jobType = mock(PipelineJobType.class);
        PipelineJobOption jobOption = mock(PipelineJobOption.class);
        when(jobOption.getYamlJobItemProgressSwapper()).thenReturn(null);
        when(jobType.getOption()).thenReturn(jobOption);
        when(jobType.getType()).thenReturn("TEST");
        ThreadLocalRandom randomMock = mock(ThreadLocalRandom.class);
        when(ThreadLocalRandom.current()).thenReturn(randomMock);
        when(randomMock.nextInt(100)).thenReturn(6);
        try (
                MockedStatic<PipelineJobIdUtils> jobIdUtilsMock = mockStatic(PipelineJobIdUtils.class);
                MockedStatic<TypedSPILoader> typedSpiLoaderStatic = mockStatic(TypedSPILoader.class);
                MockedConstruction<PipelineJobItemManager> mockedConstruction = mockConstruction(PipelineJobItemManager.class,
                        (mock, context) -> doNothing().when(mock).updateProgress(jobItemContext))) {
            jobIdUtilsMock.when(() -> PipelineJobIdUtils.parseJobType(jobId)).thenReturn(jobType);
            typedSpiLoaderStatic.when(() -> TypedSPILoader.getService(PipelineJobType.class, "TEST")).thenReturn(jobType);
            PipelineJobProgressPersistService.persistNow(jobId, shardingItem);
            assertThat(getJobProgressPersistMap().get(jobId).get(shardingItem).getUnhandledEventCount().get(), is(0L));
            verify(mockedConstruction.constructed().get(0)).updateProgress(jobItemContext);
        }
    }
    
    @Test
    void assertPersistNowSkipsWhenJobItemContextMissing() {
        String jobId = "foo_id_missing_context";
        int shardingItem = 1;
        PipelineJobProgressPersistService.add(jobId, shardingItem);
        PipelineJobProgressPersistService.notifyPersist(jobId, shardingItem);
        when(PipelineJobRegistry.getItemContext(jobId, shardingItem)).thenReturn(Optional.empty());
        PipelineJobProgressPersistService.persistNow(jobId, shardingItem);
        assertThat(getJobProgressPersistMap().get(jobId).get(shardingItem).getUnhandledEventCount().get(), is(1L));
    }
    
    @Test
    void assertPersistNowHandlesSubsequentExceptionLoggingBranches() {
        String jobId = "foo_id_exception_follow";
        int shardingItem = 1;
        PipelineJobProgressPersistService.add(jobId, shardingItem);
        PipelineJobProgressPersistContext persistContext = getJobProgressPersistMap().get(jobId).get(shardingItem);
        persistContext.getUnhandledEventCount().set(-1L);
        when(PipelineJobRegistry.getItemContext(jobId, shardingItem)).thenReturn(Optional.empty());
        ThreadLocalRandom randomMock = mock(ThreadLocalRandom.class);
        when(ThreadLocalRandom.current()).thenReturn(randomMock);
        when(randomMock.nextInt(60)).thenReturn(5, 4);
        assertDoesNotThrow(() -> PipelineJobProgressPersistService.persistNow(jobId, shardingItem));
        assertDoesNotThrow(() -> PipelineJobProgressPersistService.persistNow(jobId, shardingItem));
        assertDoesNotThrow(() -> PipelineJobProgressPersistService.persistNow(jobId, shardingItem));
        verify(randomMock, times(2)).nextInt(60);
        assertTrue(persistContext.getFirstExceptionLogged().get());
    }
}
