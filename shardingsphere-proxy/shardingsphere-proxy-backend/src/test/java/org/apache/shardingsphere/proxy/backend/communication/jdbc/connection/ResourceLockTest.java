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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ResourceLockTest {
    
    @SneakyThrows(value = InterruptedException.class)
    @Test
    public void assertDoAwait() {
        int numberOfThreads = 10;
        ResourceLock resourceLock = new ResourceLock();
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                resourceLock.doAwait();
                counter.incrementAndGet();
                latch.countDown();
            });
        }
        latch.await();
        assertEquals(numberOfThreads, counter.get());
    }
    
    @SneakyThrows(value = InterruptedException.class)
    @Test
    public void assertDoAwaitThrowsException() {
        int numberOfThreads = 10;
        ResourceLock resourceLock = new ResourceLock();
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                resourceLock.doAwait();
                counter.incrementAndGet();
                latch.countDown();
            });
        }
        latch.await(100, TimeUnit.MILLISECONDS);
        service.shutdownNow();
        assertNotEquals(numberOfThreads, counter.get());
    }
    
    @SneakyThrows(value = InterruptedException.class)
    @Test
    public void assertDoNotify() {
        int numberOfThreads = 10;
        ResourceLock resourceLock = new ResourceLock();
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                resourceLock.doAwait();
                counter.incrementAndGet();
                latch.countDown();
                resourceLock.doNotify();
            });
        }
        latch.await();
        assertEquals(numberOfThreads, counter.get());
    }
}
