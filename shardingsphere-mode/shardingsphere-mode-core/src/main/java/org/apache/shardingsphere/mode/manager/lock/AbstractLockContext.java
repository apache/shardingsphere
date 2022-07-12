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

package org.apache.shardingsphere.mode.manager.lock;

import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.infra.lock.LockNameDefinition;
import org.apache.shardingsphere.mode.manager.lock.definition.DatabaseLockDefinition;
import org.apache.shardingsphere.mode.manager.lock.definition.DatabaseLockNameDefinition;

/**
 * Abstract lock context.
 */
public abstract class AbstractLockContext implements LockContext {
    
    @Override
    public boolean tryLock(final LockDefinition lockDefinition) {
        LockNameDefinition lockNameDefinition = lockDefinition.getLockNameDefinition();
        if (lockNameDefinition instanceof DatabaseLockNameDefinition) {
            return tryLock((DatabaseLockDefinition) lockDefinition);
        }
        throw new UnsupportedOperationException();
    }
    
    protected abstract boolean tryLock(DatabaseLockDefinition lockDefinition);
    
    @Override
    public boolean tryLock(final LockDefinition lockDefinition, final long timeoutMilliseconds) {
        LockNameDefinition lockNameDefinition = lockDefinition.getLockNameDefinition();
        if (lockNameDefinition instanceof DatabaseLockNameDefinition) {
            return tryLock((DatabaseLockDefinition) lockDefinition, timeoutMilliseconds);
        }
        throw new UnsupportedOperationException();
    }
    
    protected abstract boolean tryLock(DatabaseLockDefinition lockDefinition, long timeoutMilliseconds);
    
    @Override
    public void releaseLock(final LockDefinition lockDefinition) {
        LockNameDefinition lockNameDefinition = lockDefinition.getLockNameDefinition();
        if (lockNameDefinition instanceof DatabaseLockNameDefinition) {
            releaseLock((DatabaseLockDefinition) lockDefinition);
            return;
        }
        throw new UnsupportedOperationException();
    }
    
    protected abstract void releaseLock(DatabaseLockDefinition lockDefinition);
    
    @Override
    public boolean isLocked(final LockDefinition lockDefinition) {
        LockNameDefinition lockNameDefinition = lockDefinition.getLockNameDefinition();
        if (lockNameDefinition instanceof DatabaseLockNameDefinition) {
            return isLocked((DatabaseLockDefinition) lockDefinition);
        }
        throw new UnsupportedOperationException();
    }
    
    protected abstract boolean isLocked(DatabaseLockDefinition lockDefinition);
}
