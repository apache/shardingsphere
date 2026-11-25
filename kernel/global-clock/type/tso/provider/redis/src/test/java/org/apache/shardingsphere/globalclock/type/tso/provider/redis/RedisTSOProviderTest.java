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

package org.apache.shardingsphere.globalclock.type.tso.provider.redis;

import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisTSOProviderTest {
    
    private static final String LOCK_KEY = "csn_lock";
    
    private final Jedis jedis = mock(Jedis.class);
    
    @Test
    void assertTryLock() {
        when(jedis.set(eq(LOCK_KEY), anyString(), any(SetParams.class))).thenReturn("OK");
        try (MockedConstruction<JedisPool> ignored = mockJedisPool()) {
            assertTrue(createProvider().tryLock(0L));
        }
    }
    
    @Test
    void assertTryLockAfterRetry() {
        when(jedis.set(eq(LOCK_KEY), anyString(), any(SetParams.class))).thenReturn(null, "OK");
        try (MockedConstruction<JedisPool> ignored = mockJedisPool()) {
            assertTrue(createProvider().tryLock(1000L));
        }
    }
    
    @Test
    void assertTryLockWhenRedisLockTimeout() throws ExecutionException, InterruptedException {
        when(jedis.set(eq(LOCK_KEY), anyString(), any(SetParams.class))).thenReturn(null, "OK");
        try (MockedConstruction<JedisPool> ignored = mockJedisPool()) {
            GlobalClockProvider provider = createProvider();
            assertFalse(provider.tryLock(0L));
            assertTrue(CompletableFuture.supplyAsync(() -> provider.tryLock(0L)).get());
        }
    }
    
    @Test
    void assertTryLockWhenRedisFailed() throws ExecutionException, InterruptedException {
        when(jedis.set(eq(LOCK_KEY), anyString(), any(SetParams.class))).thenThrow(JedisConnectionException.class).thenReturn("OK");
        try (MockedConstruction<JedisPool> ignored = mockJedisPool()) {
            GlobalClockProvider provider = createProvider();
            assertThrows(JedisConnectionException.class, () -> provider.tryLock(0L));
            assertTrue(CompletableFuture.supplyAsync(() -> provider.tryLock(0L)).get());
        }
    }
    
    @Test
    void assertTryLockWhenLockedByAnotherThread() throws ExecutionException, InterruptedException {
        when(jedis.set(eq(LOCK_KEY), anyString(), any(SetParams.class))).thenReturn("OK");
        try (MockedConstruction<JedisPool> ignored = mockJedisPool()) {
            GlobalClockProvider provider = createProvider();
            assertTrue(CompletableFuture.supplyAsync(() -> provider.tryLock(0L)).get());
            assertFalse(provider.tryLock(0L));
        }
    }
    
    @Test
    void assertTryLockWhenInterrupted() {
        try (MockedConstruction<JedisPool> ignored = mockJedisPool()) {
            GlobalClockProvider provider = createProvider();
            Thread.currentThread().interrupt();
            assertFalse(provider.tryLock(0L));
            assertTrue(Thread.interrupted());
        }
    }
    
    @Test
    void assertUnlock() throws ExecutionException, InterruptedException {
        when(jedis.set(eq(LOCK_KEY), anyString(), any(SetParams.class))).thenReturn("OK");
        try (MockedConstruction<JedisPool> ignored = mockJedisPool()) {
            GlobalClockProvider provider = createProvider();
            provider.tryLock(0L);
            provider.unlock();
            assertTrue(CompletableFuture.supplyAsync(() -> provider.tryLock(0L)).get());
        }
        ArgumentCaptor<String> lockToken = ArgumentCaptor.forClass(String.class);
        verify(jedis, times(2)).set(eq(LOCK_KEY), lockToken.capture(), any(SetParams.class));
        verify(jedis).eval(anyString(), eq(Collections.singletonList(LOCK_KEY)), eq(Collections.singletonList(lockToken.getAllValues().get(0))));
    }
    
    private MockedConstruction<JedisPool> mockJedisPool() {
        return mockConstruction(JedisPool.class, (mock, context) -> when(mock.getResource()).thenReturn(jedis));
    }
    
    private GlobalClockProvider createProvider() {
        return TypedSPILoader.getService(GlobalClockProvider.class, "TSO.redis", new Properties());
    }
}
