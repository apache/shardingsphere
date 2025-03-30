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

package org.apache.shardingsphere.mode.manager.cluster.lock.global;

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalLockPersistServiceTest {
    
    @Mock
    private GlobalLock globalLock;
    
    @Mock
    private DistributedLock distributedLock;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ClusterPersistRepository repository;
    
    private GlobalLockPersistService globalLockPersistService;
    
    @BeforeEach
    void setUp() {
        globalLockPersistService = new GlobalLockPersistService(repository);
    }
    
    @Test
    void assertTryLock() {
        mockLock("foo_lock", "/lock/global/locks/foo_lock");
        when(distributedLock.tryLock(1000L)).thenReturn(true);
        GlobalLockDefinition lockDefinition = new GlobalLockDefinition(globalLock);
        assertTrue(globalLockPersistService.tryLock(lockDefinition, 1000L));
    }
    
    @Test
    void assertUnlock() {
        mockLock("bar_lock", "/lock/global/locks/bar_lock");
        GlobalLockDefinition lockDefinition = new GlobalLockDefinition(globalLock);
        globalLockPersistService.unlock(lockDefinition);
        verify(distributedLock).unlock();
    }
    
    private void mockLock(final String lockName, final String lockKey) {
        when(globalLock.getName()).thenReturn(lockName);
        when(repository.getDistributedLock(lockKey)).thenReturn(Optional.of(distributedLock));
    }
}
