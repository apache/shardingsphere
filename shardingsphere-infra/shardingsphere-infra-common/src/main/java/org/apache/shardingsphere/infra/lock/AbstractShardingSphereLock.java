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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract ShardingSphere lock.
 */
public abstract class AbstractShardingSphereLock implements ShardingSphereLock {
    
    private final Lock innerLock = new ReentrantLock();
    
    private final Condition innerCondition = innerLock.newCondition();
    
    @Override
    public final boolean await(final Long timeoutMilliseconds) {
        innerLock.lock();
        try {
            return innerCondition.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
        } finally {
            innerLock.unlock();
        }
        return false;
    }
    
    @Override
    public final void signalAll() {
        innerLock.lock();
        try {
            innerCondition.signalAll();
        } finally {
            innerLock.unlock();
        }
    }
}
