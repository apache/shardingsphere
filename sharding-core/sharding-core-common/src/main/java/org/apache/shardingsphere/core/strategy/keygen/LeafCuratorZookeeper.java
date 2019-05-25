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

package org.apache.shardingsphere.core.strategy.keygen;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jason on 2019/4/28.
 */
public final class LeafCuratorZookeeper {

    private CuratorFramework client;

    public void init(final LeafConfiguration config) {
        client = buildCuratorClient(config);
        initCuratorClient(config);
    }

    private CuratorFramework buildCuratorClient(final LeafConfiguration config) {
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

    private void initCuratorClient(final LeafConfiguration config) {
        client.start();
        try {
            if (!client.blockUntilConnected(config.getRetryIntervalMilliseconds() * config.getMaxRetries(), TimeUnit.MILLISECONDS)) {
                client.close();
                throw new KeeperException.OperationTimeoutException();
            }
        } catch (final InterruptedException | KeeperException.OperationTimeoutException ex) {
            LeafExceptionHandler.handleException(ex);
        }
    }

    public String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), Charsets.UTF_8);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            LeafExceptionHandler.handleException(ex);
            return null;
        }
    }

    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            LeafExceptionHandler.handleException(ex);
            return false;
        }
    }

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
            LeafExceptionHandler.handleException(ex);
        }
    }

    public void update(final String key, final String value) {
        try {
            client.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Charsets.UTF_8)).and().commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            LeafExceptionHandler.handleException(ex);
        }
    }

    public String getType() {
        return "zookeeper";
    }

    public long incrementCacheId(final String tableName,final long step){
        InterProcessMutex lock = new InterProcessMutex(client, tableName);
        long result=Long.MIN_VALUE;
        boolean lockIsAcquired = tryLock(lock);
        if ( lockIsAcquired ) {
            result = updateCacheIdInCenter(tableName, step);
            tryRelease(lock);
        }
        return  result;
    }

    private long updateCacheIdInCenter(final String tableName,final long step){
        long cacheId = Long.parseLong(getDirectly(tableName));
        long result = cacheId+step;
        update(tableName, String.valueOf(result));
        return result;
    }

    private boolean tryLock(final InterProcessMutex lock){
        try{
            return lock.acquire(5,TimeUnit.SECONDS);
        }catch (Exception e){
            LeafExceptionHandler.handleException(e);
            return Boolean.FALSE;
        }
    }

    private void tryRelease(final InterProcessMutex lock){
        try{
            lock.release();
        }catch (Exception e){
            LeafExceptionHandler.handleException(e);
        }
    }
}
