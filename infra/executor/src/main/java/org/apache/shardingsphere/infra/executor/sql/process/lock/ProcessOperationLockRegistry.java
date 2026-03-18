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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process operation lock registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessOperationLockRegistry {
    
    private static final ProcessOperationLockRegistry INSTANCE = new ProcessOperationLockRegistry();
    
    private final Map<String, ProcessOperationLock> locks = new ConcurrentHashMap<>();
    
    /**
     * Get process operation lock registry.
     *
     * @return got instance
     */
    public static ProcessOperationLockRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Wait until release ready.
     *
     * @param processId process ID
     * @param latchCount count fot latch
     * @param releaseStrategy process operation lock release strategy
     * @return release ready or not
     */
    public boolean waitUntilReleaseReady(final String processId, final int latchCount, final ProcessOperationLockReleaseStrategy releaseStrategy) {
        ProcessOperationLock lock = new ProcessOperationLock(latchCount);
        locks.put(processId, lock);
        try {
            return lock.awaitDefaultTime(releaseStrategy);
        } finally {
            locks.remove(processId);
        }
    }
    
    /**
     * Notify lock.
     *
     * @param processId process ID
     */
    public void notify(final String processId) {
        ProcessOperationLock lock = locks.get(processId);
        if (null != lock) {
            lock.doNotify();
        }
    }
}
