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

package org.apache.shardingsphere.mode.repository.cluster.etcd.lock;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.lock.UnlockResponse;
import org.apache.shardingsphere.mode.repository.cluster.etcd.props.EtcdProperties;
import org.apache.shardingsphere.mode.repository.cluster.etcd.props.EtcdPropertyKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtcdDistributedLockTest {
    
    @Mock
    private Client client;
    
    @Mock
    private Lock lock;
    
    @Mock
    private Lease lease;
    
    @Mock
    private EtcdProperties etcdProps;
    
    @BeforeEach
    void setUp() {
        when(client.getLockClient()).thenReturn(lock);
        when(client.getLeaseClient()).thenReturn(lease);
        when(etcdProps.getValue(EtcdPropertyKey.TIME_TO_LIVE_SECONDS)).thenReturn(1);
    }
    
    @Test
    void assertTryLockSuccess() {
        when(lease.grant(1)).thenReturn(CompletableFuture.completedFuture(new LeaseGrantResponse(io.etcd.jetcd.api.LeaseGrantResponse.newBuilder().setID(1L).build())));
        when(lock.lock(any(ByteSequence.class), eq(1L))).thenReturn(CompletableFuture.completedFuture(mock(LockResponse.class)));
        assertTrue(new EtcdDistributedLock("foo_lock", client, etcdProps).tryLock(50L));
        ArgumentCaptor<ByteSequence> lockKeyCaptor = ArgumentCaptor.forClass(ByteSequence.class);
        verify(lock).lock(lockKeyCaptor.capture(), eq(1L));
        assertThat(new String(lockKeyCaptor.getValue().getBytes(), StandardCharsets.UTF_8), is("foo_lock"));
    }
    
    @Test
    void assertTryLockInterrupted() throws ExecutionException, InterruptedException {
        CompletableFuture<LeaseGrantResponse> leaseGrantFuture = mock(CompletableFuture.class);
        when(leaseGrantFuture.get()).thenThrow(InterruptedException.class);
        when(lease.grant(1)).thenReturn(leaseGrantFuture);
        assertFalse(new EtcdDistributedLock("foo_lock", client, etcdProps).tryLock(20L));
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }
    
    @Test
    void assertTryLockOnExecutionException() {
        when(lease.grant(1)).thenReturn(CompletableFuture.completedFuture(new LeaseGrantResponse(io.etcd.jetcd.api.LeaseGrantResponse.newBuilder().setID(1L).build())));
        CompletableFuture<LockResponse> lockFuture = new CompletableFuture<>();
        lockFuture.completeExceptionally(new RuntimeException("lock failure"));
        when(lock.lock(any(ByteSequence.class), eq(1L))).thenReturn(lockFuture);
        assertFalse(new EtcdDistributedLock("foo_lock", client, etcdProps).tryLock(10L));
        assertFalse(Thread.currentThread().isInterrupted());
    }
    
    @Test
    void assertUnlockSuccess() {
        when(lock.unlock(any(ByteSequence.class))).thenReturn(CompletableFuture.completedFuture(mock(UnlockResponse.class)));
        new EtcdDistributedLock("foo_lock", client, etcdProps).unlock();
        ArgumentCaptor<ByteSequence> lockKeyCaptor = ArgumentCaptor.forClass(ByteSequence.class);
        verify(lock).unlock(lockKeyCaptor.capture());
        assertThat(new String(lockKeyCaptor.getValue().getBytes(), StandardCharsets.UTF_8), is("foo_lock"));
    }
    
    @Test
    void assertUnlockInterrupted() throws ExecutionException, InterruptedException {
        CompletableFuture<UnlockResponse> unlockFuture = mock(CompletableFuture.class);
        when(unlockFuture.get()).thenThrow(new InterruptedException());
        when(lock.unlock(any(ByteSequence.class))).thenReturn(unlockFuture);
        new EtcdDistributedLock("foo_lock", client, etcdProps).unlock();
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }
}
