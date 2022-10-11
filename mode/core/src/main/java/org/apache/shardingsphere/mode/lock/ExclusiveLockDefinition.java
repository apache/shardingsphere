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

package org.apache.shardingsphere.mode.lock;

import lombok.Getter;
import org.apache.shardingsphere.infra.lock.LockDefinition;

/**
 * Exclusive lock definition.
 */
@Getter
public final class ExclusiveLockDefinition implements LockDefinition {
    
    private static final String LOCK_KEY_PREFIX = "/lock/exclusive/locks/";
    
    private final String lockKey;
    
    private final String lockName;
    
    public ExclusiveLockDefinition(final String lockName) {
        this.lockName = lockName;
        lockKey = LOCK_KEY_PREFIX + lockName;
    }
}
