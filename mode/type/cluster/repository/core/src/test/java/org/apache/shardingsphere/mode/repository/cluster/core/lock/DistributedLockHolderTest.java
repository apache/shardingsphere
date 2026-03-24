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

package org.apache.shardingsphere.mode.repository.cluster.core.lock;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.type.DefaultDistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;

class DistributedLockHolderTest {
    
    @Test
    void assertGetDistributedLockReturnsRepositoryLock() {
        DistributedLock firstLock = new StubDistributedLock();
        DistributedLock secondLock = new StubDistributedLock();
        StubClusterPersistRepository repository = new StubClusterPersistRepository(Optional.of(firstLock), Optional.of(secondLock));
        assertThat(DistributedLockHolder.getDistributedLock("lock-key", repository), is(firstLock));
        assertThat(DistributedLockHolder.getDistributedLock("lock-key", repository), is(secondLock));
    }
    
    @Test
    void assertGetDistributedLockCreatesDefaultLockEachTime() {
        StubClusterPersistRepository repository = new StubClusterPersistRepository(Optional.empty(), Optional.empty());
        DistributedLock actual = DistributedLockHolder.getDistributedLock("lock-key", repository);
        DistributedLock another = DistributedLockHolder.getDistributedLock("lock-key", repository);
        assertThat(actual, isA(DefaultDistributedLock.class));
        assertThat(another, isA(DefaultDistributedLock.class));
        assertThat(another, not(actual));
    }
    
    private static final class StubDistributedLock implements DistributedLock {
        
        @Override
        public boolean tryLock(final long timeoutMillis) {
            return true;
        }
        
        @Override
        public void unlock() {
        }
    }
    
    private static final class StubClusterPersistRepository implements ClusterPersistRepository {
        
        private final List<Optional<DistributedLock>> distributedLocks;
        
        private int index;
        
        private StubClusterPersistRepository(final Optional<DistributedLock> firstLock, final Optional<DistributedLock> secondLock) {
            distributedLocks = Arrays.asList(firstLock, secondLock);
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
            Optional<DistributedLock> result = distributedLocks.get(index);
            if (index < distributedLocks.size() - 1) {
                index++;
            }
            return result;
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
