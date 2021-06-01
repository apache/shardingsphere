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

import org.apache.shardingsphere.governance.core.lock.service.LockRegistryService;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSphereDistributeLockTest {
    
    @Mock
    private LockRegistryService lockService;
    
    private final ShardingSphereDistributeLock lock = new ShardingSphereDistributeLock(mock(RegistryCenterRepository.class), 50L);
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        Field field = lock.getClass().getDeclaredField("lockService");
        field.setAccessible(true);
        field.set(lock, lockService);
    }
    
    @Test
    public void assertTryLock() {
        when(lockService.tryLock(eq("test"), eq(50L))).thenReturn(Boolean.TRUE);
        lock.tryLock("test", 50L);
        verify(lockService).tryLock(eq("test"), eq(50L));
    }
    
    @Test
    public void assertReleaseLock() {
        when(lockService.checkUnlockAck("test")).thenReturn(Boolean.TRUE);
        lock.releaseLock("test");
        verify(lockService).checkUnlockAck(eq("test"));
        verify(lockService).releaseLock(eq("test"));
    }
}
