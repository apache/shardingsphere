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

package org.apache.shardingsphere.governance.core.lock.service;

import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class LockRegistryServiceTest {
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    private LockRegistryService lockRegistryService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        lockRegistryService = new LockRegistryService(registryCenterRepository);
        Field field = lockRegistryService.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(lockRegistryService, registryCenterRepository);
    }
    
    @Test
    public void assertTryLock() {
        lockRegistryService.tryLock("test", 50L);
        verify(registryCenterRepository).tryLock(LockNode.getLockNodePath("test"), 50L, TimeUnit.MILLISECONDS);
    }
    
    @Test
    public void assertReleaseLock() {
        lockRegistryService.releaseLock("test");
        verify(registryCenterRepository).releaseLock(LockNode.getLockNodePath("test"));
    }
    
    @Test
    public void assertDeleteLockAck() {
        lockRegistryService.deleteLockAck("test");
        verify(registryCenterRepository).delete(anyString());
    }
}
