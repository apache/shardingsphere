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

package org.apache.shardingsphere.mode.manager.cluster.exclusive;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.exclusive.ExclusiveLockHandle;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterExclusiveOperatorContextTest {
    
    @Test
    void assertStart() {
        final SharedState sharedState = new SharedState();
        final ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new FreshLockClusterPersistRepository(sharedState, true));
        assertTrue(context.start("op", 50L).isPresent());
        assertThat(sharedState.tryLockCallCount.get(), is(1));
    }
    
    @Test
    void assertStartWhenOperationKeyExists() {
        final SharedState sharedState = new SharedState();
        final ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new FreshLockClusterPersistRepository(sharedState, true));
        final ExclusiveLockHandle lockHandle = context.start("op", 50L).orElseThrow(AssertionError::new);
        try {
            assertFalse(context.start("op", 1L).isPresent());
            assertThat(sharedState.tryLockCallCount.get(), is(1));
        } finally {
            lockHandle.close();
        }
    }
    
    @Test
    void assertStartWaitsUntilOperationKeyReleased() throws InterruptedException, ExecutionException, TimeoutException {
        final SharedState sharedState = new SharedState();
        final ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new FreshLockClusterPersistRepository(sharedState, true, true));
        final ExclusiveLockHandle lockHandle = context.start("op", 50L).orElseThrow(AssertionError::new);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            final Future<Optional<ExclusiveLockHandle>> future = executorService.submit(() -> context.start("op", 500L));
            TimeUnit.MILLISECONDS.sleep(100L);
            assertFalse(future.isDone());
            assertThat(sharedState.tryLockCallCount.get(), is(1));
            lockHandle.close();
            final Optional<ExclusiveLockHandle> actual = future.get(1L, TimeUnit.SECONDS);
            assertTrue(actual.isPresent());
            assertThat(sharedState.tryLockCallCount.get(), is(2));
            actual.orElseThrow(AssertionError::new).close();
        } finally {
            executorService.shutdownNow();
        }
    }
    
    @Test
    void assertClose() {
        final SharedState sharedState = new SharedState();
        new ClusterExclusiveOperatorContext(new FreshLockClusterPersistRepository(sharedState, true)).start("op", 50L).orElseThrow(AssertionError::new).close();
        assertThat(sharedState.unlockCallCount.get(), is(1));
    }
    
    @Test
    void assertCloseOnlyUnlockOnce() {
        final SharedState sharedState = new SharedState();
        final ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new FreshLockClusterPersistRepository(sharedState, true));
        final ExclusiveLockHandle lockHandle = context.start("op", 50L).orElseThrow(AssertionError::new);
        lockHandle.close();
        lockHandle.close();
        assertThat(sharedState.unlockCallCount.get(), is(1));
    }
    
    @Test
    void assertStartAfterClose() {
        final SharedState sharedState = new SharedState();
        final ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new FreshLockClusterPersistRepository(sharedState, true, true));
        final ExclusiveLockHandle firstHandle = context.start("op", 50L).orElseThrow(AssertionError::new);
        firstHandle.close();
        final Optional<ExclusiveLockHandle> actual = context.start("op", 50L);
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow(AssertionError::new), not(firstHandle));
    }
    
    @Test
    void assertStartAfterTryLockFailure() {
        final SharedState sharedState = new SharedState();
        final ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new FreshLockClusterPersistRepository(sharedState, false, true));
        assertFalse(context.start("op", 50L).isPresent());
        assertTrue(context.start("op", 50L).isPresent());
        assertThat(sharedState.tryLockCallCount.get(), is(2));
    }
    
    @Test
    void assertStartWithFreshLockInstances() {
        final SharedState sharedState = new SharedState();
        final FreshLockClusterPersistRepository repository = new FreshLockClusterPersistRepository(sharedState, true, true);
        final ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(repository);
        final ExclusiveLockHandle lockHandle = context.start("op", 50L).orElseThrow(AssertionError::new);
        try {
            assertFalse(context.start("op", 1L).isPresent());
            assertThat(repository.getDistributedLockCallCount.get(), is(1));
        } finally {
            lockHandle.close();
        }
        context.start("op", 50L).orElseThrow(AssertionError::new).close();
        assertThat(repository.getDistributedLockCallCount.get(), is(2));
    }
    
    @Test
    void assertStartWithDefaultDistributedLockFallback() {
        final DefaultLockClusterPersistRepository repository = new DefaultLockClusterPersistRepository();
        final ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(repository);
        final ExclusiveLockHandle lockHandle = context.start("op", 50L).orElseThrow(AssertionError::new);
        try {
            assertFalse(context.start("op", 1L).isPresent());
            assertThat(repository.persistExclusiveEphemeralCallCount.get(), is(1));
        } finally {
            lockHandle.close();
        }
        context.start("op", 50L).orElseThrow(AssertionError::new).close();
        assertThat(repository.persistExclusiveEphemeralCallCount.get(), is(2));
    }
    
    private static final class SharedState {
        
        private final AtomicInteger tryLockCallCount = new AtomicInteger();
        
        private final AtomicInteger unlockCallCount = new AtomicInteger();
        
        private final List<Boolean> tryLockResults = new LinkedList<>();
        
        private void addTryLockResult(final boolean tryLockResult) {
            tryLockResults.add(tryLockResult);
        }
    }
    
    private static final class FreshLockClusterPersistRepository implements ClusterPersistRepository {
        
        private final SharedState sharedState;
        
        private final AtomicInteger getDistributedLockCallCount = new AtomicInteger();
        
        private FreshLockClusterPersistRepository(final SharedState sharedState, final boolean... tryLockResults) {
            this.sharedState = sharedState;
            for (boolean each : tryLockResults) {
                sharedState.addTryLockResult(each);
            }
        }
        
        @Override
        public void init(final ClusterPersistRepositoryConfiguration config, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        }
        
        @Override
        public String query(final String key) {
            return null;
        }
        
        @Override
        public List<String> getChildrenKeys(final String key) {
            return Collections.emptyList();
        }
        
        @Override
        public boolean isExisted(final String key) {
            return false;
        }
        
        @Override
        public void persist(final String key, final String value) {
        }
        
        @Override
        public void update(final String key, final String value) {
        }
        
        @Override
        public void persistEphemeral(final String key, final String value) {
        }
        
        @Override
        public boolean persistExclusiveEphemeral(final String key, final String value) {
            return false;
        }
        
        @Override
        public Optional<DistributedLock> getDistributedLock(final String lockKey) {
            getDistributedLockCallCount.incrementAndGet();
            return Optional.of(new FreshDistributedLock(sharedState));
        }
        
        @Override
        public void delete(final String key) {
        }
        
        @Override
        public void watch(final String key, final DataChangedEventListener listener) {
        }
        
        @Override
        public void removeDataListener(final String key) {
        }
        
        @Override
        public void close() {
        }
        
        @Override
        public String getType() {
            return "STUB";
        }
    }
    
    private static final class FreshDistributedLock implements DistributedLock {
        
        private final SharedState sharedState;
        
        private FreshDistributedLock(final SharedState sharedState) {
            this.sharedState = sharedState;
        }
        
        @Override
        public boolean tryLock(final long timeoutMillis) {
            sharedState.tryLockCallCount.incrementAndGet();
            return sharedState.tryLockResults.remove(0);
        }
        
        @Override
        public void unlock() {
            sharedState.unlockCallCount.incrementAndGet();
        }
    }
    
    private static final class DefaultLockClusterPersistRepository implements ClusterPersistRepository {
        
        private final Set<String> ephemeralKeys = ConcurrentHashMap.newKeySet();
        
        private final AtomicInteger persistExclusiveEphemeralCallCount = new AtomicInteger();
        
        @Override
        public void init(final ClusterPersistRepositoryConfiguration config, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        }
        
        @Override
        public String query(final String key) {
            return null;
        }
        
        @Override
        public List<String> getChildrenKeys(final String key) {
            return Collections.emptyList();
        }
        
        @Override
        public boolean isExisted(final String key) {
            return ephemeralKeys.contains(key);
        }
        
        @Override
        public void persist(final String key, final String value) {
        }
        
        @Override
        public void update(final String key, final String value) {
        }
        
        @Override
        public void persistEphemeral(final String key, final String value) {
            ephemeralKeys.add(key);
        }
        
        @Override
        public boolean persistExclusiveEphemeral(final String key, final String value) {
            persistExclusiveEphemeralCallCount.incrementAndGet();
            return ephemeralKeys.add(key);
        }
        
        @Override
        public Optional<DistributedLock> getDistributedLock(final String lockKey) {
            return Optional.empty();
        }
        
        @Override
        public void delete(final String key) {
            ephemeralKeys.remove(key);
        }
        
        @Override
        public void watch(final String key, final DataChangedEventListener listener) {
        }
        
        @Override
        public void removeDataListener(final String key) {
        }
        
        @Override
        public void close() {
        }
        
        @Override
        public String getType() {
            return "STUB";
        }
    }
}
