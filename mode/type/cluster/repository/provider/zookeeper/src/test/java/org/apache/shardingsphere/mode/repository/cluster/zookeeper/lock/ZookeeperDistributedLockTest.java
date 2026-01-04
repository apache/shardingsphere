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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper.lock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.exception.ZookeeperExceptionHandler;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZookeeperDistributedLockTest {
    
    @Test
    void assertTryLockSuccess() {
        try (
                MockedConstruction<InterProcessMutex> mocked = mockConstruction(InterProcessMutex.class,
                        (constructed, context) -> when(constructed.acquire(200L, TimeUnit.MILLISECONDS)).thenReturn(true))) {
            assertTrue(new ZookeeperDistributedLock("/lock", mock()).tryLock(200L));
            assertDoesNotThrow(() -> verify(mocked.constructed().get(0)).acquire(200L, TimeUnit.MILLISECONDS));
        }
    }
    
    @Test
    void assertTryLockHandlesException() {
        Exception ex = new Exception("failed");
        try (
                MockedStatic<ZookeeperExceptionHandler> mockedStatic = mockStatic(ZookeeperExceptionHandler.class);
                MockedConstruction<InterProcessMutex> ignored = mockConstruction(InterProcessMutex.class,
                        (constructed, context) -> when(constructed.acquire(100L, TimeUnit.MILLISECONDS)).thenThrow(ex))) {
            assertFalse(new ZookeeperDistributedLock("/lock", mock()).tryLock(100L));
            mockedStatic.verify(() -> ZookeeperExceptionHandler.handleException(ex));
        }
    }
    
    @Test
    void assertUnlockSuccess() {
        try (MockedConstruction<InterProcessMutex> mocked = mockConstruction(InterProcessMutex.class)) {
            new ZookeeperDistributedLock("/lock", mock()).unlock();
            assertDoesNotThrow(() -> verify(mocked.constructed().get(0)).release());
        }
    }
    
    @Test
    void assertUnlockHandlesException() {
        Exception ex = new Exception("release failed");
        try (
                MockedStatic<ZookeeperExceptionHandler> mockedStatic = mockStatic(ZookeeperExceptionHandler.class);
                MockedConstruction<InterProcessMutex> ignored = mockConstruction(InterProcessMutex.class, (constructed, context) -> doThrow(ex).when(constructed).release())) {
            new ZookeeperDistributedLock("/lock", mock()).unlock();
            assertDoesNotThrow(() -> mockedStatic.verify(() -> ZookeeperExceptionHandler.handleException(ex)));
        }
    }
}
