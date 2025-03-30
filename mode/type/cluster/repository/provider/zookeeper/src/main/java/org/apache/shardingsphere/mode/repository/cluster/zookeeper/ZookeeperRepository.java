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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper;

import com.google.common.base.Strings;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterRepositoryPersistException;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.exception.ZookeeperExceptionHandler;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.listener.SessionConnectionReconnectListener;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.lock.ZookeeperDistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperProperties;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperPropertyKey;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.KeeperException.OperationTimeoutException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Registry repository of ZooKeeper.
 */
public final class ZookeeperRepository implements ClusterPersistRepository {
    
    private final Map<String, CuratorCache> caches = new ConcurrentHashMap<>();
    
    private final Map<String, CuratorCacheListener> dataListeners = new ConcurrentHashMap<>();
    
    private final Builder builder = CuratorFrameworkFactory.builder();
    
    private CuratorFramework client;
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config, final ComputeNodeInstanceContext computeNodeInstanceContext) {
        ZookeeperProperties zookeeperProps = new ZookeeperProperties(config.getProps());
        client = buildCuratorClient(config, zookeeperProps);
        client.getConnectionStateListenable().addListener(new SessionConnectionReconnectListener(computeNodeInstanceContext, this));
        initCuratorClient(zookeeperProps);
    }
    
    private CuratorFramework buildCuratorClient(final ClusterPersistRepositoryConfiguration config, final ZookeeperProperties zookeeperProps) {
        int retryIntervalMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS);
        int maxRetries = zookeeperProps.getValue(ZookeeperPropertyKey.MAX_RETRIES);
        int timeToLiveSeconds = zookeeperProps.getValue(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS);
        int operationTimeoutMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS);
        builder.connectString(config.getServerLists())
                .ensembleTracker(false)
                .retryPolicy(new ExponentialBackoffRetry(retryIntervalMilliseconds, maxRetries, retryIntervalMilliseconds * maxRetries))
                .namespace(config.getNamespace());
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
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (final OperationTimeoutException ex) {
            ZookeeperExceptionHandler.handleException(ex);
        }
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
            ZookeeperExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void persist(final String key, final String value) {
        try {
            if (isExisted(key)) {
                update(key, value);
            } else {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            ZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void update(final String key, final String value) {
        try {
            client.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            ZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public String query(final String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
        } catch (final KeeperException.NoNodeException ex) {
            return null;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new ClusterRepositoryPersistException(ex);
        }
    }
    
    @Override
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            ZookeeperExceptionHandler.handleException(ex);
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
            ZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public boolean persistExclusiveEphemeral(final String key, final String value) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (final NodeExistsException ex) {
            return false;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            ZookeeperExceptionHandler.handleException(ex);
            // CHECKSTYLE:ON
        }
        return true;
    }
    
    @Override
    public Optional<DistributedLock> getDistributedLock(final String lockKey) {
        return Optional.of(new ZookeeperDistributedLock(lockKey, client));
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
            ZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        if (null != dataListeners.get(key)) {
            return;
        }
        CuratorCache cache = caches.get(key);
        if (null == cache) {
            cache = CuratorCache.build(client, key);
            caches.put(key, cache);
        }
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder()
                .forCreates(childData -> listener.onChange(new DataChangedEvent(childData.getPath(), new String(childData.getData(), StandardCharsets.UTF_8), Type.ADDED)))
                .forChanges((oldData, newData) -> {
                    if (!Objects.equals(oldData, newData)) {
                        listener.onChange(new DataChangedEvent(newData.getPath(), new String(newData.getData(), StandardCharsets.UTF_8), Type.UPDATED));
                    }
                })
                .forDeletes(oldData -> listener.onChange(new DataChangedEvent(oldData.getPath(), new String(oldData.getData(), StandardCharsets.UTF_8), Type.DELETED)))
                .afterInitialized()
                .build();
        cache.listenable().addListener(curatorCacheListener);
        cache.start();
        dataListeners.computeIfAbsent(key, curator -> curatorCacheListener);
    }
    
    @Override
    public void removeDataListener(final String key) {
        CuratorCacheListener cacheListener = dataListeners.remove(key);
        if (null == cacheListener) {
            return;
        }
        Optional.ofNullable(caches.remove(key)).ifPresent(optional -> optional.listenable().removeListener(cacheListener));
    }
    
    @Override
    public void close() {
        caches.values().forEach(CuratorCache::close);
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
    }
    
    /*
     * TODO wait 500ms, close cache before close client, or will throw exception Because of asynchronous processing, may cause client to close first and cache has not yet closed the end. Wait for new
     * version of Curator to fix this. BUG address: https://issues.apache.org/jira/browse/CURATOR-157
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
