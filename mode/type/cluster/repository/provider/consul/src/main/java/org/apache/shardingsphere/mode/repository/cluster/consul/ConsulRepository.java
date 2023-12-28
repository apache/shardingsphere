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

import com.google.common.base.Strings;
import com.orbitz.consul.Consul;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import lombok.Getter;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulProperties;
import org.apache.shardingsphere.mode.repository.cluster.consul.props.ConsulPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.lock.holder.DistributedLockHolder;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry repository of Consul.
 * Before JDK 18 implemented in JEP 400, the return value of `{@link java.nio.charset.Charset}.defaultCharset()` on the
 * Windows platform was usually not `{@link java.nio.charset.StandardCharsets}.UTF_8`.
 * This explains the series of settings this class has on CharSet.
 */
public final class ConsulRepository implements ClusterPersistRepository {
    
    private final Map<String, KVCache> caches = new ConcurrentHashMap<>();
    
    private Consul consulClient;
    
    @Getter
    private DistributedLockHolder distributedLockHolder;
    
    private final Set<String> ephemeralKeySet = new HashSet<>();
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config) {
        ConsulProperties consulProps = new ConsulProperties(config.getProps());
        consulClient = createConsulClient(config.getServerLists(), consulProps.getValue(ConsulPropertyKey.BLOCK_QUERY_TIME_TO_SECONDS));
        distributedLockHolder = new DistributedLockHolder(getType(), consulClient, consulProps);
    }
    
    /**
     * Set ReadTimeoutMillis to avoid `java.lang.IllegalArgumentException: Cache watchInterval=10sec >= networkClientReadTimeout=10000ms. It can cause issues`.
     *
     * @param serverLists             serverUrl.
     * @param blockQueryTimeToSeconds blockQueryTimeToSeconds for Mode Config.
     * @return Consul client.
     * @throws RuntimeException MalformedURLException.
     */
    @SuppressWarnings("HttpUrlsUsage")
    private Consul createConsulClient(final String serverLists, final long blockQueryTimeToSeconds) {
        Consul.Builder builder = Consul.builder().withReadTimeoutMillis(blockQueryTimeToSeconds * 1000);
        if (Strings.isNullOrEmpty(serverLists)) {
            return builder.build();
        }
        URL serverUrl;
        try {
            serverUrl = new URL(!serverLists.startsWith("https://") && !serverLists.startsWith("http://") ? "http://" + serverLists : serverLists);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return builder.withUrl(serverUrl).build();
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return consulClient.keyValueClient()
                .getKeys(key)
                .stream()
                .filter(s -> !s.equals(key))
                .collect(Collectors.toList());
    }
    
    @Override
    public void persist(final String key, final String value) {
        consulClient.keyValueClient().putValue(key, value, StandardCharsets.UTF_8);
    }
    
    @Override
    public void update(final String key, final String value) {
        consulClient.keyValueClient().putValue(key, value, StandardCharsets.UTF_8);
    }
    
    @Override
    public String getDirectly(final String key) {
        return consulClient.keyValueClient().getValueAsString(key, StandardCharsets.UTF_8).orElse(null);
    }
    
    @Override
    public boolean isExisted(final String key) {
        return !consulClient.keyValueClient().getValuesAsString(key, StandardCharsets.UTF_8).isEmpty();
    }
    
    /**
     * Persist Ephemeral by flushing session by update TTL.
     *
     * @param key   key of data
     * @param value value of data
     */
    @Override
    public void persistEphemeral(final String key, final String value) {
        if (isExisted(key)) {
            consulClient.keyValueClient().deleteKeys(key);
            ephemeralKeySet.remove(key);
        }
        consulClient.keyValueClient().putValue(key, value, StandardCharsets.UTF_8);
        ephemeralKeySet.add(key);
    }
    
    @Override
    public void persistExclusiveEphemeral(final String key, final String value) {
        consulClient.keyValueClient().putValue(key, value, StandardCharsets.UTF_8);
        ephemeralKeySet.add(key);
    }
    
    @Override
    public void delete(final String key) {
        if (isExisted(key)) {
            consulClient.keyValueClient().deleteKeys(key);
        }
    }
    
    /**
     * Consul doesn't tell clients what key changed when performing a watch. we best bet is to do a comparison with the previous set of values.
     * This is a bit troublesome in ShardingSphere context implementation.
     *
     * @param key      key of data
     * @param listener data changed event listener
     */
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        KVCache cache = caches.get(key);
        if (null == cache) {
            cache = KVCache.newCache(consulClient.keyValueClient(), key);
            caches.put(key, cache);
        }
        cache.addListener(newValues -> {
            Optional<Value> newValue = newValues.values().stream().filter(value -> value.getKey().equals(key)).findAny();
            newValue.ifPresent(value -> {
                Optional<String> decodedValue = newValue.get().getValueAsString();
                decodedValue.ifPresent(v -> listener.onChange(new DataChangedEvent(key, v, DataChangedEvent.Type.ADDED)));
            });
        });
        cache.start();
    }
    
    @Override
    public void close() {
        caches.values().forEach(KVCache::close);
        ephemeralKeySet.forEach(s -> consulClient.keyValueClient().deleteKey(s));
        ephemeralKeySet.clear();
        consulClient.destroy();
    }
    
    @Override
    public String getType() {
        return "Consul";
    }
}
