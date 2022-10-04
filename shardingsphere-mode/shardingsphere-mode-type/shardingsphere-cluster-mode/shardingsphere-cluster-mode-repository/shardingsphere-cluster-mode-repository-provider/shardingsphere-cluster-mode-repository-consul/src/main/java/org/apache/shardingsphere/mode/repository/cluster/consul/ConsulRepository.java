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

package org.apache.shardingsphere.mode.repository.cluster.consul;

import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.consul.lock.ConsulInternalLockProvider;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registry repository of Consul.
 */
public class ConsulRepository implements ClusterPersistRepository {
    
    private ShardingSphereConsulClient consulClient;
    
    private ConsulInternalLockProvider consulInternalLockProvider;
    
    private ConsulProperties consulProperties;
    
    private Map<String, Set<String>> watchKeyMap;
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config) {
        this.consulClient = new ShardingSphereConsulClient(new ConsulRawClient(config.getServerLists()));
        this.consulProperties = new ConsulProperties(config.getProps());
        this.consulInternalLockProvider = new ConsulInternalLockProvider(this.consulClient, this.consulProperties);
        this.watchKeyMap = new HashMap<String, Set<String>>(6);
    }
    
    @Override
    public String get(final String key) {
        Response<GetValue> response = this.consulClient.getKVValue(key);
        return response != null ? response.getValue().getValue() : null;
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        Response<List<String>> response = this.consulClient.getKVKeysOnly(key);
        return response != null ? response.getValue() : Collections.EMPTY_LIST;
    }
    
    @Override
    public void persist(final String key, final String value) {
        this.consulClient.setKVValue(key, value);
    }
    
    @Override
    public void delete(final String key) {
        this.consulClient.deleteKVValue(key);
    }
    
    @Override
    public void close() {
        // this.consulClien
        // this.consulClient.
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        NewSession session = new NewSession();
        session.setName(key);
        session.setBehavior(Session.Behavior.DELETE);
        session.setTtl(this.consulProperties.getValue(ConsulPropertyKey.TIME_TO_LIVE_IN_SECONDS) + "s");
        Response<String> response = this.consulClient.sessionCreate(session, QueryParams.DEFAULT);
        final String sessionId = response.getValue();
        PutParams putParams = new PutParams();
        putParams.setAcquireSession(sessionId);
        this.consulClient.setKVValue(key, value, putParams);
        this.consulInternalLockProvider.generatorFlushSessionTtlTask(this.consulClient, sessionId);
    }
    
    @Override
    public void persistExclusiveEphemeral(final String key, final String value) {
        this.persistEphemeral(key, value);
    }
    
    @Override
    public boolean tryLock(final String lockKey, final long timeoutMillis) {
        InternalLock lock = this.consulInternalLockProvider.getInternalMutexLock(lockKey);
        return lock.tryLock(timeoutMillis);
    }
    
    @Override
    public void unlock(final String lockKey) {
        InternalLock lock = this.consulInternalLockProvider.getInternalMutexLock(lockKey);
        lock.unlock();
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        Thread watchThread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                watchChildKeyChangeEvent(key, listener);
            }
        });
        watchThread.setDaemon(true);
        watchThread.start();
    }
    
    private void watchChildKeyChangeEvent(final String key, final DataChangedEventListener listener) {
        AtomicBoolean running = new AtomicBoolean(true);
        long currentIndex = 0;
        while (running.get()) {
            Response<List<GetValue>> response = consulClient.getKVValues(key,
                    new QueryParams(consulProperties.getValue(ConsulPropertyKey.BLOCK_QUERY_TIME_IN_SECONDS), currentIndex));
            Long index = response.getConsulIndex();
            if (index != null && currentIndex == 0) {
                currentIndex = index;
                Set<String> watchKeySet = watchKeyMap.get(key);
                if (watchKeySet == null) {
                    watchKeySet = new HashSet<>();
                }
                for (GetValue getValue : response.getValue()) {
                    if (!watchKeySet.contains(getValue.getKey())) {
                        watchKeySet.add(getValue.getKey());
                    }
                }
                continue;
            }
            if (index != null && index > currentIndex) {
                currentIndex = index;
                Set<String> newKeySet = new HashSet<>(response.getValue().size());
                Set<String> watchKeySet = watchKeyMap.get(key);
                for (GetValue getValue : response.getValue()) {
                    newKeySet.add(getValue.getKey());
                    if (!watchKeySet.contains(getValue.getKey())) {
                        watchKeySet.add(getValue.getKey());
                        fireDataChangeEvent(getValue, listener, DataChangedEvent.Type.ADDED);
                    } else if (watchKeySet.contains(getValue.getKey()) && getValue.getModifyIndex() >= currentIndex) {
                        fireDataChangeEvent(getValue, listener, DataChangedEvent.Type.UPDATED);
                    }
                }
                for (String existKey : watchKeySet) {
                    if (!newKeySet.contains(existKey)) {
                        GetValue getValue = new GetValue();
                        getValue.setKey(existKey);
                        fireDataChangeEvent(getValue, listener, DataChangedEvent.Type.DELETED);
                    }
                }
                this.watchKeyMap.put(key, newKeySet);
            } else if (index != null && index < currentIndex) {
                currentIndex = 0;
            }
        }
    }
    
    private void fireDataChangeEvent(final GetValue getValue, final DataChangedEventListener listener, final DataChangedEvent.Type type) {
        DataChangedEvent event = new DataChangedEvent(getValue.getKey(), getValue.getValue(), type);
        listener.onChange(event);
    }
    
    @Override
    public String getType() {
        return "Consul";
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return ClusterPersistRepository.super.getTypeAliases();
    }
    
}
