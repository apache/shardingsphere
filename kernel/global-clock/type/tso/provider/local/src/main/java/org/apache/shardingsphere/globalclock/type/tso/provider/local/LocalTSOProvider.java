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

package org.apache.shardingsphere.globalclock.type.tso.provider.local;

import org.apache.shardingsphere.globalclock.type.tso.provider.TSOProvider;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Local TSO provider.
 */
public final class LocalTSOProvider implements TSOProvider {
    
    private final AtomicLong localClock = new AtomicLong();
    
    private final ReentrantLock lock = new ReentrantLock();
    
    @Override
    public long getCurrentTimestamp() {
        return localClock.get();
    }
    
    @Override
    public long getNextTimestamp() {
        return localClock.incrementAndGet();
    }
    
    @Override
    public boolean tryLock(final long timeoutMillis) {
        try {
            return lock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void unlock() {
        lock.unlock();
    }
    
    @Override
    public String getType() {
        return "TSO.local";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
