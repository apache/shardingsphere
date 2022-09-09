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

import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.infra.util.exception.external.sql.UnsupportedSQLOperationException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingSphereLockContextTest {
    
    public static final long MAX_TRY_LOCK = 3 * 60 * 1000L;
    
    private ShardingSphereLockContext lockContext;
    
    private LockPersistService lockPersistService;
    
    private LockDefinition lockDefinition;
    
    @Before
    public void init() {
        lockDefinition = new ExclusiveLockDefinition("exclusive_lock");
        lockPersistService = mock(LockPersistService.class);
        when(lockPersistService.tryLock(lockDefinition, MAX_TRY_LOCK)).thenReturn(true);
        when(lockPersistService.tryLock(lockDefinition, 3000)).thenReturn(true);
        doAnswer(invocationOnMock -> null).when(lockPersistService).unlock(lockDefinition);
        lockContext = new ShardingSphereLockContext(lockPersistService);
    }
    
    @Test
    public void assertTryLock() {
        assertTrue(lockContext.tryLock(lockDefinition));
    }
    
    @Test
    public void assertTryLockTimeout() {
        assertTrue(lockContext.tryLock(lockDefinition, 3000));
    }
    
    @Test
    public void assertUnlock() {
        lockContext.unlock(lockDefinition);
        verify(lockPersistService).unlock(lockDefinition);
    }
    
    @Test(expected = UnsupportedSQLOperationException.class)
    public void assertIsLocked() {
        assertTrue(lockContext.isLocked(lockDefinition));
    }
}
