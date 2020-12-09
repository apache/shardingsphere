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

package org.apache.shardingsphere.infra.lock;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockContext {
    
    private static final AtomicReference<LockStrategy> LOCK_STRATEGY = new AtomicReference<>();
    
    private static final Lock LOCK = new ReentrantLock();
    
    private static final Condition CONDITION = LOCK.newCondition();
    
    /**
     * Init lock strategy.
     * 
     * @param lockStrategy lock strategy
     */
    public static void init(final LockStrategy lockStrategy) {
        LOCK_STRATEGY.set(lockStrategy);
    }
    
    /**
     * Get lock strategy.
     * 
     * @return lock strategy
     */
    public static LockStrategy getLockStrategy() {
        return LOCK_STRATEGY.get();
    }
    
    /**
     * Waiting for unlock.
     * 
     * @param timeout the maximum time in milliseconds to wait
     * @return false if wait timeout exceeded, else true
     */
    public static boolean await(final Long timeout) {
        LOCK.lock();
        try {
            return CONDITION.await(timeout, TimeUnit.MILLISECONDS);
            // CHECKSTYLE:OFF
        } catch (InterruptedException e) {
            // CHECKSTYLE:ON
        } finally {
            LOCK.unlock();
        }
        return false;
    }
    
    /**
     * Notify all blocked tasks.
     */
    public static void signalAll() {
        LOCK.lock();
        try {
            CONDITION.signalAll();
        } finally {
            LOCK.unlock();
        }
    }
}
