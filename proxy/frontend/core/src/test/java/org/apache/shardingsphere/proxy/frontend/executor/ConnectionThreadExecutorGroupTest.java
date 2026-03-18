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

package org.apache.shardingsphere.proxy.frontend.executor;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionThreadExecutorGroupTest {
    
    @Test
    void assertRegister() {
        int connectionId = 1;
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        assertNotNull(ConnectionThreadExecutorGroup.getInstance().get(connectionId));
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(connectionId);
    }
    
    @Test
    void assertUnregisterWithRegisteredConnectionId() {
        int connectionId = 2;
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(connectionId);
        assertNull(ConnectionThreadExecutorGroup.getInstance().get(connectionId));
    }
    
    @Test
    void assertUnregisterWithoutRegisteredConnectionId() {
        int connectionId = 3;
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(connectionId);
        assertNull(ConnectionThreadExecutorGroup.getInstance().get(connectionId));
    }
    
    @Test
    void assertUnregisterWithInterruptedAwait() throws InterruptedException {
        int connectionId = 4;
        CountDownLatch taskStartedLatch = new CountDownLatch(1);
        CountDownLatch blockTaskLatch = new CountDownLatch(1);
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        ExecutorService executorService = ConnectionThreadExecutorGroup.getInstance().get(connectionId);
        executorService.execute(() -> {
            taskStartedLatch.countDown();
            try {
                blockTaskLatch.await();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        taskStartedLatch.await();
        Thread.currentThread().interrupt();
        try {
            ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(connectionId);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
            blockTaskLatch.countDown();
            executorService.awaitTermination(1L, TimeUnit.SECONDS);
        }
        assertNull(ConnectionThreadExecutorGroup.getInstance().get(connectionId));
    }
}
