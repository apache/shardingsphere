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

package org.apache.shardingsphere.mode.manager.cluster.listener;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.manager.cluster.event.builder.DispatchEventBuilder;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

/**
 * Data changed event listener registry.
 */
public final class DataChangedEventListenerRegistry {
    
    private final DataChangedEventListenerManager listenerManager;
    
    private final EventBusContext eventBusContext;
    
    private final Collection<String> databaseNames;
    
    public DataChangedEventListenerRegistry(final ContextManager contextManager, final Collection<String> databaseNames) {
        listenerManager = new DataChangedEventListenerManager((ClusterPersistRepository) contextManager.getPersistServiceFacade().getRepository());
        eventBusContext = contextManager.getComputeNodeInstanceContext().getEventBusContext();
        this.databaseNames = databaseNames;
    }
    
    /**
     * Register data changed event listeners.
     */
    public void register() {
        registerDatabaseRequiredListeners();
        for (DispatchEventBuilder<?> each : ShardingSphereServiceLoader.getServiceInstances(DispatchEventBuilder.class)) {
            register(each);
        }
    }
    
    private void register(final DispatchEventBuilder<?> builder) {
        for (String each : builder.getSubscribedKeys()) {
            register(each, builder);
        }
    }
    
    private void register(final String subscribedKey, final DispatchEventBuilder<?> builder) {
        listenerManager.addListener(subscribedKey, dataChangedEvent -> {
            if (builder.getSubscribedTypes().contains(dataChangedEvent.getType())) {
                builder.build(dataChangedEvent).ifPresent(eventBusContext::post);
            }
        });
    }
    
    private void registerDatabaseRequiredListeners() {
        registerMetaDataChangedEventListener();
    }
    
    private void registerMetaDataChangedEventListener() {
        databaseNames.forEach(each -> listenerManager.addListener(DatabaseMetaDataNode.getDatabaseNamePath(each), new MetaDataChangedListener(eventBusContext)));
    }
}
