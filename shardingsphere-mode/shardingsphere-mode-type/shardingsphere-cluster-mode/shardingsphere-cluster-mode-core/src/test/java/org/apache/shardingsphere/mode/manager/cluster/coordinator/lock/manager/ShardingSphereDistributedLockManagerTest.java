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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager;

import org.apache.shardingsphere.mode.lock.manager.state.LockStateContext;
import org.apache.shardingsphere.mode.lock.util.TimeoutMilliseconds;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.ShardingSphereDistributedDatabaseLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.ShardingSphereDistributedGlobalLock;
import org.apache.shardingsphere.mode.lock.definition.DatabaseLockDefinition;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereDistributedLockManagerTest {
    
    private final ShardingSphereDistributedLockManager shardingSphereDistributedLockManager = new ShardingSphereDistributedLockManager();
    
    @Before
    public void init() throws ReflectiveOperationException {
        LockStateContext lockStateContext = new LockStateContext();
        Field lockStateContextField = shardingSphereDistributedLockManager.getClass().getDeclaredField("lockStateContext");
        lockStateContextField.setAccessible(true);
        lockStateContextField.set(shardingSphereDistributedLockManager, lockStateContext);
        ShardingSphereDistributedGlobalLock distributedLock = mock(ShardingSphereDistributedGlobalLock.class);
        Field distributedLockField = shardingSphereDistributedLockManager.getClass().getDeclaredField("globalDistributedLock");
        distributedLockField.setAccessible(true);
        distributedLockField.set(shardingSphereDistributedLockManager, distributedLock);
        ShardingSphereDistributedDatabaseLock databaseLock = mock(ShardingSphereDistributedDatabaseLock.class);
        when(databaseLock.tryLock("databaseName", 3000L - TimeoutMilliseconds.DEFAULT_REGISTRY)).thenReturn(true);
        Field databaseLockField = shardingSphereDistributedLockManager.getClass().getDeclaredField("databaseLock");
        databaseLockField.setAccessible(true);
        databaseLockField.set(shardingSphereDistributedLockManager, databaseLock);
    }
    
    @Test
    public void assertGetDistributedLock() {
        assertNotNull(shardingSphereDistributedLockManager.getDistributedLock());
    }
    
    @Test
    public void assertTryReadLock() {
        assertTrue(shardingSphereDistributedLockManager.tryLock(new DatabaseLockDefinition("databaseName"), 3000L));
    }
    
    @Test
    public void assertReleaseLock() {
        shardingSphereDistributedLockManager.releaseLock(new DatabaseLockDefinition("databaseName"));
    }
    
    @Test
    public void assertIsLocked() {
        assertFalse(shardingSphereDistributedLockManager.isLocked(new DatabaseLockDefinition("databaseName")));
    }
}
