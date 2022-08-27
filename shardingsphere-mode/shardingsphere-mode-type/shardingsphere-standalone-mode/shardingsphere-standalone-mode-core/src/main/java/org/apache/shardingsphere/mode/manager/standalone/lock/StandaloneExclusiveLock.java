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

package org.apache.shardingsphere.mode.manager.standalone.lock;

import org.apache.shardingsphere.infra.lock.LockState;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Standalone exclusive lock.
 */
public final class StandaloneExclusiveLock extends ReentrantLock {
    
    private final AtomicReference<LockState> lockState = new AtomicReference<>(LockState.UNLOCKED);
    
    @Override
    public boolean tryLock(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (!lockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKING)) {
            return false;
        }
        boolean isLocked = super.tryLock(timeout, unit);
        if (isLocked && lockState.compareAndSet(LockState.LOCKING, LockState.LOCKED)) {
            return true;
        }
        lockState.compareAndSet(LockState.LOCKING, LockState.UNLOCKED);
        return false;
    }
    
    @Override
    public void unlock() {
        if (LockState.LOCKED == lockState.get()) {
            super.unlock();
            lockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
        }
    }
    
    @Override
    public boolean isLocked() {
        return LockState.LOCKED == lockState.get();
    }
}
