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
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.LeaderExecutionCallback;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterPersistRepositoryException;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.nacos.entity.KeyValue;
import org.apache.shardingsphere.mode.repository.cluster.nacos.entity.ServiceController;
import org.apache.shardingsphere.mode.repository.cluster.nacos.entity.ServiceMetadata;
import org.apache.shardingsphere.mode.repository.cluster.nacos.listener.NamingEventListener;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosProperties;
import org.apache.shardingsphere.mode.repository.cluster.nacos.props.NacosPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.nacos.utils.NacosMetaDataUtil;
import org.apache.shardingsphere.mode.repository.cluster.transaction.TransactionOperation;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Registry repository of Nacos.
 */
public final class NacosRepository implements ClusterPersistRepository {
    
    private NamingService client;
    
    private NacosProperties nacosProps;
    
    private ServiceController serviceController;
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config) {
        nacosProps = new NacosProperties(config.getProps());
        client = createClient(config);
        initServiceMetadata();
    }
    
    @Override
    public int getNumChildren(final String key) {
        return 0;
    }
    
    @Override
    public void addCacheData(final String cachePath) {
        // TODO
    }
    
    @Override
    public void evictCacheData(final String cachePath) {
        // TODO
    }
    
    @Override
    public Object getRawCache(final String cachePath) {
        // TODO
        return null;
    }
    
    @Override
    public void executeInLeader(final String key, final LeaderExecutionCallback callback) {
        // TODO
    }
    
    @Override
    public void executeInTransaction(final List<TransactionOperation> transactionOperations) {
        // TODO
    }
    
    @Override
    public void updateInTransaction(final String key, final String value) {
        // TODO
    }
    
    private NamingService createClient(final ClusterPersistRepositoryConfiguration config) {
        Properties props = new Properties();
        props.setProperty("serverAddr", config.getServerLists());
        props.setProperty("namespace", config.getNamespace());
        try {
            return NamingFactory.createNamingService(props);
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    private void initServiceMetadata() {
        try {
            String clusterIp = nacosProps.getValue(NacosPropertyKey.CLUSTER_IP);
            String ip = Strings.isNullOrEmpty(clusterIp) ? IpUtils.getIp() : clusterIp;
            serviceController = new ServiceController();
            for (ServiceMetadata each : serviceController.getAllServices()) {
                Integer port = client.getAllInstances(each.getServiceName(), false).stream()
                        .filter(instance -> StringUtils.equals(instance.getIp(), ip)).map(Instance::getPort).max(Comparator.naturalOrder()).orElse(Integer.MIN_VALUE);
                each.setIp(ip);
                each.setPort(new AtomicInteger(port));
            }
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            Preconditions.checkNotNull(value, "Value can not be null");
            if (!findExistedInstance(key, true).isEmpty()) {
                delete(key);
            }
            put(key, value, true);
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public void persistExclusiveEphemeral(final String key, final String value) {
        try {
            Preconditions.checkState(findExistedInstance(key, true).isEmpty(), "Key `%s` already exists", key);
            put(key, value, true);
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public boolean tryLock(final String lockKey, final long timeoutMillis) {
        // TODO
        return false;
    }
    
    @Override
    public void unlock(final String lockKey) {
        // TODO
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener, final Executor executor) {
        try {
            for (ServiceMetadata each : serviceController.getAllServices()) {
                NamingEventListener eventListener = each.getListener();
                if (null != eventListener) {
                    eventListener.put(key, listener);
                    return;
                }
                eventListener = new NamingEventListener();
                eventListener.put(key, listener);
                each.setListener(eventListener);
                client.subscribe(each.getServiceName(), eventListener);
            }
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public String get(final String key) {
        try {
            for (ServiceMetadata each : serviceController.getAllServices()) {
                Optional<Instance> instance = findExistedInstance(key, each.isEphemeral()).stream().max(Comparator.comparing(NacosMetaDataUtil::getTimestamp));
                if (instance.isPresent()) {
                    return NacosMetaDataUtil.getValue(instance.get());
                }
            }
            return null;
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        try {
            Stream<String> concatKeys = Stream.empty();
            for (ServiceMetadata each : serviceController.getAllServices()) {
                Stream<String> keys = findExistedInstance(each.isEphemeral()).stream()
                        .map(instance -> {
                            String fullPath = NacosMetaDataUtil.getKey(instance);
                            if (fullPath.startsWith(key + PATH_SEPARATOR)) {
                                String pathWithoutPrefix = fullPath.substring((key + PATH_SEPARATOR).length());
                                return pathWithoutPrefix.contains(PATH_SEPARATOR) ? pathWithoutPrefix.substring(0, pathWithoutPrefix.indexOf(PATH_SEPARATOR)) : pathWithoutPrefix;
                            }
                            return null;
                        }).filter(Objects::nonNull);
                concatKeys = Stream.concat(concatKeys, keys);
            }
            return concatKeys.distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public boolean isExisted(final String key) {
        return false;
    }
    
    @Override
    public void persist(final String key, final String value) {
        try {
            Preconditions.checkNotNull(value, "Value can not be null");
            Optional<Instance> instance = findExistedInstance(key, false).stream().max(Comparator.comparing(NacosMetaDataUtil::getTimestamp));
            if (instance.isPresent()) {
                update(instance.get(), value);
            } else {
                put(key, value, false);
            }
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public void update(final String key, final String value) {
        // TODO
    }
    
    private void update(final Instance instance, final String value) throws NacosException {
        Map<String, String> metadataMap = instance.getMetadata();
        String key = NacosMetaDataUtil.getKey(instance);
        metadataMap.put(key, value);
        metadataMap.put(NacosMetaDataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(NacosMetaDataUtil.getTimestamp()));
        instance.setMetadata(metadataMap);
        ServiceMetadata persistentService = serviceController.getPersistentService();
        client.registerInstance(persistentService.getServiceName(), instance);
        Collection<KeyValue> keyValues = new LinkedList<>();
        keyValues.add(new KeyValue(key, value, instance.isEphemeral()));
        waitValue(keyValues);
    }
    
    private void put(final String key, final String value, final boolean ephemeral) throws NacosException {
        final Collection<KeyValue> keyValues = buildParentPath(key);
        ServiceMetadata serviceMetadata = serviceController.getService(ephemeral);
        Instance instance = new Instance();
        instance.setIp(serviceMetadata.getIp());
        instance.setPort(serviceMetadata.getPort());
        instance.setEphemeral(ephemeral);
        Map<String, String> metadataMap = new HashMap<>(5, 1);
        if (ephemeral) {
            fillEphemeralMetadata(metadataMap);
        }
        metadataMap.put(key, value);
        metadataMap.put(NacosMetaDataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(NacosMetaDataUtil.getTimestamp()));
        instance.setMetadata(metadataMap);
        client.registerInstance(serviceMetadata.getServiceName(), instance);
        keyValues.add(new KeyValue(key, value, ephemeral));
        waitValue(keyValues);
    }
    
    private Collection<KeyValue> buildParentPath(final String key) throws NacosException {
        Collection<KeyValue> result = new LinkedList<>();
        StringBuilder parentPath = new StringBuilder();
        String[] partPath = key.split(PATH_SEPARATOR);
        for (int index = 1; index < partPath.length - 1; index++) {
            String path = parentPath.append(PATH_SEPARATOR).append(partPath[index]).toString();
            if (findExistedInstance(path, false).isEmpty()) {
                result.addAll(build(path));
            }
        }
        return result;
    }
    
    private Collection<KeyValue> build(final String key) throws NacosException {
        Collection<KeyValue> result = new LinkedList<>();
        if (findExistedInstance(key, false).isEmpty()) {
            Instance instance = new Instance();
            ServiceMetadata persistentService = serviceController.getPersistentService();
            instance.setIp(persistentService.getIp());
            instance.setPort(persistentService.getPort());
            instance.setEphemeral(false);
            Map<String, String> metadataMap = new HashMap<>(2, 1);
            metadataMap.put(key, "");
            metadataMap.put(NacosMetaDataUtil.UTC_ZONE_OFFSET.toString(), String.valueOf(NacosMetaDataUtil.getTimestamp()));
            instance.setMetadata(metadataMap);
            client.registerInstance(persistentService.getServiceName(), instance);
            result.add(new KeyValue(key, "", false));
        }
        return result;
    }
    
    private void fillEphemeralMetadata(final Map<String, String> metadataMap) {
        int timeToLiveSeconds = nacosProps.getValue(NacosPropertyKey.TIME_TO_LIVE_SECONDS);
        metadataMap.put(PreservedMetadataKeys.HEART_BEAT_INTERVAL, String.valueOf(timeToLiveSeconds * 1000 / 3));
        metadataMap.put(PreservedMetadataKeys.HEART_BEAT_TIMEOUT, String.valueOf(timeToLiveSeconds * 1000 * 2 / 3));
        metadataMap.put(PreservedMetadataKeys.IP_DELETE_TIMEOUT, String.valueOf(timeToLiveSeconds * 1000));
    }
    
    @Override
    public void delete(final String key) {
        try {
            for (ServiceMetadata each : serviceController.getAllServices()) {
                Collection<Instance> instances = findExistedInstance(each.isEphemeral()).stream()
                        .filter(instance -> {
                            String fullPath = NacosMetaDataUtil.getKey(instance);
                            return fullPath.startsWith(key + PATH_SEPARATOR) || StringUtils.equals(fullPath, key);
                        })
                        .sorted(Comparator.comparing(NacosMetaDataUtil::getKey).reversed()).collect(Collectors.toList());
                Collection<KeyValue> keyValues = new LinkedList<>();
                for (Instance instance : instances) {
                    client.deregisterInstance(each.getServiceName(), instance);
                    keyValues.add(new KeyValue(NacosMetaDataUtil.getKey(instance), null, each.isEphemeral()));
                }
                waitValue(keyValues);
            }
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public long getRegistryCenterTime(final String key) {
        return 0;
    }
    
    @Override
    public Object getRawClient() {
        return client;
    }
    
    private Collection<Instance> findExistedInstance(final String key, final boolean ephemeral) throws NacosException {
        return client.getAllInstances(serviceController.getService(ephemeral).getServiceName(), false).stream()
                .filter(each -> Objects.equals(key, NacosMetaDataUtil.getKey(each))).collect(Collectors.toList());
    }
    
    private Collection<Instance> findExistedInstance(final boolean ephemeral) throws NacosException {
        return client.getAllInstances(serviceController.getService(ephemeral).getServiceName(), false);
    }
    
    @SneakyThrows(InterruptedException.class)
    private void waitValue(final Collection<KeyValue> keyValues) throws NacosException {
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
    
    private boolean isAvailable(final Collection<KeyValue> keyValues) throws NacosException {
        Map<Boolean, List<KeyValue>> keyValueMap = keyValues.stream().collect(Collectors.groupingBy(KeyValue::isEphemeral));
        for (Entry<Boolean, List<KeyValue>> entry : keyValueMap.entrySet()) {
            ServiceMetadata service = serviceController.getService(entry.getKey());
            Map<String, List<Instance>> instanceMap = client.getAllInstances(service.getServiceName(), false).stream().collect(Collectors.groupingBy(NacosMetaDataUtil::getKey));
            keyValues.removeIf(keyValue -> {
                Collection<Instance> instances = instanceMap.get(keyValue.getKey());
                String value = keyValue.getValue();
                return CollectionUtils.isNotEmpty(instances) ? instances.stream().anyMatch(instance -> StringUtils.equals(NacosMetaDataUtil.getValue(instance), value)) : Objects.isNull(value);
            });
        }
        return keyValues.isEmpty();
    }
    
    private long getSleepTimeMs(final int retryCount, final long baseSleepTimeMs) {
        return baseSleepTimeMs * Math.max(1, new Random().nextInt(1 << (retryCount + 1)));
    }
    
    @Override
    public void close() {
        try {
            client.shutDown();
        } catch (final NacosException ex) {
            throw new ClusterPersistRepositoryException(ex);
        }
    }
    
    @Override
    public String getType() {
        return "Nacos";
    }
}
