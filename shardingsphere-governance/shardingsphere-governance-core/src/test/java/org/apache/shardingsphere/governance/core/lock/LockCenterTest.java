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

package org.apache.shardingsphere.governance.core.lock;

import org.apache.shardingsphere.governance.core.event.model.lock.GlobalLockAddedEvent;
import org.apache.shardingsphere.governance.core.lock.node.LockNode;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNodeStatus;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class LockCenterTest {
    
    @Mock
    private RegistryRepository registryRepository;
    
    @Mock
    private RegistryCenter registryCenter;
    
    private LockCenter lockCenter;
    
    @Before
    public void setUp() {
        lockCenter = new LockCenter(registryRepository, registryCenter);
    }
    
    @Test
    public void assertLock() {
        lockCenter.lock(new GlobalLockAddedEvent());
        verify(registryCenter).persistInstanceData(RegistryCenterNodeStatus.LOCKED.toString());
    }
    
    @Test
    public void assertUnlock() {
        lockCenter.lock(new GlobalLockAddedEvent());
        lockCenter.unlock();
        verify(registryCenter).persistInstanceData(RegistryCenterNodeStatus.OK.toString());
    }
    
    @Test
    public void assertTryGlobalLock() {
        lockCenter.tryGlobalLock(50L);
        verify(registryRepository).tryLock(eq(50L), eq(TimeUnit.MILLISECONDS));
    }
    
    @Test
    public void assertReleaseGlobalLock() {
        lockCenter.releaseGlobalLock();
        verify(registryRepository).releaseLock();
        verify(registryRepository).delete(eq(new LockNode().getGlobalLockNodePath()));
    }
    
    @After
    public void tearDown() {
        lockCenter.unlock();
    }
}
