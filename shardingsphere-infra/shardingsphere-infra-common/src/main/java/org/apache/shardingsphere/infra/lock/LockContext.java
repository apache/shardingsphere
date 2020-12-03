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

import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockContext {
    
    private static final AtomicReference<LockStrategy> LOCK_STRATEGY = new AtomicReference<>();
    
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
}
