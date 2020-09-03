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

package org.apache.shardingsphere.governance.repository.etcd;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Observers;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Registry repository of ETCD.
 */
public final class EtcdRepository implements ConfigurationRepository, RegistryRepository {
    
    private Client client;
    
    @Getter
    @Setter
    private Properties props = new Properties();

    private EtcdProperties etcdProperties;

    @Override
    public void init(final String name, final GovernanceCenterConfiguration config) {
        etcdProperties = new EtcdProperties(props);
        client = Client.builder().endpoints(Util.toURIs(Splitter.on(",").trimResults().splitToList(config.getServerLists()))).namespace(ByteSequence.from(name, Charsets.UTF_8)).build();
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public String get(final String key) {
        List<KeyValue> keyValues = client.getKVClient().get(ByteSequence.from(key, Charsets.UTF_8)).get().getKvs();
        return keyValues.isEmpty() ? null : keyValues.iterator().next().getValue().toString(Charsets.UTF_8);
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public List<String> getChildrenKeys(final String key) {
        String prefix = key + PATH_SEPARATOR;
        ByteSequence prefixByteSequence = ByteSequence.from(prefix, Charsets.UTF_8);
        GetOption getOption = GetOption.newBuilder().withPrefix(prefixByteSequence).withSortField(GetOption.SortTarget.KEY).withSortOrder(GetOption.SortOrder.ASCEND).build();
        List<KeyValue> keyValues = client.getKVClient().get(prefixByteSequence, getOption).get().getKvs();
        return keyValues.stream().map(e -> getSubNodeKeyName(prefix, e.getKey().toString(Charsets.UTF_8))).distinct().collect(Collectors.toList());
    }
    
    private String getSubNodeKeyName(final String prefix, final String fullPath) {
        String pathWithoutPrefix = fullPath.substring(prefix.length());
        return pathWithoutPrefix.contains(PATH_SEPARATOR) ? pathWithoutPrefix.substring(0, pathWithoutPrefix.indexOf(PATH_SEPARATOR)) : pathWithoutPrefix;
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public void persist(final String key, final String value) {
        client.getKVClient().put(ByteSequence.from(key, Charsets.UTF_8), ByteSequence.from(value, Charsets.UTF_8)).get();
    }
    
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public void persistEphemeral(final String key, final String value) {
        long leaseId = client.getLeaseClient().grant(etcdProperties.getValue(EtcdPropertyKey.TIME_TO_LIVE_SECONDS)).get().getID();
        client.getLeaseClient().keepAlive(leaseId, Observers.observer(response -> { }));
        client.getKVClient().put(ByteSequence.from(key, Charsets.UTF_8), ByteSequence.from(value, Charsets.UTF_8), PutOption.newBuilder().withLeaseId(leaseId).build()).get();
    }
    
    @Override
    public void delete(final String key) {
        client.getKVClient().delete(ByteSequence.from(key, Charsets.UTF_8));
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        Watch.Listener listener = Watch.listener(response -> {
            for (WatchEvent each : response.getEvents()) {
                ChangedType changedType = getEventChangedType(each);
                if (ChangedType.IGNORED != changedType) {
                    dataChangedEventListener.onChange(new DataChangedEvent(each.getKeyValue().getKey().toString(Charsets.UTF_8), each.getKeyValue().getValue().toString(Charsets.UTF_8), changedType));
                }
            }
        });
        client.getWatchClient().watch(ByteSequence.from(key, Charsets.UTF_8), listener);
    }
    
    private ChangedType getEventChangedType(final WatchEvent event) {
        switch (event.getEventType()) {
            case PUT:
                return ChangedType.UPDATED;
            case DELETE:
                return ChangedType.DELETED;
            default:
                return ChangedType.IGNORED;
        }
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
