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

package org.apache.shardingsphere.mode.repository.cluster;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Optional;

/**
 * Cluster persist repository.
 */
public interface ClusterPersistRepository extends PersistRepository {
    
    /**
     * Initialize registry center.
     *
     * @param config cluster persist repository configuration
     * @param computeNodeInstanceContext compute node instance context
     */
    void init(ClusterPersistRepositoryConfiguration config, ComputeNodeInstanceContext computeNodeInstanceContext);
    
    /**
     * Persist ephemeral data.
     *
     * @param key key of data
     * @param value value of data
     */
    void persistEphemeral(String key, String value);
    
    /**
     * Persist exclusive ephemeral data.
     *
     * @param key key of data
     * @param value value of data
     * @return persist success or not
     */
    boolean persistExclusiveEphemeral(String key, String value);
    
    /**
     * Get distributed lock.
     *
     * @param lockKey kock key
     * @return distributed lock
     */
    Optional<DistributedLock> getDistributedLock(String lockKey);
    
    /**
     * Watch key or path of governance server.
     *
     * @param key key of data
     * @param listener data changed event listener
     */
    void watch(String key, DataChangedEventListener listener);
    
    /**
     * Remove listener by key.
     *
     * @param key key to be removed
     */
    void removeDataListener(String key);
}
