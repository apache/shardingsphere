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

package org.apache.shardingsphere.mode.manager.standalone.lock;

import org.apache.shardingsphere.mode.lock.LockDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StandaloneLockContextTest {
    
    private final StandaloneLockContext lockContext = new StandaloneLockContext();
    
    @Test
    void assertTryLock() {
        LockDefinition lockDefinition = mock(LockDefinition.class);
        when(lockDefinition.getLockKey()).thenReturn("foo_key");
        assertTrue(lockContext.tryLock(lockDefinition, 100L));
        assertFalse(lockContext.tryLock(lockDefinition, 100L));
    }
    
    @Test
    void assertUnlock() {
        LockDefinition lockDefinition = mock(LockDefinition.class);
        when(lockDefinition.getLockKey()).thenReturn("foo_key");
        lockContext.tryLock(lockDefinition, 100L);
        lockContext.unlock(lockDefinition);
        assertTrue(lockContext.tryLock(lockDefinition, 100L));
    }
}
