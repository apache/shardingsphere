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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Resource synchronizer.
 */
public final class ResourceSynchronizer {
    
    private static final long DEFAULT_TIMEOUT_MILLISECONDS = 200;
    
    private final Lock lock = new ReentrantLock();
    
    private final Condition condition = lock.newCondition();
    
    /**
     * Do await until default timeout milliseconds.
     *
     * @throws InterruptedException interrupted exception
     */
    public void doAwaitUntil() throws InterruptedException {
        lock.lock();
        try {
            condition.await(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Do notify.
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
