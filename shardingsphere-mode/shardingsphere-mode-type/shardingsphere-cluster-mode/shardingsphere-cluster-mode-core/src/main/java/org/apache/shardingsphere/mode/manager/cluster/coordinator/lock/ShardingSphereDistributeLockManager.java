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

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.lock.LockType;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;

/**
 * Lock manager of ShardingSphere.
 */
public interface ShardingSphereDistributeLockManager {
    
    /**
     * Init locks state.
     *
     * @param repository persist repository
     * @param instance instance
     * @param computeNodeInstances compute node instances
     */
    void initLocksState(PersistRepository repository, ComputeNodeInstance instance, Collection<ComputeNodeInstance> computeNodeInstances);
    
    /**
     * Get or create lock.
     *
     * @param lockName lock name
     * @return lock
     */
    ShardingSphereLock getOrCreateLock(String lockName);
    
    /**
     * Is locked.
     *
     * @param lockName lock name
     * @return is locked or not
     */
    boolean isLocked(String lockName);
    
    /**
     * Get lock type.
     *
     * @return lock type
     */
    LockType getLockType();
}
