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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Process operation lock.
 */
public final class ProcessOperationLock {
    
    private static final long TIMEOUT_MILLS = 5000L;
    
    private final Lock lock = new ReentrantLock();
    
    private final Condition condition = lock.newCondition();
    
    /**
     * Lock.
     */
    public void lock() {
        lock.lock();
    }
    
    /**
     * Unlock.
     */
    public void unlock() {
        lock.unlock();
    }
    
    /**
     * Await default time.
     *
     * @param releaseStrategy release strategy
     * @return boolean
     */
    @SneakyThrows(InterruptedException.class)
    public boolean awaitDefaultTime(final ProcessOperationLockReleaseStrategy releaseStrategy) {
        while (!releaseStrategy.isReadyToRelease()) {
            if (condition.await(TIMEOUT_MILLS, TimeUnit.MILLISECONDS)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Notify.
     */
    public void doNotify() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
