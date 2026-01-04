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

import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.type.DefaultDistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistributedLockHolderTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @BeforeEach
    @AfterEach
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    void clearLocks() {
        ((Map<String, DistributedLock>) Plugins.getMemberAccessor().get(DistributedLockHolder.class.getDeclaredField("LOCKS"), null)).clear();
    }
    
    @Test
    void assertGetDistributedLockReturnsRepositoryLock() {
        DistributedLock distributedLock = mock(DistributedLock.class);
        when(repository.getDistributedLock("lock-key")).thenReturn(Optional.of(distributedLock));
        assertThat(DistributedLockHolder.getDistributedLock("lock-key", repository), is(distributedLock));
    }
    
    @Test
    void assertGetDistributedLockCreatesDefaultLock() {
        when(repository.getDistributedLock("lock-key")).thenReturn(Optional.empty());
        DistributedLock actual = DistributedLockHolder.getDistributedLock("lock-key", repository);
        assertThat(actual, isA(DefaultDistributedLock.class));
    }
}
