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

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MySQL connection ID registry.
 *
 * <p>
 * Tests the binding between MySQL handshake connection IDs and
 * cluster-unique process IDs. Ensures thread safety and lifecycle cleanup.
 * </p>
 */
@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class MySQLConnectionIdRegistryTest {
    
    private final MySQLConnectionIdRegistry registry = MySQLConnectionIdRegistry.getInstance();
    
    private final Set<Long> registeredConnectionIds = ConcurrentHashMap.newKeySet();
    
    @AfterEach
    void tearDown() {
        for (Long each : registeredConnectionIds) {
            registry.unregister(each);
        }
        registeredConnectionIds.clear();
    }
    
    /**
     * Assert registration stores the mapping correctly.
     */
    @Test
    void assertRegisterMapping() {
        int mysqlConnectionId = 1;
        String processId = "process_cluster_id_001";
        
        register(mysqlConnectionId, processId);
        
        assertThat(registry.getProcessId(mysqlConnectionId), is(processId));
    }
    
    /**
     * Assert unregistration removes the mapping.
     */
    @Test
    void assertUnregisterMapping() {
        int mysqlConnectionId = 2;
        String processId = "process_cluster_id_002";
        
        register(mysqlConnectionId, processId);
        assertThat(registry.getProcessId(mysqlConnectionId), is(processId));
        
        registry.unregister(mysqlConnectionId);
        registeredConnectionIds.remove((long) mysqlConnectionId);
        
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
        
        register(mysqlConnectionId, processId1);
        register(mysqlConnectionId, processId2);
        
        assertThat(registry.getProcessId(mysqlConnectionId), is(processId2));
    }
    
    @Test
    void assertRegisterPersistsToClusterRepository() {
        int mysqlConnectionId = 100;
        String processId = "process_cluster_id_100";
        ClusterPersistRepository repository = mockClusterRepository();
        mockProxyContextRepository(repository);
        
        register(mysqlConnectionId, processId);
        
        assertThat(registry.getProcessId(mysqlConnectionId), is(processId));
        verify(repository).persistEphemeral("/nodes/compute_nodes/proxy_connection_id_mapping/" + mysqlConnectionId, processId);
    }
    
    @Test
    void assertGetFromClusterRepositoryWhenLocalAbsent() {
        int mysqlConnectionId = 101;
        String expectedProcessId = "process_cluster_id_101";
        ClusterPersistRepository repository = mockClusterRepository();
        when(repository.query("/nodes/compute_nodes/proxy_connection_id_mapping/" + mysqlConnectionId)).thenReturn(expectedProcessId);
        mockProxyContextRepository(repository);
        assertThat(registry.getProcessId(mysqlConnectionId), is(expectedProcessId));
    }
    
    @Test
    void assertUnregisterDeletesClusterRepositoryMapping() {
        int mysqlConnectionId = 102;
        String processId = "process_cluster_id_102";
        ClusterPersistRepository repository = mockClusterRepository();
        mockProxyContextRepository(repository);
        
        register(mysqlConnectionId, processId);
        
        registry.unregister(mysqlConnectionId);
        registeredConnectionIds.remove((long) mysqlConnectionId);
        assertThat(registry.getProcessId(mysqlConnectionId), nullValue());
        verify(repository).delete("/nodes/compute_nodes/proxy_connection_id_mapping/" + mysqlConnectionId);
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
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        try {
            Future<?>[] futures = new Future[threadCount];
            for (int i = 0; i < threadCount; i++) {
                int threadId = i;
                futures[i] = executorService.submit(() -> {
                    await(startLatch);
                    for (int j = 0; j < registrationsPerThread; j++) {
                        long connectionId = threadId * 100L + j;
                        String processId = "process_" + threadId + "_" + j;
                        register(connectionId, processId);
                        assertThat(registry.getProcessId(connectionId), is(processId));
                    }
                });
            }
            startLatch.countDown();
            for (Future<?> each : futures) {
                getUnchecked(each);
            }
        } finally {
            executorService.shutdownNow();
        }
    }
    
    /**
     * Assert concurrent unregistration is thread-safe.
     */
    @Test
    void assertConcurrentUnregistration() throws InterruptedException {
        int registeredCount = 100;
        for (int i = 0; i < registeredCount; i++) {
            register(i, "process_" + i);
        }
        
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        try {
            Future<?>[] futures = new Future[threadCount];
            for (int i = 0; i < threadCount; i++) {
                int threadId = i;
                futures[i] = executorService.submit(() -> {
                    await(startLatch);
                    for (int j = 0; j < 10; j++) {
                        long connectionId = threadId * 10L + j;
                        registry.unregister(connectionId);
                        registeredConnectionIds.remove(connectionId);
                        assertThat(registry.getProcessId(connectionId), nullValue());
                    }
                });
            }
            startLatch.countDown();
            for (Future<?> each : futures) {
                getUnchecked(each);
            }
        } finally {
            executorService.shutdownNow();
        }
        for (int i = 0; i < registeredCount; i++) {
            assertThat(registry.getProcessId(i), nullValue());
        }
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
    
    private void register(final long mysqlConnectionId, final String processId) {
        registry.register(mysqlConnectionId, processId);
        registeredConnectionIds.add(mysqlConnectionId);
    }
    
    private void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }
    
    private void getUnchecked(final Future<?> future) {
        try {
            future.get();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        } catch (final ExecutionException ex) {
            Throwable cause = ex.getCause();
            throw cause instanceof RuntimeException ? (RuntimeException) cause : new RuntimeException(cause);
        }
    }
    
    private void mockProxyContextRepository(final ClusterPersistRepository repository) {
        ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance()).thenReturn(proxyContext);
        when(proxyContext.getContextManager().getPersistServiceFacade().getRepository()).thenReturn(repository);
    }
    
    private ClusterPersistRepository mockClusterRepository() {
        return mock(ClusterPersistRepository.class, RETURNS_DEEP_STUBS);
    }
}
