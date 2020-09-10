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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceLockTest {
    
    private ResourceLock resourceLock;
    
    private Condition condition;
    
    @SneakyThrows(value = {NoSuchFieldException.class, IllegalAccessException.class})
    @Before
    public void setup() {
        resourceLock = new ResourceLock();
        condition = mock(Condition.class);
        Field field = ResourceLock.class.getDeclaredField("condition");
        field.setAccessible(true);
        field.set(resourceLock, condition);
    }
    
    @SneakyThrows(value = InterruptedException.class)
    @Test
    public void assertDoAwaitUntil() {
        resourceLock.doAwaitUntil();
        verify(condition).await(200, TimeUnit.MILLISECONDS);
    }
    
    @SneakyThrows(value = InterruptedException.class)
    @Test(expected = InterruptedException.class)
    public void assertDoAwaitUntilThrowsException() {
        when(condition.await(200, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());
        resourceLock.doAwaitUntil();
    }
    
    @Test
    public void assertDoNotify() {
        resourceLock.doNotify();
        verify(condition).signalAll();
    }
}
