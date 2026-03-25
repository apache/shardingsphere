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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClusterExclusiveOperatorContextTest {
    
    @Test
    void assertStartReturnsHandle() {
        StubDistributedLock distributedLock = new StubDistributedLock(true);
        assertTrue(new ClusterExclusiveOperatorContext(new StubClusterPersistRepository(distributedLock)).start("op", 50L).isPresent());
        assertThat(distributedLock.tryLockCallCount, is(1));
    }
    
    @Test
    void assertStartReturnsEmptyWhenKeyExists() {
        StubDistributedLock distributedLock = new StubDistributedLock(true);
        ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new StubClusterPersistRepository(distributedLock));
        ExclusiveLockHandle lockHandle = context.start("op", 50L).orElseThrow(AssertionError::new);
        assertFalse(context.start("op", 50L).isPresent());
        assertThat(distributedLock.tryLockCallCount, is(1));
        lockHandle.close();
    }
    
    @Test
    void assertCloseUnlock() {
        StubDistributedLock distributedLock = new StubDistributedLock(true);
        new ClusterExclusiveOperatorContext(new StubClusterPersistRepository(distributedLock)).start("op", 50L).orElseThrow(AssertionError::new).close();
        assertThat(distributedLock.unlockCallCount, is(1));
    }
    
    @Test
    void assertStartReturnsHandleAfterClose() {
        StubDistributedLock distributedLock = new StubDistributedLock(true, true);
        ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new StubClusterPersistRepository(distributedLock));
        ExclusiveLockHandle firstHandle = context.start("op", 50L).orElseThrow(AssertionError::new);
        firstHandle.close();
        Optional<ExclusiveLockHandle> actual = context.start("op", 50L);
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow(AssertionError::new), not(firstHandle));
    }
    
    @Test
    void assertStartRemovesOperationKeyAfterTryLockFailure() {
        StubDistributedLock distributedLock = new StubDistributedLock(false, true);
        ClusterExclusiveOperatorContext context = new ClusterExclusiveOperatorContext(new StubClusterPersistRepository(distributedLock));
        assertFalse(context.start("op", 50L).isPresent());
        assertTrue(context.start("op", 50L).isPresent());
        assertThat(distributedLock.tryLockCallCount, is(2));
    }
    
    private static final class StubDistributedLock implements DistributedLock {
        
        private final List<Boolean> tryLockResults;
        
        private int tryLockCallCount;
        
        private int unlockCallCount;
        
        private StubDistributedLock(final boolean... tryLockResults) {
            this.tryLockResults = new LinkedList<>();
            for (boolean each : tryLockResults) {
                this.tryLockResults.add(each);
            }
        }
        
        @Override
        public boolean tryLock(final long timeoutMillis) {
            tryLockCallCount++;
            return tryLockResults.remove(0);
        }
        
        @Override
        public void unlock() {
            unlockCallCount++;
        }
    }
    
    private static final class StubClusterPersistRepository implements ClusterPersistRepository {
        
        private final DistributedLock distributedLock;
        
        private StubClusterPersistRepository(final DistributedLock distributedLock) {
            this.distributedLock = distributedLock;
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
            return Optional.of(distributedLock);
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
}
