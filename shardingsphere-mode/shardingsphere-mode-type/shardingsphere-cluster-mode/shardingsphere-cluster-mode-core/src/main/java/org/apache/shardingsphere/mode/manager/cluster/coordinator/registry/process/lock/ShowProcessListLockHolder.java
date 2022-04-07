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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.lock;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Show process list lock holder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowProcessListLockHolder {
    
    private static final ShowProcessListLockHolder INSTANCE = new ShowProcessListLockHolder();
    
    @Getter
    private final Map<String, ShowProcessListSimpleLock> locks = new ConcurrentHashMap<>();
    
    /**
     * Get show process list lock holder.
     *
     * @return show process list holder
     */
    public static ShowProcessListLockHolder getInstance() {
        return INSTANCE;
    }
}
