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

package org.apache.shardingsphere.orchestration.distributed.lock.zookeeper.curator;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.shardingsphere.orchestration.distributed.lock.api.DistributedLockCenter;
import org.apache.shardingsphere.orchestration.distributed.lock.api.DistributedLockConfiguration;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.OperationTimeoutException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Distributed lock center for zookeeper with curator.
 * 
 * @author wangguangyuan
 */
public final class CuratorZookeeperDistributedLockCenter implements DistributedLockCenter {
    
    private final Map<String, TreeCache> caches = new HashMap<>();
    
    private CuratorFramework client;

    private InterProcessMutex leafLock;
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Override
    public void init(final DistributedLockConfiguration config) {
        client = buildCuratorClient(config);
        initCuratorClient(config);
    }
    
    private CuratorFramework buildCuratorClient(final DistributedLockConfiguration config) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(config.getServerLists())
                .retryPolicy(new ExponentialBackoffRetry(config.getRetryIntervalMilliseconds(), config.getMaxRetries(), config.getRetryIntervalMilliseconds() * config.getMaxRetries()))
                .namespace(config.getNamespace());
        if (0 != config.getTimeToLiveSeconds()) {
            builder.sessionTimeoutMs(config.getTimeToLiveSeconds() * 1000);
        }
        if (0 != config.getOperationTimeoutMilliseconds()) {
            builder.connectionTimeoutMs(config.getOperationTimeoutMilliseconds());
        }
        if (!Strings.isNullOrEmpty(config.getDigest())) {
            builder.authorization("digest", config.getDigest().getBytes(Charsets.UTF_8))
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
    
    private void initCuratorClient(final DistributedLockConfiguration config) {
        client.start();
        try {
            if (!client.blockUntilConnected(config.getRetryIntervalMilliseconds() * config.getMaxRetries(), TimeUnit.MILLISECONDS)) {
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
        for (Entry<String, TreeCache> entry : caches.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
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
    public void close() {
        for (Entry<String, TreeCache> each : caches.entrySet()) {
            each.getValue().close();
        }
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
    }
    
    /* TODO 等待500ms, cache先关闭再关闭client, 否则会抛异常
     * 因为异步处理, 可能会导致client先关闭而cache还未关闭结束.
     * 等待Curator新版本解决这个bug.
     * BUG地址：https://issues.apache.org/jira/browse/CURATOR-157
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
        return "zookeeper";
    }

    @Override
    public void initLock(final String key) {
        leafLock = new InterProcessMutex(client, key);
    }

    @Override
    @SneakyThrows
    public boolean tryLock() {
        return leafLock.acquire(5, TimeUnit.SECONDS);
    }

    @Override
    @SneakyThrows
    public void tryRelease() {
        leafLock.release();
    }
}
