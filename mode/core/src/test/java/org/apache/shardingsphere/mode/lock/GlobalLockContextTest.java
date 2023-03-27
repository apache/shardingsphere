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

package org.apache.shardingsphere.mode.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalLockContextTest {
    
    private final GlobalLockDefinition lockDefinition = new GlobalLockDefinition("foo_lock");
    
    @Mock
    private LockPersistService<GlobalLockDefinition> lockPersistService;
    
    private GlobalLockContext lockContext;
    
    @BeforeEach
    void init() {
        lockContext = new GlobalLockContext(lockPersistService);
    }
    
    @Test
    void assertTryLock() {
        when(lockPersistService.tryLock(lockDefinition, 3000L)).thenReturn(true);
        assertTrue(lockContext.tryLock(lockDefinition, 3000L));
    }
    
    @Test
    void assertUnlock() {
        lockContext.unlock(lockDefinition);
        verify(lockPersistService).unlock(lockDefinition);
    }
}
