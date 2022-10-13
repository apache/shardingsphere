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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.LeaderExecutionCallback;
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterPersistRepositoryException;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.handler.CuratorZookeeperExceptionHandler;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.listener.SessionConnectionListener;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.lock.ZookeeperInternalLockProvider;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperProperties;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.props.ZookeeperPropertyKey;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.KeeperException.OperationTimeoutException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Registry repository of ZooKeeper.
 */
public final class CuratorZookeeperRepository implements ClusterPersistRepository, InstanceContextAware {
    
    private final Map<String, CuratorCache> caches = new ConcurrentHashMap<>();
    
    private final Builder builder = CuratorFrameworkFactory.builder();
    
    private CuratorFramework client;
    
    private ZookeeperInternalLockProvider internalLockProvider;
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config) {
        ZookeeperProperties zookeeperProps = new ZookeeperProperties(config.getProps());
        client = buildCuratorClient(config, zookeeperProps);
        internalLockProvider = new ZookeeperInternalLockProvider(client);
        initCuratorClient(zookeeperProps);
    }
    
    private CuratorFramework buildCuratorClient(final ClusterPersistRepositoryConfiguration config, final ZookeeperProperties zookeeperProps) {
        int retryIntervalMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS);
        int maxRetries = zookeeperProps.getValue(ZookeeperPropertyKey.MAX_RETRIES);
        int timeToLiveSeconds = zookeeperProps.getValue(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS);
        int operationTimeoutMilliseconds = zookeeperProps.getValue(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS);
        builder.connectString(config.getServerLists())
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
        } catch (final InterruptedException | OperationTimeoutException ex) {
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public int getNumChildren(final String key) {
        try {
            Stat stat = client.checkExists().forPath(key);
            if (null != stat) {
                return stat.getNumChildren();
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
        return 0;
    }
    
    @Override
    public void addCacheData(final String cachePath) {
        CuratorCache cache = CuratorCache.build(client, cachePath);
        try {
            cache.start();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + "/", cache);
    }
    
    @Override
    public void evictCacheData(final String cachePath) {
        CuratorCache cache = caches.remove(cachePath + "/");
        if (null != cache) {
            cache.close();
        }
    }
    
    @Override
    public Object getRawCache(final String cachePath) {
        return caches.get(cachePath + "/");
    }
    
    @Override
    public void executeInLeader(final String key, final LeaderExecutionCallback callback) {
        // TODO
    }
    
    @Override
    public void updateInTransaction(final String key, final String value) {
        try {
            TransactionOp transactionOp = client.transactionOp();
            client.transaction().forOperations(transactionOp.check().forPath(key), transactionOp.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8)));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public String get(final String key) {
        CuratorCache cache = findCuratorCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        Optional<ChildData> resultInCache = cache.get(key);
        return resultInCache.map(v -> null == v.getData() ? null : new String(v.getData(), StandardCharsets.UTF_8)).orElseGet(() -> getDirectly(key));
    }
    
    private CuratorCache findCuratorCache(final String key) {
        for (Map.Entry<String, CuratorCache> entry : caches.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
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
    
    @Override
    public void update(final String key, final String value) {
        try {
            client.setData().forPath(key, value.getBytes(StandardCharsets.UTF_8));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), StandardCharsets.UTF_8);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    @Override
    public boolean isExisted(final String key) {
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
    public void persistExclusiveEphemeral(final String key, final String value) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            if (ex instanceof NodeExistsException) {
                throw new ClusterPersistRepositoryException(ex);
            }
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
    public long getRegistryCenterTime(final String key) {
        long result = 0L;
        try {
            persist(key, "");
            result = client.checkExists().forPath(key).getMtime();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
        Preconditions.checkState(0L != result, "Cannot get registry center time.");
        return result;
    }
    
    @Override
    public Object getRawClient() {
        return client;
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener, final Executor executor) {
        CuratorCache cache = caches.get(key);
        if (null == cache) {
            cache = CuratorCache.build(client, key);
            caches.put(key, cache);
        }
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder()
                .forTreeCache(client, (framework, treeCacheListener) -> {
                    Type changedType = getChangedType(treeCacheListener.getType());
                    if (Type.IGNORED != changedType) {
                        listener.onChange(new DataChangedEvent(treeCacheListener.getData().getPath(),
                                new String(treeCacheListener.getData().getData(), StandardCharsets.UTF_8), changedType));
                    }
                }).build();
        if (null != executor) {
            cache.listenable().addListener(curatorCacheListener, executor);
        } else {
            cache.listenable().addListener(curatorCacheListener);
        }
        start(cache);
    }
    
    private void start(final CuratorCache cache) {
        try {
            cache.start();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    private Type getChangedType(final TreeCacheEvent.Type type) {
        switch (type) {
            case NODE_ADDED:
                return Type.ADDED;
            case NODE_UPDATED:
                return Type.UPDATED;
            case NODE_REMOVED:
                return Type.DELETED;
            default:
                return Type.IGNORED;
        }
    }
    
    @Override
    public boolean tryLock(final String lockKey, final long timeoutMillis) {
        return internalLockProvider.getInternalLock(lockKey).tryLock(timeoutMillis);
    }
    
    @Override
    public void unlock(final String lockKey) {
        internalLockProvider.getInternalLock(lockKey).unlock();
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
    public void setInstanceContext(final InstanceContext instanceContext) {
        client.getConnectionStateListenable().addListener(new SessionConnectionListener(instanceContext, this));
    }
    
    @Override
    public String getType() {
        return "ZooKeeper";
    }
}
