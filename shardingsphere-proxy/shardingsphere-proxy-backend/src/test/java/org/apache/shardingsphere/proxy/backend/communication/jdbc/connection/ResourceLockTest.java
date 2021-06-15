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

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

public final class ResourceLockTest {
    
    @Test
    public void assertDoAwait() {
        ResourceLock resourceLock = new ResourceLock();
        long startTime = System.currentTimeMillis();
        resourceLock.doAwait();
        assertTrue(System.currentTimeMillis() - startTime >= 200L);
    }
    
    @Test
    public void assertDoNotify() {
        ResourceLock resourceLock = new ResourceLock();
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(() -> {
            try {
                Thread.sleep(50L);
            } catch (final InterruptedException ignored) {
            }
            resourceLock.doNotify();
        });
        resourceLock.doAwait();
        assertTrue(System.currentTimeMillis() > startTime);
    }
}
