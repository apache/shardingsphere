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

package org.apache.shardingsphere.mode.repository.cluster.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.google.common.util.concurrent.SettableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryException;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.nacos.entity.KeyValue;
import org.apache.shardingsphere.mode.repository.cluster.nacos.entity.RegisterMetadata;
import org.apache.shardingsphere.mode.repository.cluster.nacos.listener.NamingEventListener;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosProperties;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.nacos.utils.MetadataUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Registry repository of Nacos.
 */
@Slf4j
public final class NacosRepository implements ClusterPersistRepository {
    
    private NamingService client;
    
    private NacosProperties nacosProps;
    
    private AtomicBoolean isDuplicated;
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config) {
        nacosProps = new NacosProperties(config.getProps());
        initClient(config);
        initRegisterMetadata();
    }
    
    private void initClient(final ClusterPersistRepositoryConfiguration config) {
        Properties props = new Properties();
        props.setProperty("serverAddr", config.getServerLists());
        props.setProperty("namespace", config.getNamespace());
        try {
            client = NamingFactory.createNamingService(props);
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    private void initRegisterMetadata() {
        try {
            String ip = nacosProps.getValue(NacosPropertyKey.CLUSTER_IP);
            Instance instance = new Instance();
            Map<String, String> metadataMap = new HashMap<>(4, 1);
            instance.setIp(ip);
            fillEphemeralMetadata(metadataMap);
            instance.setMetadata(metadataMap);
            metadataMap.put(MetadataUtil.UUID_NAME, MetadataUtil.UNIQUE_ID);
            String clusterIp = NacosPropertyKey.CLUSTER_IP.name();
            client.registerInstance(clusterIp, instance);
            SettableFuture<AtomicBoolean> settableFuture = SettableFuture.create();
            client.subscribe(clusterIp, event -> {
                NamingEvent namingEvent = (NamingEvent) event;
                namingEvent.getInstances().stream().filter(filterInstance -> StringUtils.equals(filterInstance.getIp(), ip)).findFirst()
                        .ifPresent(presentInstance -> {
                            boolean duplicated = !StringUtils.equals(MetadataUtil.getUUID(presentInstance), MetadataUtil.UNIQUE_ID);
                            if (Objects.isNull(isDuplicated)) {
                                settableFuture.set(new AtomicBoolean(duplicated));
                            } else {
                                isDuplicated.set(duplicated);
                            }
                        });
            });
            long initTimeoutMilliseconds = nacosProps.getValue(NacosPropertyKey.INIT_TIMEOUT_MILLISECONDS);
            isDuplicated = settableFuture.get(initTimeoutMilliseconds, TimeUnit.MILLISECONDS);
            for (RegisterMetadata registerMetadata : RegisterMetadata.values()) {
                AtomicInteger port = client.getAllInstances(registerMetadata.name(), false).stream()
                        .filter(filterInstance -> StringUtils.equals(filterInstance.getIp(), ip)).max(Comparator.comparing(Instance::getPort))
                        .map(convert -> new AtomicInteger(convert.getPort())).orElse(new AtomicInteger(Integer.MIN_VALUE));
                registerMetadata.setPort(port);
            }
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (Objects.isNull(value)) {
                throw new IllegalArgumentException("Value cannot be null");
            }
            if (!findExisted(key, true).isEmpty()) {
                delete(key);
            }
            put(key, value, true);
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    @Override
    public void persistExclusiveEphemeral(final String key, final String value) {
        try {
            if (!findExisted(key, true).isEmpty()) {
                throw new IllegalStateException("Key: " + key + " already exists");
            }
            put(key, value, true);
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    @Override
    public boolean persistLock(final String lockKey, final long timeoutMillis) {
        // TODO
        return false;
    }
    
    @Override
    public void deleteLock(final String lockKey) {
        // TODO
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        try {
            for (RegisterMetadata registerMetadata : RegisterMetadata.values()) {
                NamingEventListener eventListener = registerMetadata.getListener();
                if (Objects.isNull(eventListener)) {
                    Map<String, DataChangedEventListener> parentPathListenerMap = new HashMap<>();
                    parentPathListenerMap.put(key, listener);
                    eventListener = new NamingEventListener(parentPathListenerMap);
                    registerMetadata.setListener(eventListener);
                    client.subscribe(registerMetadata.name(), eventListener);
                } else {
                    eventListener.put(key, listener);
                }
            }
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    @Override
    public String get(final String key) {
        try {
            for (RegisterMetadata registerMetadata : RegisterMetadata.values()) {
                Optional<Instance> optional = findExisted(key, registerMetadata.isEphemeral()).stream().max(Comparator.comparing(MetadataUtil::getTimestamp));
                if (optional.isPresent()) {
                    return MetadataUtil.getValue(optional.get());
                }
            }
            return null;
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        try {
            Stream<String> concatKeys = Stream.empty();
            for (RegisterMetadata registerMetadata : RegisterMetadata.values()) {
                Stream<String> keys = findExisted(registerMetadata.isEphemeral()).stream()
                        .map(instance -> {
                            String fullPath = MetadataUtil.getKey(instance);
                            if (fullPath.startsWith(key + PATH_SEPARATOR)) {
                                String pathWithoutPrefix = fullPath.substring((key + PATH_SEPARATOR).length());
                                return pathWithoutPrefix.contains(PATH_SEPARATOR) ? pathWithoutPrefix.substring(0, pathWithoutPrefix.indexOf(PATH_SEPARATOR)) : pathWithoutPrefix;
                            }
                            return null;
                        }).filter(Objects::nonNull);
                concatKeys = Stream.concat(concatKeys, keys);
            }
            return concatKeys.distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    @Override
    public void persist(final String key, final String value) {
        try {
            if (Objects.isNull(value)) {
                throw new IllegalArgumentException("Value cannot be null");
            }
            Optional<Instance> optional = findExisted(key, false).stream().max(Comparator.comparing(MetadataUtil::getTimestamp));
            if (optional.isPresent()) {
                update(optional.get(), value);
            } else {
                put(key, value, false);
            }
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    private void put(final String key, final String value, final boolean ephemeral) throws NacosException, InterruptedException {
        if (isDuplicated.get()) {
            throw new IllegalStateException("Ip specified is duplicated in cluster");
        }
        final List<KeyValue> keyValues = buildParentPath(key);
        RegisterMetadata registerMetadata = RegisterMetadata.of(ephemeral);
        Instance instance = new Instance();
        instance.setIp(nacosProps.getValue(NacosPropertyKey.CLUSTER_IP));
        instance.setPort(registerMetadata.getPort());
        instance.setEphemeral(ephemeral);
        Map<String, String> metadataMap = new HashMap<>(5, 1);
        if (ephemeral) {
            fillEphemeralMetadata(metadataMap);
        }
        metadataMap.put(key, value);
        metadataMap.put(MetadataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(MetadataUtil.getTimestamp()));
        instance.setMetadata(metadataMap);
        client.registerInstance(registerMetadata.name(), instance);
        keyValues.add(new KeyValue(key, value, ephemeral));
        waitValue(keyValues);
    }
    
    private List<KeyValue> buildParentPath(final String key) throws NacosException {
        List<KeyValue> keyValues = new LinkedList<>();
        StringBuilder parentPath = new StringBuilder();
        String[] partPath = key.split(PATH_SEPARATOR);
        for (int index = 1; index < partPath.length - 1; index++) {
            String path = parentPath.append(PATH_SEPARATOR).append(partPath[index]).toString();
            if (findExisted(path, false).isEmpty()) {
                keyValues.addAll(build(path));
            }
        }
        return keyValues;
    }
    
    private List<KeyValue> build(final String key) throws NacosException {
        List<KeyValue> keyValues = new LinkedList<>();
        if (findExisted(key, RegisterMetadata.PERSISTENT.isEphemeral()).isEmpty()) {
            Instance instance = new Instance();
            instance.setIp(nacosProps.getValue(NacosPropertyKey.CLUSTER_IP));
            instance.setPort(RegisterMetadata.PERSISTENT.getPort());
            instance.setEphemeral(false);
            Map<String, String> metadataMap = new HashMap<>(2, 1);
            metadataMap.put(key, MetadataUtil.EMPTY);
            metadataMap.put(MetadataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(MetadataUtil.getTimestamp()));
            instance.setMetadata(metadataMap);
            client.registerInstance(RegisterMetadata.PERSISTENT.name(), instance);
            keyValues.add(new KeyValue(key, MetadataUtil.EMPTY, false));
        }
        return keyValues;
    }
    
    private void fillEphemeralMetadata(final Map<String, String> metadataMap) {
        int timeToLiveSeconds = nacosProps.getValue(NacosPropertyKey.TIME_TO_LIVE_SECONDS);
        metadataMap.put(PreservedMetadataKeys.HEART_BEAT_INTERVAL, String.valueOf(timeToLiveSeconds * 1000 / 3));
        metadataMap.put(PreservedMetadataKeys.HEART_BEAT_TIMEOUT, String.valueOf(timeToLiveSeconds * 1000 * 2 / 3));
        metadataMap.put(PreservedMetadataKeys.IP_DELETE_TIMEOUT, String.valueOf(timeToLiveSeconds * 1000));
    }
    
    private void update(final Instance instance, final String value) throws NacosException, InterruptedException {
        Map<String, String> metadataMap = instance.getMetadata();
        String key = MetadataUtil.getKey(instance);
        metadataMap.put(key, value);
        metadataMap.put(MetadataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(MetadataUtil.getTimestamp()));
        instance.setMetadata(metadataMap);
        client.registerInstance(RegisterMetadata.PERSISTENT.name(), instance);
        LinkedList<KeyValue> keyValues = new LinkedList<>();
        keyValues.add(new KeyValue(key, value, instance.isEphemeral()));
        waitValue(keyValues);
    }
    
    @Override
    public void delete(final String key) {
        try {
            for (RegisterMetadata registerMetadata : RegisterMetadata.values()) {
                List<Instance> instances = findExisted(registerMetadata.isEphemeral()).stream()
                        .filter(instance -> {
                            String fullPath = MetadataUtil.getKey(instance);
                            return fullPath.startsWith(key + PATH_SEPARATOR) || StringUtils.equals(fullPath, key);
                        })
                        .sorted(Comparator.comparing(MetadataUtil::getKey).reversed()).collect(Collectors.toList());
                List<KeyValue> keyValues = new LinkedList<>();
                for (Instance instance : instances) {
                    client.deregisterInstance(registerMetadata.name(), instance);
                    keyValues.add(new KeyValue(MetadataUtil.getKey(instance), null, registerMetadata.isEphemeral()));
                }
                waitValue(keyValues);
            }
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    private List<Instance> findExisted(final String key, final boolean ephemeral) throws NacosException {
        return client.getAllInstances(RegisterMetadata.of(ephemeral).name(), false).stream()
                .filter(instance -> Objects.equals(key, MetadataUtil.getKey(instance))).collect(Collectors.toList());
    }
    
    private List<Instance> findExisted(final boolean ephemeral) throws NacosException {
        return client.getAllInstances(RegisterMetadata.of(ephemeral).name(), false);
    }
    
    private void waitValue(final List<KeyValue> keyValues) throws NacosException, InterruptedException {
        if (!isAvailable(keyValues)) {
            long retryIntervalMilliseconds = nacosProps.getValue(NacosPropertyKey.RETRY_INTERVAL_MILLISECONDS);
            int maxRetries = nacosProps.getValue(NacosPropertyKey.MAX_RETRIES);
            for (int retry = 0; retry < maxRetries; retry++) {
                Thread.sleep(getSleepTimeMs(retry, retryIntervalMilliseconds));
                if (isAvailable(keyValues)) {
                    return;
                }
            }
            throw new NacosException(NacosException.RESOURCE_NOT_FOUND, "Wait value availability timeout exceeded");
        }
    }
    
    private boolean isAvailable(final List<KeyValue> keyValues) throws NacosException {
        Map<Boolean, List<KeyValue>> keyValueMap = keyValues.stream().collect(Collectors.groupingBy(KeyValue::isEphemeral));
        for (Map.Entry<Boolean, List<KeyValue>> entry : keyValueMap.entrySet()) {
            Map<String, List<Instance>> instanceMap = client.getAllInstances(RegisterMetadata.of(entry.getKey()).name(), false).stream()
                    .collect(Collectors.groupingBy(MetadataUtil::getKey));
            keyValues.removeIf(keyValue -> {
                List<Instance> instances = instanceMap.get(keyValue.getKey());
                String value = keyValue.getValue();
                return CollectionUtils.isNotEmpty(instances) ? instances.stream().anyMatch(instance -> StringUtils.equals(MetadataUtil.getValue(instance), value)) : Objects.isNull(value);
            });
        }
        return keyValues.isEmpty();
    }
    
    private long getSleepTimeMs(final int retryCount, final long baseSleepTimeMs) {
        // copied from Hadoop's RetryPolicies.java
        return baseSleepTimeMs * Math.max(1, new Random().nextInt(1 << (retryCount + 1)));
    }
    
    @Override
    public void close() {
        try {
            client.shutDown();
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    @Override
    public String getType() {
        return "Nacos";
    }
}
