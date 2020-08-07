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

package org.apache.shardingsphere.orchestration.repository.zookeeper;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.orchestration.repository.api.RegistryRepository;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEventListener;
import org.apache.shardingsphere.orchestration.repository.zookeeper.handler.CuratorZookeeperExceptionHandler;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.OperationTimeoutException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Orchestration repository of ZooKeeper.
 */
public final class CuratorZookeeperRepository implements ConfigurationRepository, RegistryRepository {
    
    private final Map<String, TreeCache> caches = new HashMap<>();
    
    private CuratorFramework client;
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Override
    public void init(final String name, final OrchestrationCenterConfiguration config) {
        ZookeeperProperties zookeeperProperties = new ZookeeperProperties(props);
        client = buildCuratorClient(name, config, zookeeperProperties);
        initCuratorClient(zookeeperProperties);
    }
    
    private CuratorFramework buildCuratorClient(final String namespace, final OrchestrationCenterConfiguration config, final ZookeeperProperties zookeeperProperties) {
        int retryIntervalMilliseconds = zookeeperProperties.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS);
        int maxRetries = zookeeperProperties.getValue(ZookeeperPropertyKey.MAX_RETRIES);
        int timeToLiveSeconds = zookeeperProperties.getValue(ZookeeperPropertyKey.TIME_TO_LIVE_SECONDS);
        int operationTimeoutMilliseconds = zookeeperProperties.getValue(ZookeeperPropertyKey.OPERATION_TIMEOUT_MILLISECONDS);
        String digest = zookeeperProperties.getValue(ZookeeperPropertyKey.DIGEST);
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
            .connectString(config.getServerLists())
            .retryPolicy(new ExponentialBackoffRetry(retryIntervalMilliseconds, maxRetries, retryIntervalMilliseconds * maxRetries))
            .namespace(namespace);
        if (0 != timeToLiveSeconds) {
            builder.sessionTimeoutMs(timeToLiveSeconds * 1000);
        }
        if (0 != operationTimeoutMilliseconds) {
            builder.connectionTimeoutMs(operationTimeoutMilliseconds);
        }
        if (!Strings.isNullOrEmpty(digest)) {
            builder.authorization("digest", digest.getBytes(Charsets.UTF_8))
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
    
    private void initCuratorClient(final ZookeeperProperties zookeeperProperties) {
        client.start();
        try {
            int retryIntervalMilliseconds = zookeeperProperties.getValue(ZookeeperPropertyKey.RETRY_INTERVAL_MILLISECONDS);
            int maxRetries = zookeeperProperties.getValue(ZookeeperPropertyKey.MAX_RETRIES);
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
        TreeCache cache = findTreeCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        ChildData resultInCache = cache.getCurrentData(key);
        if (null != resultInCache) {
            return null == resultInCache.getData() ? null : new String(resultInCache.getData(), Charsets.UTF_8);
        }
        return getDirectly(key);
    }
    
    private TreeCache findTreeCache(final String key) {
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
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(Charsets.UTF_8));
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
            client.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Charsets.UTF_8)).and().commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    private String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), Charsets.UTF_8);
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
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(Charsets.UTF_8));
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
        String path = key + "/";
        if (!caches.containsKey(path)) {
            addCacheData(key);
        }
        TreeCache cache = caches.get(path);
        cache.getListenable().addListener((client, event) -> {
            ChildData data = event.getData();
            if (null == data || null == data.getPath()) {
                return;
            }
            DataChangedEvent.ChangedType changedType = getChangedType(event);
            if (ChangedType.IGNORED != changedType) {
                listener.onChange(new DataChangedEvent(data.getPath(), null == data.getData() ? null : new String(data.getData(), Charsets.UTF_8), changedType));
            }
        });
    }
    
    private void addCacheData(final String cachePath) {
        TreeCache cache = new TreeCache(client, cachePath);
        try {
            cache.start();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + "/", cache);
    }
    
    private ChangedType getChangedType(final TreeCacheEvent event) {
        switch (event.getType()) {
            case NODE_ADDED:
                return ChangedType.ADDED;
            case NODE_UPDATED:
                return ChangedType.UPDATED;
            case NODE_REMOVED:
                return ChangedType.DELETED;
            default:
                return ChangedType.IGNORED;
        }
    }
    
    @Override
    public void close() {
        caches.values().forEach(TreeCache::close);
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
