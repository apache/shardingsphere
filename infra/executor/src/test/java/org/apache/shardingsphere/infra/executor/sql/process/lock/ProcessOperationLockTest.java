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

package org.apache.shardingsphere.infra.executor.sql.process.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessOperationLockTest {
    
    @Test
    void assertAwaitReturnsWhenReadyToRelease() {
        assertTrue(new ProcessOperationLock(1).awaitDefaultTime(() -> true));
    }
    
    @Test
    void assertAwaitAndBeReleasedByNotify() {
        ProcessOperationLock lock = new ProcessOperationLock(1);
        AtomicBoolean result = new AtomicBoolean(false);
        Thread awaitingThread = new Thread(() -> result.set(lock.awaitDefaultTime(() -> false)));
        awaitingThread.start();
        lock.doNotify();
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1), () -> awaitingThread.join());
        assertTrue(result.get());
    }
}
