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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.fixture;

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.LeaderExecutionCallback;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.transaction.TransactionOperation;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public final class ClusterPersistRepositoryFixture implements ClusterPersistRepository {
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config) {
    }
    
    @Override
    public int getNumChildren(final String key) {
        return 0;
    }
    
    @Override
    public void addCacheData(final String cachePath) {
    }
    
    @Override
    public void evictCacheData(final String cachePath) {
    }
    
    @Override
    public Object getRawCache(final String cachePath) {
        return null;
    }
    
    @Override
    public void executeInLeader(final String key, final LeaderExecutionCallback callback) {
    }
    
    @Override
    public void executeInTransaction(final List<TransactionOperation> transactionOperations) {
    }
    
    @Override
    public void updateInTransaction(final String key, final String value) {
    }
    
    @Override
    public String get(final String key) {
        return null;
    }
    
    @Override
    public String getDirectly(final String key) {
        return "";
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return Collections.emptyList();
    }
    
    @Override
    public boolean isExisted(final String key) {
        return false;
    }
    
    @Override
    public void persist(final String key, final String value) {
    }
    
    @Override
    public void update(final String key, final String value) {
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
    }
    
    @Override
    public void persistExclusiveEphemeral(final String key, final String value) {
    }
    
    @Override
    public void delete(final String key) {
    }
    
    @Override
    public long getRegistryCenterTime(final String key) {
        return 0;
    }
    
    @Override
    public Object getRawClient() {
        return null;
    }
    
    @Override
    public boolean tryLock(final String lockKey, final long timeoutMillis) {
        return false;
    }
    
    @Override
    public void unlock(final String lockKey) {
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener, final Executor executor) {
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
