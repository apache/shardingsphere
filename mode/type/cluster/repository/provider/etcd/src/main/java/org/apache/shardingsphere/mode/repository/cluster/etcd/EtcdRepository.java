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

package org.apache.shardingsphere.mode.repository.cluster.etcd;

import com.google.common.base.Splitter;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.support.Observers;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.lite.internal.storage.LeaderExecutionCallback;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.etcd.lock.EtcdInternalLockProvider;
import org.apache.shardingsphere.mode.repository.cluster.etcd.props.EtcdProperties;
import org.apache.shardingsphere.mode.repository.cluster.etcd.props.EtcdPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.transaction.TransactionOperation;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Registry repository of ETCD.
 */
@Slf4j
public final class EtcdRepository implements ClusterPersistRepository {
    
    private Client client;
    
    private EtcdProperties etcdProps;
    
    private EtcdInternalLockProvider etcdInternalLockHolder;
    
    @Override
    public void init(final ClusterPersistRepositoryConfiguration config) {
        etcdProps = new EtcdProperties(config.getProps());
        client = Client.builder().endpoints(Util.toURIs(Splitter.on(",").trimResults().splitToList(config.getServerLists())))
                .namespace(ByteSequence.from(config.getNamespace(), StandardCharsets.UTF_8))
                .maxInboundMessageSize((int) 32e9)
                .build();
        etcdInternalLockHolder = new EtcdInternalLockProvider(client, etcdProps);
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
    
    @Override
    public String get(final String key) {
        // TODO
        return null;
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public String getDirectly(final String key) {
        List<KeyValue> keyValues = client.getKVClient().get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
        return keyValues.isEmpty() ? null : keyValues.iterator().next().getValue().toString(StandardCharsets.UTF_8);
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public List<String> getChildrenKeys(final String key) {
        String prefix = key + PATH_SEPARATOR;
        ByteSequence prefixByteSequence = ByteSequence.from(prefix, StandardCharsets.UTF_8);
        GetOption getOption = GetOption.newBuilder().isPrefix(true).withSortField(GetOption.SortTarget.KEY).withSortOrder(GetOption.SortOrder.ASCEND).build();
        List<KeyValue> keyValues = client.getKVClient().get(prefixByteSequence, getOption).get().getKvs();
        return keyValues.stream().map(each -> getSubNodeKeyName(prefix, each.getKey().toString(StandardCharsets.UTF_8))).distinct().collect(Collectors.toList());
    }
    
    @Override
    public boolean isExisted(final String key) {
        return false;
    }
    
    private String getSubNodeKeyName(final String prefix, final String fullPath) {
        String pathWithoutPrefix = fullPath.substring(prefix.length());
        return pathWithoutPrefix.contains(PATH_SEPARATOR) ? pathWithoutPrefix.substring(0, pathWithoutPrefix.indexOf(PATH_SEPARATOR)) : pathWithoutPrefix;
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public void persist(final String key, final String value) {
        buildParentPath(key);
        client.getKVClient().put(ByteSequence.from(key, StandardCharsets.UTF_8), ByteSequence.from(value, StandardCharsets.UTF_8)).get();
    }
    
    @Override
    public void update(final String key, final String value) {
        // TODO
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public void persistEphemeral(final String key, final String value) {
        buildParentPath(key);
        long leaseId = client.getLeaseClient().grant(etcdProps.getValue(EtcdPropertyKey.TIME_TO_LIVE_SECONDS)).get().getID();
        client.getLeaseClient().keepAlive(leaseId, Observers.observer(response -> {
        }));
        client.getKVClient().put(ByteSequence.from(key, StandardCharsets.UTF_8), ByteSequence.from(value, StandardCharsets.UTF_8), PutOption.newBuilder().withLeaseId(leaseId).build()).get();
    }
    
    @Override
    public void persistExclusiveEphemeral(final String key, final String value) {
        persistEphemeral(key, value);
    }
    
    private void buildParentPath(final String key) throws ExecutionException, InterruptedException {
        StringBuilder parentPath = new StringBuilder();
        String[] partPath = key.split(PATH_SEPARATOR);
        for (int index = 1; index < partPath.length - 1; index++) {
            parentPath.append(PATH_SEPARATOR);
            parentPath.append(partPath[index]);
            String path = parentPath.toString();
            List<KeyValue> keyValues = client.getKVClient().get(ByteSequence.from(path, StandardCharsets.UTF_8)).get().getKvs();
            if (keyValues.isEmpty()) {
                client.getKVClient().put(ByteSequence.from(path, StandardCharsets.UTF_8), ByteSequence.from("", StandardCharsets.UTF_8)).get();
            }
        }
    }
    
    @Override
    public void delete(final String key) {
        client.getKVClient().delete(ByteSequence.from(key, StandardCharsets.UTF_8), DeleteOption.newBuilder().isPrefix(true).build());
    }
    
    @Override
    public long getRegistryCenterTime(final String key) {
        return 0;
    }
    
    @Override
    public Object getRawClient() {
        return client;
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener, final Executor executor) {
        Watch.Listener listener = Watch.listener(response -> {
            for (WatchEvent each : response.getEvents()) {
                Type type = getEventChangedType(each);
                if (Type.IGNORED != type) {
                    dataChangedEventListener.onChange(new DataChangedEvent(each.getKeyValue().getKey().toString(StandardCharsets.UTF_8),
                            each.getKeyValue().getValue().toString(StandardCharsets.UTF_8), type));
                }
            }
        });
        client.getWatchClient().watch(ByteSequence.from(key, StandardCharsets.UTF_8),
                WatchOption.newBuilder().withPrefix(ByteSequence.from(key, StandardCharsets.UTF_8)).build(), listener);
    }
    
    private Type getEventChangedType(final WatchEvent event) {
        if (1 == event.getKeyValue().getVersion()) {
            return Type.ADDED;
        }
        switch (event.getEventType()) {
            case PUT:
                return Type.UPDATED;
            case DELETE:
                return Type.DELETED;
            default:
                return Type.IGNORED;
        }
    }
    
    @Override
    public boolean tryLock(final String lockKey, final long timeoutMillis) {
        return etcdInternalLockHolder.getInternalLock(lockKey).tryLock(timeoutMillis);
    }
    
    @Override
    public void unlock(final String lockKey) {
        etcdInternalLockHolder.getInternalLock(lockKey).unlock();
    }
    
    @Override
    public void close() {
        client.close();
    }
    
    @Override
    public String getType() {
        return "etcd";
    }
}
