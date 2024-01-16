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

import com.ecwid.consul.transport.HttpResponse;
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
import org.apache.http.HttpStatus;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.lock.holder.DistributedLockHolder;

import java.net.MalformedURLException;
import java.net.URL;
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
        consulProps = new ConsulProperties(config.getProps());
        ConsulRawClient rawClient = createConsulRawClient(config.getServerLists());
        consulClient = new ShardingSphereConsulClient(rawClient);
        distributedLockHolder = new DistributedLockHolder(getType(), consulClient, consulProps);
        watchKeyMap = new HashMap<>(6, 1F);
    }
    
    @Override
    public String getDirectly(final String key) {
        Response<GetValue> response = consulClient.getKVValue(key);
        if (null == response) {
            return null;
        }
        GetValue value = response.getValue();
        return null == value ? null : value.getValue();
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        Response<List<String>> response = consulClient.getKVKeysOnly(key);
        if (null == response) {
            return Collections.emptyList();
        }
        List<String> value = response.getValue();
        return null == value ? Collections.emptyList() : value;
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
    
    /**
     * {@link ConsulRawClient} is a wrapper of blocking HTTP client and does not have a close method.
     * Using such a Client does not necessarily conform to the implementation of the relevant SPI. ShardingSphere needs to
     * consider solutions similar to <a href="https://github.com/spring-cloud/spring-cloud-consul/issues/475">spring-cloud/spring-cloud-consul#475</a>.
     *
     * @see ConsulRawClient
     */
    @Override
    public void close() {
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        Response<String> response = consulClient.sessionCreate(createNewSession(key), QueryParams.DEFAULT);
        String sessionId = response.getValue();
        PutParams putParams = new PutParams();
        putParams.setAcquireSession(sessionId);
        consulClient.setKVValue(key, value, putParams);
        generatorFlushSessionTtlTask(consulClient, sessionId);
        verifyConsulAgentRunning();
    }
    
    @SuppressWarnings("HttpUrlsUsage")
    private ConsulRawClient createConsulRawClient(final String serverLists) {
        if (Strings.isNullOrEmpty(serverLists)) {
            return new ConsulRawClient();
        }
        URL serverUrl;
        try {
            serverUrl = new URL(!serverLists.startsWith("https://") && !serverLists.startsWith("http://") ? "http://" + serverLists : serverLists);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (-1 == serverUrl.getPort()) {
            return new ConsulRawClient(serverUrl.getHost());
        }
        return new ConsulRawClient(serverUrl.getHost(), serverUrl.getPort());
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
            List<GetValue> value = response.getValue();
            if (null == value) {
                continue;
            }
            Long index = response.getConsulIndex();
            if (null != index && 0 == currentIndex) {
                currentIndex = index;
                if (!watchKeyMap.containsKey(key)) {
                    watchKeyMap.put(key, new HashSet<>());
                }
                Collection<String> watchKeys = watchKeyMap.get(key);
                for (GetValue each : value) {
                    watchKeys.add(each.getKey());
                }
                continue;
            }
            if (null != index && index > currentIndex) {
                currentIndex = index;
                Collection<String> newKeys = new HashSet<>(value.size(), 1F);
                Collection<String> watchKeys = watchKeyMap.get(key);
                for (GetValue each : value) {
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
     * @param sessionId    session id
     */
    public void generatorFlushSessionTtlTask(final ConsulClient consulClient, final String sessionId) {
        SESSION_FLUSH_EXECUTOR.scheduleAtFixedRate(() -> consulClient.renewSession(sessionId, QueryParams.DEFAULT), 1L, 10L, TimeUnit.SECONDS);
    }
    
    /**
     * See <a href="https://developer.hashicorp.com/consul/api-docs/v1.17.x/status">Status HTTP API</a> .
     *
     * @throws RuntimeException Unable to connect to Consul Agent.
     */
    private void verifyConsulAgentRunning() {
        HttpResponse httpResponse = consulClient.getRawClient().makeGetRequest("/v1/status/leader");
        if (HttpStatus.SC_OK != httpResponse.getStatusCode()) {
            throw new RuntimeException("Unable to connect to Consul Agent and StatusCode is " + httpResponse.getStatusCode() + ".");
        }
    }
    
    @Override
    public String getType() {
        return "Consul";
    }
}
