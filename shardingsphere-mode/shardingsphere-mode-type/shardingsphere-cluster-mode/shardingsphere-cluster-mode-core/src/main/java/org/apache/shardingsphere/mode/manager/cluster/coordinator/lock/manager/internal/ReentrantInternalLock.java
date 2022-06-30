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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.internal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Inter mutex reentrant lock.
 */
@RequiredArgsConstructor
public final class ReentrantInternalLock implements InternalLock {
    
    private final Lock internalLock;
    
    @Override
    public boolean tryLock() {
        return tryLock(TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLock(final long timeoutMillis) {
        try {
            return internalLock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignore) {
            return false;
        }
    }
    
    @Override
    public void unlock() {
        internalLock.unlock();
    }
    
    @Override
    public boolean isLocked() {
        throw new UnsupportedOperationException();
    }
}
