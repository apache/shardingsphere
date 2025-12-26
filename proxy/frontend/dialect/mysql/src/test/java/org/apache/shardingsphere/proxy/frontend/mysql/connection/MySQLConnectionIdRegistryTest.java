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

package org.apache.shardingsphere.proxy.frontend.mysql.connection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for MySQL connection ID registry.
 *
 * <p>
 * Tests the binding between MySQL handshake connection IDs and
 * cluster-unique process IDs. Ensures thread safety and lifecycle cleanup.
 * </p>
 */
class MySQLConnectionIdRegistryTest {
    
    private final MySQLConnectionIdRegistry registry = MySQLConnectionIdRegistry.getInstance();
    
    @AfterEach
    void tearDown() {
        registry.unregister(1);
        registry.unregister(2);
        registry.unregister(3);
        registry.unregister(100);
    }
    
    /**
     * Assert registration stores the mapping correctly.
     */
    @Test
    void assertRegisterMapping() {
        int mysqlConnectionId = 1;
        String processId = "process_cluster_id_001";
        
        registry.register(mysqlConnectionId, processId);
        
        assertThat(registry.getProcessId(mysqlConnectionId), is(processId));
    }
    
    /**
     * Assert unregistration removes the mapping.
     */
    @Test
    void assertUnregisterMapping() {
        int mysqlConnectionId = 2;
        String processId = "process_cluster_id_002";
        
        registry.register(mysqlConnectionId, processId);
        assertThat(registry.getProcessId(mysqlConnectionId), is(processId));
        
        registry.unregister(mysqlConnectionId);
        
        assertThat(registry.getProcessId(mysqlConnectionId), nullValue());
    }
    
    /**
     * Assert registration overwrites previous mapping.
     */
    @Test
    void assertRegisterOverwriteExistingMapping() {
        int mysqlConnectionId = 3;
        String processId1 = "process_v1";
        String processId2 = "process_v2";
        
        registry.register(mysqlConnectionId, processId1);
        registry.register(mysqlConnectionId, processId2);
        
        assertThat(registry.getProcessId(mysqlConnectionId), is(processId2));
    }
    
    /**
     * Assert retrieving non-existent mapping returns null.
     */
    @Test
    void assertGetNonExistentMapping() {
        assertThat(registry.getProcessId(999), nullValue());
    }
    
    /**
     * Assert concurrent registration is thread-safe.
     */
    @Test
    void assertConcurrentRegistration() throws InterruptedException {
        int threadCount = 10;
        int registrationsPerThread = 10;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> runRegisterTask(barrier, threadId, registrationsPerThread, successCount));
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertThat(successCount.get(), is(threadCount * registrationsPerThread));
    }
    
    /**
     * Assert concurrent unregistration is thread-safe.
     */
    @Test
    void assertConcurrentUnregistration() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            registry.register(i, "process_" + i);
        }
        
        int threadCount = 10;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> runUnregisterTask(barrier, threadId, successCount));
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertThat(successCount.get(), is(100));
    }
    
    /**
     * Assert unregistering non-existent mapping is safe.
     */
    @Test
    void assertUnregisterNonExistentMapping() {
        registry.unregister(9999);
        assertThat(registry.getProcessId(9999), nullValue());
    }
    
    /**
     * Assert singleton instance consistency.
     */
    @Test
    void assertSingletonInstance() {
        MySQLConnectionIdRegistry instance1 = MySQLConnectionIdRegistry.getInstance();
        MySQLConnectionIdRegistry instance2 = MySQLConnectionIdRegistry.getInstance();
        
        assertThat(instance1, is(instance2));
    }
    
    private void runRegisterTask(final CyclicBarrier barrier, final int threadId,
                                 final int registrationsPerThread, final AtomicInteger successCount) {
        try {
            barrier.await();
            for (int j = 0; j < registrationsPerThread; j++) {
                int connectionId = threadId * 100 + j;
                String processId = "process_" + threadId + "_" + j;
                registry.register(connectionId, processId);
                assertThat(registry.getProcessId(connectionId), is(processId));
                successCount.incrementAndGet();
            }
        } catch (final InterruptedException | BrokenBarrierException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void runUnregisterTask(final CyclicBarrier barrier, final int threadId,
                                   final AtomicInteger successCount) {
        try {
            barrier.await();
            for (int j = 0; j < 10; j++) {
                int connectionId = threadId * 10 + j;
                registry.unregister(connectionId);
                assertThat(registry.getProcessId(connectionId), nullValue());
                successCount.incrementAndGet();
            }
        } catch (final InterruptedException | BrokenBarrierException ex) {
            throw new RuntimeException(ex);
        }
    }
}
