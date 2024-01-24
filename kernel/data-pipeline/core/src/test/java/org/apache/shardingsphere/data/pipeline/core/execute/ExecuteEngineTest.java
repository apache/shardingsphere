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

package org.apache.shardingsphere.data.pipeline.core.execute;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExecuteEngineTest {
    
    @Test
    void assertSubmitAndTaskSucceeded() {
        PipelineLifecycleRunnable pipelineLifecycleRunnable = mock(PipelineLifecycleRunnable.class);
        ExecuteCallback callback = mock(ExecuteCallback.class);
        ExecuteEngine executeEngine = ExecuteEngine.newCachedThreadInstance(ExecuteEngineTest.class.getSimpleName());
        Future<?> future = executeEngine.submit(pipelineLifecycleRunnable, callback);
        assertTimeout(Duration.ofSeconds(30L), () -> future.get());
        shutdownAndAwaitTerminal(executeEngine);
        verify(pipelineLifecycleRunnable).run();
        verify(callback).onSuccess();
    }
    
    @Test
    void assertSubmitAndTaskFailed() {
        PipelineLifecycleRunnable pipelineLifecycleRunnable = mock(PipelineLifecycleRunnable.class);
        RuntimeException expectedException = new RuntimeException("Expected");
        doThrow(expectedException).when(pipelineLifecycleRunnable).run();
        ExecuteCallback callback = mock(ExecuteCallback.class);
        ExecuteEngine executeEngine = ExecuteEngine.newCachedThreadInstance(ExecuteEngineTest.class.getSimpleName());
        Future<?> future = executeEngine.submit(pipelineLifecycleRunnable, callback);
        Optional<Throwable> actualCause = assertTimeout(Duration.ofSeconds(30L), () -> execute(future));
        assertTrue(actualCause.isPresent());
        assertThat(actualCause.get(), is(expectedException));
        shutdownAndAwaitTerminal(executeEngine);
        verify(callback).onFailure(expectedException);
    }
    
    private Optional<Throwable> execute(final Future<?> future) throws InterruptedException {
        try {
            future.get();
            return Optional.empty();
        } catch (final ExecutionException ex) {
            return Optional.of(ex.getCause());
        }
    }
    
    @SneakyThrows({ReflectiveOperationException.class, InterruptedException.class})
    private void shutdownAndAwaitTerminal(final ExecuteEngine executeEngine) {
        ExecutorService executorService = (ExecutorService) Plugins.getMemberAccessor().get(ExecuteEngine.class.getDeclaredField("executorService"), executeEngine);
        executorService.shutdown();
        executorService.awaitTermination(30L, TimeUnit.SECONDS);
    }
    
    @Test
    void assertTriggerAllSuccess() {
        CompletableFuture<?> future1 = CompletableFuture.runAsync(new FixtureRunnable(true));
        CompletableFuture<?> future2 = CompletableFuture.runAsync(new FixtureRunnable(true));
        FixtureExecuteCallback executeCallback = new FixtureExecuteCallback();
        ExecuteEngine.trigger(Arrays.asList(future1, future2), executeCallback);
        assertThat(executeCallback.successCount.get(), is(1));
        assertThat(executeCallback.failureCount.get(), is(0));
    }
    
    @Test
    void assertTriggerPartSuccessFailure() {
        CompletableFuture<?> future1 = CompletableFuture.runAsync(new FixtureRunnable(true));
        CompletableFuture<?> future2 = CompletableFuture.runAsync(new FixtureRunnable(false));
        FixtureExecuteCallback executeCallback = new FixtureExecuteCallback();
        try {
            ExecuteEngine.trigger(Arrays.asList(future1, future2), executeCallback);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ignored) {
            // CHECKSTYLE:ON
        }
        assertThat(executeCallback.successCount.get(), is(0));
        assertThat(executeCallback.failureCount.get(), is(1));
    }
    
    @RequiredArgsConstructor
    private static final class FixtureRunnable implements Runnable {
        
        private final boolean success;
        
        @Override
        public void run() {
            if (!success) {
                throw new RuntimeException("Failure mock");
            }
        }
    }
    
    private static final class FixtureExecuteCallback implements ExecuteCallback {
        
        private final AtomicInteger successCount = new AtomicInteger(0);
        
        private final AtomicInteger failureCount = new AtomicInteger(0);
        
        @Override
        public void onSuccess() {
            successCount.addAndGet(1);
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            failureCount.addAndGet(1);
        }
    }
}
