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

import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateEvent;
import org.apache.shardingsphere.infra.state.StateType;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Standard lock strategy.
 */
public final class StandardLockStrategy implements LockStrategy {
    
    private final ReentrantLock lock = new ReentrantLock();
    
    @Override
    public boolean tryGlobalLock(final long timeout, final TimeUnit timeUnit) {
        boolean result = false;
        try {
            result = lock.tryLock(timeout, timeUnit);
            // CHECKSTYLE:OFF
        } catch (final InterruptedException e) {
            // CHECKSTYLE:ON
        }
        if (result) {
            StateContext.switchState(new StateEvent(StateType.LOCK, true));
        }
        return result;
    }
    
    @Override
    public void releaseGlobalLock() {
        lock.unlock();
        StateContext.switchState(new StateEvent(StateType.LOCK, false));
    }
    
    @Override
    public String getType() {
        return LockStrategyType.STANDARD.name();
    }
}
