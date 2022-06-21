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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MutexLockRegistryServiceTest {
    
    @Mock
    private ClusterPersistRepository clusterPersistRepository;
    
    private LockRegistryService lockRegistryService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        lockRegistryService = new MutexLockRegistryService(clusterPersistRepository);
        Field field = lockRegistryService.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(lockRegistryService, clusterPersistRepository);
    }
    
    @Test
    public void assertRemoveLock() {
        lockRegistryService.removeLock("test");
        verify(clusterPersistRepository).delete(LockNodeUtil.generateLockLeasesNodePath("test"));
    }
    
    @Test
    public void assertAckLock() {
        lockRegistryService.ackLock("databaseAckLock", "127.0.0.1@3307");
        verify(clusterPersistRepository).persistEphemeral("databaseAckLock", "127.0.0.1@3307");
    }
    
    @Test
    public void assertReleaseAckLock() {
        lockRegistryService.releaseAckLock("databaseAckLock");
        verify(clusterPersistRepository).delete("databaseAckLock");
    }
}
