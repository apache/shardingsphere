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

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.executor.LifecycleExecutor;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ExecuteEngineTest {
    
    @Test(timeout = 30000)
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
    
    @Test(timeout = 30000)
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
        } catch (InterruptedException e) {
            fail();
        } catch (ExecutionException e) {
            actualCause = e.getCause();
        }
        assertThat(actualCause, is(expectedException));
        shutdownAndAwaitTerminal(executeEngine);
        verify(callback).onFailure(expectedException);
    }
    
    @SneakyThrows({ReflectiveOperationException.class, InterruptedException.class})
    private void shutdownAndAwaitTerminal(final ExecuteEngine executeEngine) {
        Field field = ExecuteEngine.class.getDeclaredField("executorService");
        field.setAccessible(true);
        ExecutorService executorService = (ExecutorService) field.get(executeEngine);
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
    }
}
