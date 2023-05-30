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

package org.apache.shardingsphere.infra.executor.kernel.thread;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class ExecutorServiceManagerTest {
    
    private static final TransmittableThreadLocal<String> TRANSMITTABLE_THREAD_LOCAL = new TransmittableThreadLocal<>();
    
    @Test
    void assertThreadLocalValueChangedForReusedThread() {
        AtomicBoolean finished = new AtomicBoolean(false);
        ExecutorService executorService = new ExecutorServiceManager(1).getExecutorService();
        executorService.submit(() -> {
            TRANSMITTABLE_THREAD_LOCAL.set("foo");
            executorService.submit(() -> assertThat(TRANSMITTABLE_THREAD_LOCAL.get(), is("foo")));
        });
        TRANSMITTABLE_THREAD_LOCAL.set("bar");
        executorService.submit(() -> {
            assertValueChangedInConcurrencyThread();
            finished.set(true);
        });
        assertTimeout(Duration.ofSeconds(1L), () -> assertFinished(finished));
    }
    
    private void assertFinished(final AtomicBoolean finished) {
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).pollInterval(100L, TimeUnit.MILLISECONDS).until(finished::get);
    }
    
    private void assertValueChangedInConcurrencyThread() {
        try {
            assertThat(TRANSMITTABLE_THREAD_LOCAL.get(), is("bar"));
        } catch (final AssertionError ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
