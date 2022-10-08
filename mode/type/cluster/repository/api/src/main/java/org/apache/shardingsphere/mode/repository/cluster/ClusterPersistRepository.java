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

import org.apache.shardingsphere.elasticjob.lite.internal.storage.LeaderExecutionCallback;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.transaction.TransactionOperation;

import java.util.List;

/**
 * Cluster persist repository.
 */
public interface ClusterPersistRepository extends PersistRepository {
    
    /**
     * Initialize registry center.
     *
     * @param config cluster persist repository configuration
     */
    void init(ClusterPersistRepositoryConfiguration config);
    
    /**
     * Get children number.
     *
     * @param key key
     * @return children number
     */
    int getNumChildren(String key);
    
    /**
     * Add data to cache.
     *
     * @param cachePath cache path
     */
    void addCacheData(String cachePath);
    
    /**
     * Evict data from cache.
     *
     * @param cachePath cache path
     */
    void evictCacheData(String cachePath);
    
    /**
     * Get raw cache object of registry center.
     *
     * @param cachePath cache path
     * @return raw cache object of registry center
     */
    Object getRawCache(String cachePath);
    
    /**
     * Execute in leader.
     *
     * @param key key
     * @param callback callback of leader
     */
    void executeInLeader(String key, LeaderExecutionCallback callback);
    
    /**
     * Execute oprations in transaction.
     *
     * @param transactionOperations operations
     * @throws Exception exception
     */
    void executeInTransaction(List<TransactionOperation> transactionOperations) throws Exception;
    
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
     * @param value is persisted or not
     */
    void persistExclusiveEphemeral(String key, String value);
    
    /**
     * Try lock.
     *
     * @param lockKey lock key
     * @param timeoutMillis timeout millis
     * @return is locked or not
     */
    boolean tryLock(String lockKey, long timeoutMillis);
    
    /**
     * Unlock.
     *
     * @param lockKey lock key
     */
    void unlock(String lockKey);
    
    /**
     * Watch key or path of governance server.
     *
     * @param key key of data
     * @param listener data changed event listener
     */
    void watch(String key, DataChangedEventListener listener);
}
