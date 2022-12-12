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

package org.apache.shardingsphere.mode.repository.cluster.nacos.listener;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;
import org.apache.shardingsphere.mode.repository.cluster.nacos.utils.NacosMetaDataUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Naming event listener.
 */
public final class NamingEventListener implements EventListener {
    
    private Map<String, Instance> preInstances = new HashMap<>();
    
    private final Map<String, DataChangedEventListener> prefixListenerMap = new HashMap<>();
    
    @Override
    public void onEvent(final Event event) {
        if (!(event instanceof NamingEvent)) {
            return;
        }
        NamingEvent namingEvent = (NamingEvent) event;
        List<Instance> instances = namingEvent.getInstances().stream().sorted(Comparator.comparing(NacosMetaDataUtil::getKey)).collect(Collectors.toList());
        List<WatchData> watchDataList = new LinkedList<>();
        synchronized (this) {
            instances.forEach(instance -> prefixListenerMap.forEach((prefixPath, listener) -> {
                String key = NacosMetaDataUtil.getKey(instance);
                if (key.startsWith(prefixPath)) {
                    Instance preInstance = preInstances.remove(key);
                    WatchData watchData = new WatchData(key, preInstance, instance, listener);
                    watchDataList.add(watchData);
                }
            }));
            preInstances.values().stream().sorted(Comparator.comparing(NacosMetaDataUtil::getKey).reversed()).forEach(instance -> prefixListenerMap.forEach((prefixPath, listener) -> {
                String key = NacosMetaDataUtil.getKey(instance);
                if (key.startsWith(prefixPath)) {
                    WatchData watchData = new WatchData(key, instance, null, listener);
                    watchDataList.add(watchData);
                }
            }));
            watchDataList.forEach(watchData -> {
                String key = watchData.getKey();
                Instance preInstance = watchData.getPreInstance();
                Instance instance = watchData.getInstance();
                DataChangedEventListener listener = watchData.getListener();
                DataChangedEvent.Type changedType = getEventChangedType(preInstance, instance);
                switch (changedType) {
                    case ADDED:
                    case UPDATED:
                        listener.onChange(new DataChangedEvent(key, NacosMetaDataUtil.getValue(instance), changedType));
                        break;
                    case DELETED:
                        listener.onChange(new DataChangedEvent(key, NacosMetaDataUtil.getValue(preInstance), changedType));
                        break;
                    default:
                }
            });
            setPreInstances(instances);
        }
    }
    
    private Type getEventChangedType(final Instance preInstance, final Instance instance) {
        if (Objects.isNull(preInstance) && Objects.nonNull(instance)) {
            return DataChangedEvent.Type.ADDED;
        }
        if (Objects.nonNull(preInstance) && Objects.nonNull(instance) && NacosMetaDataUtil.getTimestamp(preInstance) != NacosMetaDataUtil.getTimestamp(instance)) {
            return DataChangedEvent.Type.UPDATED;
        }
        if (Objects.nonNull(preInstance) && Objects.isNull(instance)) {
            return DataChangedEvent.Type.DELETED;
        }
        return DataChangedEvent.Type.IGNORED;
    }
    
    /**
     * Update pre instances.
     *
     * @param instances instances
     */
    public void setPreInstances(final List<Instance> instances) {
        preInstances = instances.stream().filter(instance -> {
            for (String each : prefixListenerMap.keySet()) {
                if (NacosMetaDataUtil.getKey(instance).startsWith(each)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toMap(NacosMetaDataUtil::getKey, Function.identity(), (a, b) -> NacosMetaDataUtil.getTimestamp(a) > NacosMetaDataUtil.getTimestamp(b) ? a : b));
    }
    
    /**
     * Put prefix path and listener.
     *
     * @param prefixPath prefix path
     * @param listener listener
     */
    public synchronized void put(final String prefixPath, final DataChangedEventListener listener) {
        prefixListenerMap.put(prefixPath, listener);
    }
}
