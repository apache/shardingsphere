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

package org.apache.shardingsphere.test.it.data.pipeline.core.execute;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.executor.LifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ExecuteEngineTest {
    
    @Test(timeout = 30000L)
    public void assertSubmitAndTaskSucceeded() throws ExecutionException, InterruptedException {
        LifecycleExecutor lifecycleExecutor = mock(LifecycleExecutor.class);
        ExecuteCallback callback = mock(ExecuteCallback.class);
        ExecuteEngine executeEngine = ExecuteEngine.newCachedThreadInstance(ExecuteEngineTest.class.getSimpleName());
        Future<?> future = executeEngine.submit(lifecycleExecutor, callback);
        future.get();
        shutdownAndAwaitTerminal(executeEngine);
        verify(lifecycleExecutor).run();
        verify(callback).onSuccess();
    }
    
    @Test(timeout = 30000L)
    public void assertSubmitAndTaskFailed() {
        LifecycleExecutor lifecycleExecutor = mock(LifecycleExecutor.class);
        RuntimeException expectedException = new RuntimeException("Expected");
        doThrow(expectedException).when(lifecycleExecutor).run();
        ExecuteCallback callback = mock(ExecuteCallback.class);
        ExecuteEngine executeEngine = ExecuteEngine.newCachedThreadInstance(ExecuteEngineTest.class.getSimpleName());
        Future<?> future = executeEngine.submit(lifecycleExecutor, callback);
        Throwable actualCause = null;
        try {
            future.get();
        } catch (final InterruptedException ex) {
            fail();
        } catch (final ExecutionException ex) {
            actualCause = ex.getCause();
        }
        assertThat(actualCause, is(expectedException));
        shutdownAndAwaitTerminal(executeEngine);
        verify(callback).onFailure(expectedException);
    }
    
    @SneakyThrows({ReflectiveOperationException.class, InterruptedException.class})
    private void shutdownAndAwaitTerminal(final ExecuteEngine executeEngine) {
        ExecutorService executorService = (ExecutorService) Plugins.getMemberAccessor().get(ExecuteEngine.class.getDeclaredField("executorService"), executeEngine);
        executorService.shutdown();
        executorService.awaitTermination(30L, TimeUnit.SECONDS);
    }
    
    @Test
    public void assertTriggerAllFailure() throws InterruptedException {
        CompletableFuture<?> future1 = CompletableFuture.runAsync(new FixtureRunnable(false));
        CompletableFuture<?> future2 = CompletableFuture.runAsync(new FixtureRunnable(false));
        FixtureExecuteCallback executeCallback = new FixtureExecuteCallback(2);
        ExecuteEngine.trigger(Arrays.asList(future1, future2), executeCallback);
        executeCallback.latch.await();
        assertThat(executeCallback.successCount.get(), is(0));
        assertThat(executeCallback.failureCount.get(), is(2));
    }
    
    @Test
    public void assertTriggerAllSuccess() throws InterruptedException {
        CompletableFuture<?> future1 = CompletableFuture.runAsync(new FixtureRunnable(true));
        CompletableFuture<?> future2 = CompletableFuture.runAsync(new FixtureRunnable(true));
        FixtureExecuteCallback executeCallback = new FixtureExecuteCallback(1);
        ExecuteEngine.trigger(Arrays.asList(future1, future2), executeCallback);
        executeCallback.latch.await();
        assertThat(executeCallback.successCount.get(), is(1));
        assertThat(executeCallback.failureCount.get(), is(0));
    }
    
    @Test
    public void assertTriggerPartSuccessFailure() throws InterruptedException {
        CompletableFuture<?> future1 = CompletableFuture.runAsync(new FixtureRunnable(true));
        CompletableFuture<?> future2 = CompletableFuture.runAsync(new FixtureRunnable(false));
        FixtureExecuteCallback executeCallback = new FixtureExecuteCallback(1);
        ExecuteEngine.trigger(Arrays.asList(future1, future2), executeCallback);
        executeCallback.latch.await();
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
        
        private final CountDownLatch latch;
        
        private final AtomicInteger successCount = new AtomicInteger(0);
        
        private final AtomicInteger failureCount = new AtomicInteger(0);
        
        FixtureExecuteCallback(final int latchCount) {
            this.latch = new CountDownLatch(latchCount);
        }
        
        @Override
        public void onSuccess() {
            successCount.addAndGet(1);
            latch.countDown();
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            failureCount.addAndGet(1);
            latch.countDown();
        }
    }
}
