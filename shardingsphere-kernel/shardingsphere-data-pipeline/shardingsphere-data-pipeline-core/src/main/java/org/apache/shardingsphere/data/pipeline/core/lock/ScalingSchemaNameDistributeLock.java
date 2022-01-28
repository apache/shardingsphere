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
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.util.Map;

/**
 * distributed locks added to the schema name during scaling.
 */
public final class ScalingSchemaNameDistributeLock {
    
    private static final LockRegistryService LOCK_REGISTRY_SERVICE;
    
    private static final Map<String, Boolean> LOCK_NAME_LOCKED_MAP;
    
    static {
        ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) PipelineContext.getModeConfig().getRepository();
        ClusterPersistRepository repository = (ClusterPersistRepository) PipelineContext.getContextManager().getMetaDataContexts().getMetaDataPersistService().get().getRepository();
        repository.init(repositoryConfig);
        LOCK_REGISTRY_SERVICE = new LockRegistryService(repository);
        LOCK_NAME_LOCKED_MAP = Maps.newConcurrentMap();
    }
    
    /**
     * Try to get lock.
     * @param lockName lock name
     * @param timeoutMilliseconds the maximum time in milliseconds to acquire lock
     * @return true if get the lock, false if not
     */
    public static boolean tryLock(final String lockName, final long timeoutMilliseconds) {
        boolean locked = LOCK_REGISTRY_SERVICE.tryLock(decorateLockName(lockName), timeoutMilliseconds);
        if (locked) {
            LOCK_NAME_LOCKED_MAP.put(lockName, true);
        }
        return locked;
    }
    
    /**
     * Release lock.
     * @param lockName lock name
     */
    public static void releaseLock(final String lockName) {
        if (LOCK_NAME_LOCKED_MAP.getOrDefault(lockName, false)) {
            LOCK_NAME_LOCKED_MAP.remove(lockName);
            LOCK_REGISTRY_SERVICE.releaseLock(decorateLockName(lockName));
        }
    }
    
    private static String decorateLockName(final String schemaName) {
        return "Scaling-" + schemaName;
    }
}
