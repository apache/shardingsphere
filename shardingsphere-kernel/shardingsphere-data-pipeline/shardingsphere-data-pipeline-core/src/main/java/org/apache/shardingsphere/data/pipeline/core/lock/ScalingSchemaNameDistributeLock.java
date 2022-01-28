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

package org.apache.shardingsphere.data.pipeline.core.lock;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Map;

/**
 * Distributed locks added to the schema name during scaling.
 */
public final class ScalingSchemaNameDistributeLock {
    
    private static volatile ScalingSchemaNameDistributeLock instance;
    
    private final LockRegistryService lockRegistryService;
    
    private final Map<String, Boolean> lockNameLockedMap;
    
    private ScalingSchemaNameDistributeLock() {
        ClusterPersistRepository repository = (ClusterPersistRepository) PipelineContext.getContextManager().getMetaDataContexts().getMetaDataPersistService().get().getRepository();
        lockRegistryService = new LockRegistryService(repository);
        lockNameLockedMap = Maps.newConcurrentMap();
    }
    
    /**
     * get ScalingSchemaNameDistributeLock instance.
     * @return ScalingSchemaNameDistributeLock
     */
    public static ScalingSchemaNameDistributeLock getInstance() {
        if (null == instance) {
            synchronized (ScalingSchemaNameDistributeLock.class) {
                if (null == instance) {
                    instance = new ScalingSchemaNameDistributeLock();
                }
            }
        }
        return instance;
    }
    
    /**
     * Try to get lock.
     * @param lockName lock name
     * @param timeoutMilliseconds the maximum time in milliseconds to acquire lock
     * @return true if get the lock, false if not
     */
    public boolean tryLock(final String lockName, final long timeoutMilliseconds) {
        boolean locked = lockRegistryService.tryLock(decorateLockName(lockName), timeoutMilliseconds);
        if (locked) {
            lockNameLockedMap.put(lockName, true);
        }
        return locked;
    }
    
    /**
     * Release lock.
     * @param lockName lock name
     */
    public void releaseLock(final String lockName) {
        if (lockNameLockedMap.getOrDefault(lockName, false)) {
            lockNameLockedMap.remove(lockName);
            lockRegistryService.releaseLock(decorateLockName(lockName));
        }
    }
    
    private String decorateLockName(final String schemaName) {
        return "Scaling-" + schemaName;
    }
}
