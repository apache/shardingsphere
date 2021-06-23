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

package org.apache.shardingsphere.governance.repository.zookeeper;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.zookeeper.handler.CuratorZookeeperExceptionHandler;
import org.apache.shardingsphere.governance.repository.zookeeper.props.ZookeeperProperties;
import org.apache.shardingsphere.governance.repository.zookeeper.props.ZookeeperPropertyKey;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.OperationTimeoutException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Registry repository of ZooKeeper.
 */
public final class CuratorZookeeperRepository implements RegistryCenterRepository {
    
    private final Map<String, CuratorCache> caches = new HashMap<>();
    
    private CuratorFramework client;
    
    private final Builder builder = CuratorFrameworkFactory.builder();
    
    private final Map<String, InterProcessLock> locks = new ConcurrentHashMap<>();
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Override
    public void init(final String name, final RegistryCenterConfiguration config) {
        ZookeeperProperties zookeeperProps = new ZookeeperProperties(props);
        client = buildCuratorClient(name, config, zookeeperProps);
        initCuratorClient(zookeeperProps);
    }
    
    private CuratorFramework buildCuratorClient(final String namespace, final RegistryCenterConfiguration config, final ZookeeperProperties zookeeperProps) {
        int retryIntervalMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS);
        int maxRetries = zookeeperProps.getValue(ZookeeperPropertyKey.MAX_RETRIES);
        int timeToLiveSeconds = zookeeperProps.getValue(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS);
        int operationTimeoutMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS);
        builder.connectString(config.getServerLists())
            .retryPolicy(new ExponentialBackoffRetry(retryIntervalMilliseconds, maxRetries, retryIntervalMilliseconds * maxRetries))
            .namespace(namespace);
        if (0 != timeToLiveSeconds) {
            builder.sessionTimeoutMs(timeToLiveSeconds * 1000);
        }
        if (0 != operationTimeoutMilliseconds) {
            builder.connectionTimeoutMs(operationTimeoutMilliseconds);
        }
        String digest = zookeeperProps.getValue(ZookeeperPropertyKey.DIGEST);
        if (!Strings.isNullOrEmpty(digest)) {
            builder.authorization(ZookeeperPropertyKey.DIGEST.getKey(), digest.getBytes(StandardCharsets.UTF_8))
                .aclProvider(new ACLProvider() {
                    
                    @Override
                    public List<ACL> getDefaultAcl() {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }
                    
                    @Override
                    public List<ACL> getAclForPath(final String path) {
                        return ZooDefs.Ids.CREATOR_ALL_ACL;
                    }
                });
        }
        return builder.build();
    }
    
    private void initCuratorClient(final ZookeeperProperties zookeeperProps) {
        client.start();
        try {
            int retryIntervalMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS);
            int maxRetries = zookeeperProps.getValue(ZookeeperPropertyKey.MAX_RETRIES);
            if (!client.blockUntilConnected(retryIntervalMilliseconds * maxRetries, TimeUnit.MILLISECONDS)) {
                client.close();
                throw new OperationTimeoutException();
            }
        } catch (final InterruptedException | OperationTimeoutException ex) {
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public String get(final String key) {
        CuratorCache cache = findTreeCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        Optional<ChildData> resultInCache = cache.get(key);
        if (resultInCache.isPresent()) {
            return null == resultInCache.get().getData() ? null : new String(resultInCache.get().getData(), StandardCharsets.UTF_8);
        }
        return getDirectly(key);
    }
    
    private CuratorCache findTreeCache(final String key) {
        return caches.entrySet().stream().filter(entry -> key.startsWith(entry.getKey())).findFirst().map(Entry::getValue).orElse(null);
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            result.sort(Comparator.reverseOrder());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    private void update(final String key, final String value) {
        try {
            client.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    private String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    private boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
            return false;
        }
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void delete(final String key) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        String path = key + PATH_SEPARATOR;
        if (!caches.containsKey(path)) {
            addCacheData(key);
            CuratorCache cache = caches.get(path);
            cache.listenable().addListener((type, oldData, data) -> {
                String eventPath = CuratorCacheListener.Type.NODE_DELETED == type ? oldData.getPath() : data.getPath();
                byte[] eventDataByte = CuratorCacheListener.Type.NODE_DELETED == type ? oldData.getData() : data.getData();
                Type changedType = getChangedType(type);
                if (Type.IGNORED != changedType) {
                    listener.onChange(new DataChangedEvent(eventPath, null == eventDataByte ? null : new String(eventDataByte, StandardCharsets.UTF_8), changedType));
                }
            });
        }
    }
    
    private void addCacheData(final String cachePath) {
        CuratorCache cache = CuratorCache.build(client, cachePath);
        try {
            cache.start();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + PATH_SEPARATOR, cache);
    }
    
    private Type getChangedType(final CuratorCacheListener.Type type) {
        switch (type) {
            case NODE_CREATED:
                return Type.ADDED;
            case NODE_CHANGED:
                return Type.UPDATED;
            case NODE_DELETED:
                return Type.DELETED;
            default:
                return Type.IGNORED;
        }
    }
    
    @Override
    public boolean tryLock(final String key, final long time, final TimeUnit unit) {
        try {
            return getLock(key).acquire(time, unit);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
            return false;
        }
    }
    
    @Override
    public void releaseLock(final String key) {
        try {
            if (availableLock(key)) {
                locks.get(key).release();
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    private InterProcessLock getLock(final String key) {
        if (availableLock(key)) {
            return locks.get(key);
        }
        InterProcessLock lock = new InterProcessMutex(client, key);
        locks.put(key, lock);
        return lock;
    }
    
    private boolean availableLock(final String key) {
        return Objects.nonNull(locks.get(key));
    }
    
    @Override
    public void close() {
        caches.values().forEach(CuratorCache::close);
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
    }
    
    /* TODO wait 500ms, close cache before close client, or will throw exception
     * Because of asynchronous processing, may cause client to close
     * first and cache has not yet closed the end.
     * Wait for new version of Curator to fix this.
     * BUG address: https://issues.apache.org/jira/browse/CURATOR-157
     */
    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public String getType() {
        return "ZooKeeper";
    }
}
