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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockType;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.EnumMap;
import java.util.Map;

/**
 * Lock node service factory.
 */
public final class LockNodeServiceFactory {
    
    static {
        ShardingSphereServiceLoader.register(LockNodeService.class);
    }
    
    private static final Map<LockType, LockNodeService> SERVICES = new EnumMap<>(LockType.class);
    
    private static final LockNodeServiceFactory INSTANCE = new LockNodeServiceFactory();
    
    private LockNodeServiceFactory() {
        loadLockNodeService();
    }
    
    private void loadLockNodeService() {
        for (LockNodeService each : ShardingSphereServiceLoader.getServiceInstances(LockNodeService.class)) {
            if (SERVICES.containsKey(each.getLockType())) {
                continue;
            }
            SERVICES.put(each.getLockType(), each);
        }
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static LockNodeServiceFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get lock node service.
     *
     * @param lockType lock type
     * @return lock node service
     */
    public LockNodeService getLockNodeService(final LockType lockType) {
        return SERVICES.get(lockType);
    }
}
