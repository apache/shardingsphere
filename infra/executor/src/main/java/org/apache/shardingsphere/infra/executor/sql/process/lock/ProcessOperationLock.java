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

import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Process operation lock.
 */
public final class ProcessOperationLock {
    
    private static final long TIMEOUT_MILLIS = 5000L;
    
    private final CountDownLatch latch;
    
    public ProcessOperationLock(final int latchCount) {
        latch = new CountDownLatch(latchCount);
    }
    
    /**
     * Await default time.
     *
     * @param releaseStrategy release strategy
     * @return boolean
     */
    @SneakyThrows(InterruptedException.class)
    public boolean awaitDefaultTime(final ProcessOperationLockReleaseStrategy releaseStrategy) {
        if (releaseStrategy.isReadyToRelease()) {
            return true;
        }
        return latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Notify.
     */
    public void doNotify() {
        latch.countDown();
    }
}
