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

package org.apache.shardingsphere.mode.manager.cluster.lock;

import org.apache.shardingsphere.mode.lock.GlobalLockDefinition;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalLockPersistServiceTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ClusterPersistRepository repository;
    
    @Test
    void assertTryLock() {
        when(repository.getDistributedLockHolder().getDistributedLock("/lock/exclusive/locks/foo_lock").tryLock(1000L)).thenReturn(true);
        GlobalLockDefinition lockDefinition = new GlobalLockDefinition("foo_lock");
        assertTrue(new GlobalLockPersistService(repository).tryLock(lockDefinition, 1000L));
    }
    
    @Test
    void assertUnlock() {
        GlobalLockDefinition lockDefinition = new GlobalLockDefinition("foo_lock");
        new GlobalLockPersistService(repository).unlock(lockDefinition);
        verify(repository.getDistributedLockHolder().getDistributedLock("/lock/exclusive/locks/foo_lock")).unlock();
    }
}
