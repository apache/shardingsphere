/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.reg.newzk;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.jdbc.orchestration.reg.exception.RegExceptionHandler;
import io.shardingsphere.jdbc.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.jdbc.orchestration.reg.listener.EventListener;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.StrategyType;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper native based registry center.
 * 
 * @author lidongbo
 */
public final class NewZookeeperRegistryCenter implements RegistryCenter {
    
    private final io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient client;
    
    private final Map<String, io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree> caches = new HashMap<>();
    
    public NewZookeeperRegistryCenter(final io.shardingsphere.jdbc.orchestration.reg.zookeeper.ZookeeperConfiguration zkConfig) {
        io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.ClientFactory creator = buildCreator(zkConfig);
        client = initClient(creator, zkConfig);
    }
    
    private io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.ClientFactory buildCreator(final io.shardingsphere.jdbc.orchestration.reg.zookeeper.ZookeeperConfiguration zkConfig) {
        io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.ClientFactory creator = new io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.ClientFactory();
        creator.setClientNamespace(zkConfig.getNamespace())
                .newClient(zkConfig.getServerLists(), zkConfig.getSessionTimeoutMilliseconds())
                .setRetryPolicy(new io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry.DelayRetryPolicy(zkConfig.getBaseSleepTimeMilliseconds(), zkConfig.getMaxRetries(), zkConfig.getMaxSleepTimeMilliseconds()));
        if (!Strings.isNullOrEmpty(zkConfig.getDigest())) {
            creator.authorization("digest", zkConfig.getDigest().getBytes(Charsets.UTF_8), ZooDefs.Ids.CREATOR_ALL_ACL);
        }
        return creator;
    }
    
    private io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient initClient(final io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.ClientFactory creator, final io.shardingsphere.jdbc.orchestration.reg.zookeeper.ZookeeperConfiguration zkConfig) {
        io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient newClient = null;
        try {
            newClient = creator.start();
            // block, slowly
            if (!newClient.blockUntilConnected(zkConfig.getMaxSleepTimeMilliseconds() * zkConfig.getMaxRetries(), TimeUnit.MILLISECONDS)) {
                newClient.close();
                throw new KeeperException.OperationTimeoutException();
            }
            newClient.useExecStrategy(StrategyType.SYNC_RETRY);
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(e);
        }
        return newClient;
    }
    
    @Override
    public String get(final String key) {
        io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree cache = findTreeCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        byte[] resultInCache = cache.getValue(key);
        if (null != resultInCache) {
            return null == resultInCache ? null : new String(resultInCache, Charsets.UTF_8);
        }
        return getDirectly(key);
    }
    
    private io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree findTreeCache(final String key) {
        for (Entry<String, io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree> entry : caches.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    @Override
    public String getDirectly(final String key) {
        try {
            return new String(client.getData(key), Charsets.UTF_8);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    @Override
    public boolean isExisted(final String key) {
        try {
            return client.checkExists(key);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return false;
        }
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        try {
            List<String> result = client.getChildren(key);
            Collections.sort(result, new Comparator<String>() {
                
                @Override
                public int compare(final String o1, final String o2) {
                    return o2.compareTo(o1);
                }
            });
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.createAllNeedPath(key, value, CreateMode.PERSISTENT);
            } else {
                update(key, value);
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void update(final String key, final String value) {
        try {
            client.transaction().check(key, -1).setData(key, value.getBytes(Charsets.UTF_8)).commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.deleteAllChildren(key);
            }
            client.createAllNeedPath(key, value, CreateMode.EPHEMERAL);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void watch(final String key, final EventListener eventListener) {
        final String path = key + "/";
        if (!caches.containsKey(path)) {
            addCacheData(key);
        }
        io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree cache = caches.get(path);
        cache.watch(new io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener() {
            @Override
            public void process(final WatchedEvent event) {
                if (!io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.StringUtil.isNullOrBlank(event.getPath())) {
                    eventListener.onChange(new DataChangedEvent(getEventType(event), event.getPath(), getWithoutCache(event.getPath())));
                }
            }
            
            private DataChangedEvent.Type getEventType(final WatchedEvent event) {
                switch (event.getType()) {
                    case NodeDataChanged:
                    case NodeChildrenChanged:
                        return DataChangedEvent.Type.UPDATED;
                    case NodeDeleted:
                        return DataChangedEvent.Type.DELETED;
                    default:
                        return DataChangedEvent.Type.IGNORED;
                }
            }
        });
    }
    
    private synchronized String getWithoutCache(final String key) {
        try {
            client.useExecStrategy(StrategyType.USUAL);
            byte[] data = client.getData(key);
            client.useExecStrategy(StrategyType.SYNC_RETRY);
            return null == data ? null : new String(data, Charsets.UTF_8);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    private void addCacheData(final String cachePath) {
        io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree cache = new io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree(cachePath, client);
        try {
            cache.load();
            cache.watch();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            RegExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + "/", cache);
    }
    
    @Override
    public void close() {
        for (Entry<String, io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree> each : caches.entrySet()) {
            each.getValue().close();
        }
        client.close();
    }
}
