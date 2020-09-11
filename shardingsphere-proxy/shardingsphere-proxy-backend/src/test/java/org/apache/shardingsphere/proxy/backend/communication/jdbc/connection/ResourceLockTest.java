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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceLockTest {
    
    private ResourceLock resourceLock;
    
    private Condition condition;
    
    private Lock lock;
    
    @SneakyThrows(value = {NoSuchFieldException.class, IllegalAccessException.class})
    @Before
    public void setup() {
        resourceLock = new ResourceLock();
        lock = mock(ReentrantLock.class);
        condition = mock(Condition.class);
        Field lockField = ResourceLock.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        lockField.set(resourceLock, lock);
        Field conditionField = ResourceLock.class.getDeclaredField("condition");
        conditionField.setAccessible(true);
        conditionField.set(resourceLock, condition);
    }
    
    @SneakyThrows(value = InterruptedException.class)
    @Test
    public void assertDoAwait() {
        resourceLock.doAwait();
        verify(condition).await(200, TimeUnit.MILLISECONDS);
        verify(lock).lock();
        verify(lock).unlock();
    }
    
    @SneakyThrows(value = InterruptedException.class)
    @Test(expected = InterruptedException.class)
    public void assertDoAwaitThrowsException() {
        when(condition.await(200, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());
        resourceLock.doAwait();
        verify(lock).lock();
        verify(lock).unlock();
    }
    
    @Test
    public void assertDoNotify() {
        resourceLock.doNotify();
        verify(condition).signalAll();
        verify(lock).lock();
        verify(lock).unlock();
    }
}
