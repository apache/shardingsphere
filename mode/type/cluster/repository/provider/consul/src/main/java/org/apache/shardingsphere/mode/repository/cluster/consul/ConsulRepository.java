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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.model.NewSession;
import com.ecwid.consul.v1.session.model.Session;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulPropertyKey;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.lock.holder.DistributedLockHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registry repository of Consul.
 */
public final class ConsulRepository implements ClusterPersistRepository {
    
    private static final ScheduledThreadPoolExecutor SESSION_FLUSH_EXECUTOR = new ScheduledThreadPoolExecutor(2);
    
    private ShardingSphereConsulClient consulClient;
    
    private ConsulProperties consulProps;
    
    @Getter
    private DistributedLockHolder distributedLockHolder;
    
    private Map<String, Collection<String>> watchKeyMap;
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config) {
        ConsulRawClient rawClient = Strings.isNullOrEmpty(config.getServerLists()) ? new ConsulRawClient() : new ConsulRawClient(config.getServerLists());
        consulClient = new ShardingSphereConsulClient(rawClient);
        consulProps = new ConsulProperties(config.getProps());
        distributedLockHolder = new DistributedLockHolder(getType(), consulClient, consulProps);
        watchKeyMap = new HashMap<>(6, 1F);
    }
    
    @Override
    public String getDirectly(final String key) {
        Response<GetValue> response = consulClient.getKVValue(key);
        return null == response ? null : response.getValue().getValue();
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        Response<List<String>> response = consulClient.getKVKeysOnly(key);
        return null == response ? Collections.emptyList() : response.getValue();
    }
    
    @Override
    public boolean isExisted(final String key) {
        return null != consulClient.getKVValue(key).getValue();
    }
    
    @Override
    public void persist(final String key, final String value) {
        consulClient.setKVValue(key, value);
    }
    
    @Override
    public void update(final String key, final String value) {
        consulClient.setKVValue(key, value);
    }
    
    @Override
    public void delete(final String key) {
        consulClient.deleteKVValue(key);
    }
    
    @Override
    public void close() {
        // TODO
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        Response<String> response = consulClient.sessionCreate(createNewSession(key), QueryParams.DEFAULT);
        String sessionId = response.getValue();
        PutParams putParams = new PutParams();
        putParams.setAcquireSession(sessionId);
        consulClient.setKVValue(key, value, putParams);
        generatorFlushSessionTtlTask(consulClient, sessionId);
    }
    
    private NewSession createNewSession(final String key) {
        NewSession result = new NewSession();
        result.setName(key);
        result.setBehavior(Session.Behavior.DELETE);
        result.setTtl(consulProps.getValue(ConsulPropertyKey.TIME_TO_LIVE_SECONDS));
        return result;
    }
    
    @Override
    public void persistExclusiveEphemeral(final String key, final String value) {
        persistEphemeral(key, value);
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        Thread watchThread = new Thread(() -> watchChildKeyChangeEvent(key, listener));
        watchThread.setDaemon(true);
        watchThread.start();
    }
    
    private void watchChildKeyChangeEvent(final String key, final DataChangedEventListener listener) {
        AtomicBoolean running = new AtomicBoolean(true);
        long currentIndex = 0;
        while (running.get()) {
            Response<List<GetValue>> response = consulClient.getKVValues(key, new QueryParams(consulProps.getValue(ConsulPropertyKey.BLOCK_QUERY_TIME_TO_SECONDS), currentIndex));
            Long index = response.getConsulIndex();
            if (null != index && 0 == currentIndex) {
                currentIndex = index;
                if (!watchKeyMap.containsKey(key)) {
                    watchKeyMap.put(key, new HashSet<>());
                }
                Collection<String> watchKeys = watchKeyMap.get(key);
                for (GetValue each : response.getValue()) {
                    watchKeys.add(each.getKey());
                }
                continue;
            }
            if (null != index && index > currentIndex) {
                currentIndex = index;
                Collection<String> newKeys = new HashSet<>(response.getValue().size(), 1F);
                Collection<String> watchKeys = watchKeyMap.get(key);
                for (GetValue each : response.getValue()) {
                    newKeys.add(each.getKey());
                    if (!watchKeys.contains(each.getKey())) {
                        watchKeys.add(each.getKey());
                        fireDataChangeEvent(each, listener, DataChangedEvent.Type.ADDED);
                    } else if (watchKeys.contains(each.getKey()) && each.getModifyIndex() >= currentIndex) {
                        fireDataChangeEvent(each, listener, DataChangedEvent.Type.UPDATED);
                    }
                }
                for (String each : watchKeys) {
                    if (!newKeys.contains(each)) {
                        GetValue getValue = new GetValue();
                        getValue.setKey(each);
                        fireDataChangeEvent(getValue, listener, DataChangedEvent.Type.DELETED);
                    }
                }
                watchKeyMap.put(key, newKeys);
            } else if (null != index && index < currentIndex) {
                currentIndex = 0;
            }
        }
    }
    
    private void fireDataChangeEvent(final GetValue getValue, final DataChangedEventListener listener, final DataChangedEvent.Type type) {
        listener.onChange(new DataChangedEvent(getValue.getKey(), getValue.getValue(), type));
    }
    
    /**
     * Flush session by update TTL.
     *
     * @param consulClient consul client
     * @param sessionId session id
     */
    public void generatorFlushSessionTtlTask(final ConsulClient consulClient, final String sessionId) {
        SESSION_FLUSH_EXECUTOR.scheduleAtFixedRate(() -> consulClient.renewSession(sessionId, QueryParams.DEFAULT), 1L, 10L, TimeUnit.SECONDS);
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
